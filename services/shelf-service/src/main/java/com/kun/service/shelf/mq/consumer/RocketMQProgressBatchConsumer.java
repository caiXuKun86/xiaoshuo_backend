package com.kun.service.shelf.mq.consumer;

import cn.hutool.core.collection.CollUtil;
import com.kun.service.shelf.domain.Bookshelf;
import com.kun.service.shelf.domain.ReadHistory;
import com.kun.service.shelf.mq.message.ReadingProgressSyncEvent;
import com.kun.service.shelf.service.BookshelfService;
import com.kun.service.shelf.service.ReadHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
@Slf4j
//@Component
//@RocketMQMessageListener(
//        topic = "shelf-topic",
//        consumerGroup = "shelf-progress-consumer-group",
//        consumeMode = ConsumeMode.CONCURRENTLY,
//        selectorExpression = "tag-progress-sync")
@RequiredArgsConstructor
public class RocketMQProgressBatchConsumer implements RocketMQListener<List<ReadingProgressSyncEvent>>, RocketMQPushConsumerLifecycleListener {

    private final BookshelfService bookshelfService;
    private final ReadHistoryService readHistoryService;

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        // 设置每次批量消费的最大消息数 (例如攒够 64 条或达到超时时间推过来)
        consumer.setConsumeMessageBatchMaxSize(64);
    }

    @Override
    public void onMessage(List<ReadingProgressSyncEvent> events) {

        if (CollUtil.isEmpty(events)) {
            return;
        }
        try {
            // 核心技术点：写折叠 (Write Coalescing / 内存去重)
            // 这一批 64 条消息里，同一个用户对同一本书可能上报了 5 次，只保留最新一次！
            Map<String, ReadingProgressSyncEvent> latestMap = new HashMap<>();
            for (ReadingProgressSyncEvent payload : events) {
                if (payload.getUserId() == null || payload.getBookId() == null) {
                    continue; // 过滤空消息/脏数据，防止 NPE
                }

                String uniqueKey = payload.getUserId() + ":" + payload.getBookId();
                // 如果已存在，比较时间，保留更新的那条
                latestMap.merge(uniqueKey, payload, (oldVal, newVal) ->
                        newVal.getSyncTime().isAfter(oldVal.getSyncTime()) ? newVal : oldVal
                );
            }

            List<ReadingProgressSyncEvent> batchList = new ArrayList<>(latestMap.values());
            for (ReadingProgressSyncEvent item : batchList) {
                // 根据 userId 和 bookId 更新书架进度，找不到就不更新，绝不会报错
                bookshelfService.lambdaUpdate()
                        .eq(Bookshelf::getUserId, item.getUserId())
                        .eq(Bookshelf::getBookId, item.getBookId())
                        .apply("last_read_time IS NULL OR last_read_time <= {0}", item.getSyncTime())
                        .set(Bookshelf::getLastReadChapterId, item.getChapterId())
                        .set(Bookshelf::getLastReadChapterIndex, item.getChapterIndex())
                        .set(Bookshelf::getLastReadChapterName, item.getChapterName())
                        .set(Bookshelf::getLastReadParagraph, item.getParagraphIndex())
                        .set(Bookshelf::getReadPercent, item.getReadPercent())
                        .set(Bookshelf::getLastReadTime, item.getSyncTime())
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
                    history.setLastReadTime(item.getSyncTime());

                    readHistoryService.save(history); // 插入
                } else {
                    // 3. 数据库里已有 -> 更新进度
                    readHistoryService.lambdaUpdate()
                            .eq(ReadHistory::getUserId, item.getUserId())
                            .eq(ReadHistory::getBookId, item.getBookId())
                            .apply("last_read_time IS NULL OR last_read_time <= {0}", item.getSyncTime())
                            .set(ReadHistory::getLastReadChapterId, item.getChapterId())
                            .set(ReadHistory::getLastReadChapterIndex, item.getChapterIndex())
                            .set(ReadHistory::getLastReadChapterName, item.getChapterName())
                            .set(ReadHistory::getLastReadParagraph, item.getParagraphIndex())
                            .set(ReadHistory::getReadPercent, item.getReadPercent())
                            .set(ReadHistory::getLastReadTime, item.getSyncTime())
                            .update();
                }
            }


            log.info("MQ批量落盘成功，原始消息数: {}, 折叠后落盘数: {}", events.size(), batchList.size());


        } catch (Exception e) {
            // 4. 极端未知异常捕获：
            // 因为 Redis 中已有最新数据保障跨端续读，这里记录 error 日志告警，
            // 避免整批消息陷入 16 次死循环重试打满 MQ。
            log.error("阅读进度消费逻辑严重异常，本批次消息被跳过，数量: {}", events.size(), e);
        }


    }
}