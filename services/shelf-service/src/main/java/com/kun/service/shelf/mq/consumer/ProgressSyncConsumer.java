package com.kun.service.shelf.mq.consumer;

import com.kun.service.shelf.domain.Bookshelf;
import com.kun.service.shelf.domain.ReadHistory;
import com.kun.service.shelf.mq.message.ReadingProgressSyncEvent;
import com.kun.service.shelf.service.BookshelfService;
import com.kun.service.shelf.service.ReadHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "shelf-topic",
        consumerGroup = "shelf-progress-consumer-group",
        consumeMode = ConsumeMode.CONCURRENTLY,
        selectorExpression = "tag-progress-sync")
@RequiredArgsConstructor
// 1. 去掉 RocketMQPushConsumerLifecycleListener，泛型改成单对象
public class ProgressSyncConsumer implements RocketMQListener<ReadingProgressSyncEvent> {

    private final BookshelfService bookshelfService;
    private final ReadHistoryService readHistoryService;

    // 2. 接收单个事件对象
    @Override
    public void onMessage(ReadingProgressSyncEvent item) {
        if (item == null || item.getUserId() == null || item.getBookId() == null) {
            return;
        }

        try {
            // 1. 根据 userId 和 bookId 更新书架进度
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

            // 2. 查一下之前有没有看过这本书并更新/插入 readHistory
            ReadHistory history = readHistoryService.lambdaQuery()
                    .eq(ReadHistory::getUserId, item.getUserId())
                    .eq(ReadHistory::getBookId, item.getBookId())
                    .one();

            if (history == null) {
                history = new ReadHistory();
                history.setUserId(item.getUserId());
                history.setBookId(item.getBookId());
                history.setLastReadChapterId(item.getChapterId());
                history.setLastReadChapterIndex(item.getChapterIndex());
                history.setLastReadChapterName(item.getChapterName());
                history.setLastReadParagraph(item.getParagraphIndex());
                history.setReadPercent(item.getReadPercent());
                history.setLastReadTime(item.getSyncTime());
                readHistoryService.save(history);
            } else {
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

            log.info("阅读进度同步落库成功, userId={}, bookId={}", item.getUserId(), item.getBookId());

        } catch (Exception e) {
            log.error("阅读进度消费逻辑异常, userId={}, bookId={}", item.getUserId(), item.getBookId(), e);
        }
    }
}