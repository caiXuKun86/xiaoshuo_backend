package com.kun.service.book.mq.consumer;

import cn.hutool.core.collection.CollUtil;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.mq.event.BookRatingUpdateEvent;
import com.kun.service.book.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "comment-topic",
        consumerGroup = "comment-rate-update-consumer-group",
        consumeMode = ConsumeMode.CONCURRENTLY,
        selectorExpression = "tag-bookRating-update")
@RequiredArgsConstructor
public class RocketMQProgressBatchConsumer implements RocketMQListener<List<Message<BookRatingUpdateEvent>>>, RocketMQPushConsumerLifecycleListener {

    private final BookInfoService bookInfoService;

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        // 设置每次批量消费的最大消息数 (例如攒够 64 条或达到超时时间推过来)
        consumer.setConsumeMessageBatchMaxSize(64);
    }

    @Override
    public void onMessage(List<Message<BookRatingUpdateEvent>> messages) {
        if (CollUtil.isEmpty(messages)) {
            return;
        }
        Map<Long, List<BookRatingUpdateEvent>> collect = messages.stream().map(Message::getPayload).collect(Collectors.groupingBy(BookRatingUpdateEvent::getBookId));

        for (Map.Entry<Long, List<BookRatingUpdateEvent>> entry : collect.entrySet()) {
            Long bookId = entry.getKey();
            BookInfo bookInfo = bookInfoService.getById(bookId);
            Integer totalScore = bookInfo.getTotalScore();
            Integer ratingCount = bookInfo.getRatingCount();
            for (BookRatingUpdateEvent event : entry.getValue()) {
                totalScore += event.getUpdateScore();
                ratingCount++;
            }
            bookInfoService.lambdaUpdate()
                    .eq(BookInfo::getId, bookId)
                    .set(BookInfo::getTotalScore, totalScore)
                    .set(BookInfo::getRatingCount, ratingCount)
                    .update();
        }
    }
}