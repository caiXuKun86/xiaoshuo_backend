package com.kun.service.book.mq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.mq.event.BookRatingUpdateEvent;
import com.kun.service.book.service.BookInfoService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookRatingUpdateBatchConsumer implements MessageListenerConcurrently, InitializingBean, DisposableBean {

    @Value("${rocketmq.namesrv-addr:127.0.0.1:9876}")
    private String namesrvAddr;

    // 当前消费者专属配置
    private static final String CONSUMER_GROUP = "comment-bookRating-update-consumer-group";
    private static final String TOPIC = "comment-topic";
    private static final String TAG = "tag-bookRating-update";

    private DefaultMQPushConsumer consumer;
    private final BookInfoService bookInfoService;
    private final TransactionTemplate transactionTemplate;

    // 1. Spring 依赖注入完成后自动执行启动逻辑
    @Override
    public void afterPropertiesSet() throws Exception {
        consumer = new DefaultMQPushConsumer(CONSUMER_GROUP);
        consumer.setNamesrvAddr(namesrvAddr);
        consumer.subscribe(TOPIC, TAG);

        // 设置并发线程数
        consumer.setConsumeThreadMin(32);
        consumer.setConsumeThreadMax(32);

        // 设置批量拉取与批量消费
        consumer.setPullBatchSize(32);
        consumer.setConsumeMessageBatchMaxSize(32);

        // 关键：把当前实例 (this) 直接传给监听器
        consumer.registerMessageListener(this);

        consumer.start();
        log.info("【消费者已启动】Topic: {}, Group: {}", TOPIC, CONSUMER_GROUP);
    }

    // 2. 核心业务消费逻辑（原生支持 List<MessageExt> 批量处理）
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> records, ConsumeConcurrentlyContext context) {
        log.info("【收到新消息】数量: {}", records.size());
        if (CollUtil.isEmpty(records)) {
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }

        // 1. 针对 (userId + bookId) 进行折叠，只保留最新时间的事件
        try {
            // 步骤 1：去重/写折叠（同一个用户对同一本书多次评分，只保留时间最新的）
            Map<String, BookRatingUpdateEvent> latestUserBookEventMap = new HashMap<>();
            for (MessageExt msg : records) {
                String body = new String(msg.getBody(), StandardCharsets.UTF_8);
                BookRatingUpdateEvent event = JSONUtil.toBean(body, BookRatingUpdateEvent.class);
                if (event == null || event.getBookId() == null || event.getUpdateScore() == null) {
                    continue;
                }
                String uniqueKey = event.getUserId() + ":" + event.getBookId();
                latestUserBookEventMap.merge(uniqueKey, event, (oldVal, newVal) -> {
                    long oldTime = oldVal.getEventTime() != null ? oldVal.getEventTime() : 0L;
                    long newTime = newVal.getEventTime() != null ? newVal.getEventTime() : 0L;
                    return newTime >= oldTime ? newVal : oldVal;
                });
            }
            // 步骤 2：按 bookId 聚合增量（计算这批消息给每本书增加了多少分、多少人评）
            List<BookRatingUpdateEvent> batchList = new ArrayList<>(latestUserBookEventMap.values());

            Map<Long, RatingDelta> bookDeltaMap = new HashMap<>();
            for (BookRatingUpdateEvent event : batchList) {
                bookDeltaMap.compute(event.getBookId(), (bookId, delta) -> {
                    if (delta == null) {
                        return new RatingDelta(event.getUpdateScore(), 1);
                    }
                    delta.addScore(event.getUpdateScore());
                    delta.incrementCount();
                    return delta;
                });
            }

            if (bookDeltaMap.isEmpty()) {
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
            List<Long> sortedBookIds = new ArrayList<>(bookDeltaMap.keySet());
            Collections.sort(sortedBookIds);
            // 步骤 3：数据库原子批量增量更新（规避并发覆盖更新问题）
            transactionTemplate.executeWithoutResult(status->{
                for (Long bookId  : sortedBookIds) {
                    RatingDelta delta = bookDeltaMap.get(bookId);
                    // 使用 MyBatis-Plus setSql 语法，让 MySQL 原子累加并重新计算平均分
                    // score = ROUND(total_score / rating_count, 1)
                    bookInfoService.lambdaUpdate()
                            .eq(BookInfo::getId, bookId)
                            .setSql("total_score = IFNULL(total_score, 0) + " + delta.getTotalScoreDelta())
                            .setSql("rating_count = IFNULL(rating_count, 0) + " + delta.getRatingCountDelta())
                            .setSql("score = ROUND((IFNULL(total_score, 0) + " + delta.getTotalScoreDelta() + ") / (IFNULL(rating_count, 0) + " + delta.getRatingCountDelta() + "), 1)")
                            .update();
                }
                log.info("MQ批量落盘成功，原始消息数: {}, 折叠后落盘数: {}", records.size(), batchList.size());

            });

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Throwable t) {
            log.error("消费处理异常，触发稍后重试", t);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    // 3. 应用关闭时平滑停机
    @Override
    public void destroy() {
        if (consumer != null) {
            consumer.shutdown();
            log.info("【消费者已平滑关闭】Group: {}", CONSUMER_GROUP);
        }
    }


    @Data
    @AllArgsConstructor
    private static class RatingDelta {
        private Integer totalScoreDelta;
        private Integer ratingCountDelta;
        public void addScore(int score) {
            this.totalScoreDelta += score;
        }
        public void incrementCount() {
            this.ratingCountDelta += 1;
        }
    }
}