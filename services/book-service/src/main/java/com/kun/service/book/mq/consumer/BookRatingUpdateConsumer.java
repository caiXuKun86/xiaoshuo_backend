package com.kun.service.book.mq.consumer;

import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.mq.event.BookRatingUpdateEvent;
import com.kun.service.book.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "comment-topic",
        consumerGroup = "comment-rate-update-consumer-group",
        consumeMode = ConsumeMode.CONCURRENTLY,
        selectorExpression = "tag-bookRating-update")
@RequiredArgsConstructor
public class BookRatingUpdateConsumer implements RocketMQListener<BookRatingUpdateEvent> {

    private final BookInfoService bookInfoService;

    @Override
    public void onMessage(BookRatingUpdateEvent event) {
        if (event == null || event.getBookId() == null) {
            return;
        }
        Long bookId = event.getBookId();
        BookInfo bookInfo = bookInfoService.getById(bookId);
        if (bookInfo == null) {
            return;
        }
        Integer totalScore = (bookInfo.getTotalScore() == null ? 0 : bookInfo.getTotalScore()) + event.getUpdateScore();
        Integer ratingCount = (bookInfo.getRatingCount() == null ? 0 : bookInfo.getRatingCount()) + 1;
        BigDecimal latestScore = BigDecimal.valueOf(totalScore)
                .divide(BigDecimal.valueOf(ratingCount), 1, RoundingMode.HALF_UP);
        bookInfoService.lambdaUpdate()
                .eq(BookInfo::getId, bookId)
                .set(BookInfo::getTotalScore, totalScore)
                .set(BookInfo::getRatingCount, ratingCount)
                .set(BookInfo::getScore, latestScore)
                .update();
        log.info("图书评分更新成功, bookId={}, newTotalScore={}, newRatingCount={}", bookId, totalScore, ratingCount);
    }
}