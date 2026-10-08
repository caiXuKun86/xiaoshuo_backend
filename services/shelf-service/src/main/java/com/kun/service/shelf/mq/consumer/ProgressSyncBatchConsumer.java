package com.kun.service.shelf.mq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.kun.service.shelf.domain.Bookshelf;
import com.kun.service.shelf.domain.ReadHistory;
import com.kun.service.shelf.mq.message.ReadingProgressSyncEvent;
import com.kun.service.shelf.service.BookshelfService;
import com.kun.service.shelf.service.ReadHistoryService;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProgressSyncBatchConsumer implements MessageListenerConcurrently, InitializingBean, DisposableBean {

    @Value("${rocketmq.namesrv-addr:127.0.0.1:9876}")
    private String namesrvAddr;

    // 当前消费者专属配置
    private static final String CONSUMER_GROUP = "shelf-progress-sync-consumer-group";
    private static final String TOPIC = "shelf-topic";
    private static final String TAG = "tag-progress-sync";

    private DefaultMQPushConsumer consumer;
    private final BookshelfService bookshelfService;
    private final ReadHistoryService readHistoryService;

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

        try {
            Map<String, ReadingProgressSyncEvent> latestMap = new HashMap<>();
            for (MessageExt messageExt : records) {
                String body = new String(messageExt.getBody());
                ReadingProgressSyncEvent event = JSONUtil.toBean(body, ReadingProgressSyncEvent.class);
                if (event.getUserId() == null || event.getBookId() == null) {
                    continue; // 过滤空消息/脏数据，防止 NPE
                }

                String uniqueKey = event.getUserId() + ":" + event.getBookId();
                // 如果已存在，比较时间，保留更新的那条
                latestMap.merge(uniqueKey, event, (oldVal, newVal) ->
                        newVal.getEventTime() > oldVal.getEventTime() ? newVal : oldVal
                );
            }
            List<ReadingProgressSyncEvent> batchList = new ArrayList<>(latestMap.values());
            for (ReadingProgressSyncEvent item : batchList) {

                LocalDateTime lastReadTime = Instant.ofEpochMilli(item.getEventTime())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();                // 根据 userId 和 bookId 更新书架进度，找不到就不更新，绝不会报错
                bookshelfService.lambdaUpdate()
                        .eq(Bookshelf::getUserId, item.getUserId())
                        .eq(Bookshelf::getBookId, item.getBookId())
                        .apply("last_read_time IS NULL OR last_read_time <= {0}", lastReadTime)
                        .set(Bookshelf::getLastReadChapterId, item.getChapterId())
                        .set(Bookshelf::getLastReadChapterIndex, item.getChapterIndex())
                        .set(Bookshelf::getLastReadChapterName, item.getChapterName())
                        .set(Bookshelf::getLastReadParagraph, item.getParagraphIndex())
                        .set(Bookshelf::getReadPercent, item.getReadPercent())
                        .set(Bookshelf::getLastReadTime, lastReadTime)
                        .update();
                // 1. 查一下之前有没有看过这本书
                ReadHistory history = readHistoryService.lambdaQuery()
                        .eq(ReadHistory::getUserId, item.getUserId())
                        .eq(ReadHistory::getBookId, item.getBookId())
                        .one();

                if (history == null) {
                    // 2. 数据库里没有 -> 新增一条记录
                    history = new ReadHistory();
                    history.setUserId(item.getUserId());
                    history.setBookId(item.getBookId());
                    history.setLastReadChapterId(item.getChapterId());
                    history.setLastReadChapterIndex(item.getChapterIndex());
                    history.setLastReadChapterName(item.getChapterName());
                    history.setLastReadParagraph(item.getParagraphIndex());
                    history.setReadPercent(item.getReadPercent());
                    history.setLastReadTime(lastReadTime);

                    readHistoryService.save(history); // 插入
                } else {
                    // 3. 数据库里已有 -> 更新进度
                    readHistoryService.lambdaUpdate()
                            .eq(ReadHistory::getUserId, item.getUserId())
                            .eq(ReadHistory::getBookId, item.getBookId())
                            .apply("last_read_time IS NULL OR last_read_time <= {0}", lastReadTime)
                            .set(ReadHistory::getLastReadChapterId, item.getChapterId())
                            .set(ReadHistory::getLastReadChapterIndex, item.getChapterIndex())
                            .set(ReadHistory::getLastReadChapterName, item.getChapterName())
                            .set(ReadHistory::getLastReadParagraph, item.getParagraphIndex())
                            .set(ReadHistory::getReadPercent, item.getReadPercent())
                            .set(ReadHistory::getLastReadTime, lastReadTime)
                            .update();
                }
            }

            log.info("MQ批量落盘成功，原始消息数: {}, 折叠后落盘数: {}", records.size(), batchList.size());

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