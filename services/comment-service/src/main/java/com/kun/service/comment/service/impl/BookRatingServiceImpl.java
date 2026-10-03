package com.kun.service.comment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.api.client.BookFeignClient;
import com.kun.api.dto.book.BookDTO;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.core.result.Result;
import com.kun.service.comment.domain.BookRating;
import com.kun.service.comment.dto.req.BookRatingReqDTO;
import com.kun.service.comment.dto.resp.BookRatingDetailRespDTO;
import com.kun.service.comment.dto.resp.BookRatingRespDTO;
import com.kun.service.comment.mapper.BookRatingMapper;
import com.kun.service.comment.mq.event.BookRatingUpdateEvent;
import com.kun.service.comment.service.BookRatingService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lenovo
 * @description 针对表【book_rating(书籍评分表)】的数据库操作Service实现
 * @createDate 2026-10-02 19:42:59
 */
@RequiredArgsConstructor
@Service
public class BookRatingServiceImpl extends ServiceImpl<BookRatingMapper, BookRating> implements BookRatingService {

    private final RocketMQTemplate rocketMQTemplate;
    private final BookFeignClient bookFeignClient;

    @Override
    public BookRatingRespDTO bookRating(BookRatingReqDTO bookRatingReqDTO) {
        Long userId = UserContextHolder.getUserId();
        Long bookId = bookRatingReqDTO.getBookId();
        Integer score = bookRatingReqDTO.getScore();
        Integer updateScore = 0;
        if (score < 1 || score > 5) {
            throw new BusinessException(ResultCode.RATING_SCORE_ILLEGAL);
        }
        BookRating existRating = this.lambdaQuery()
                .eq(BookRating::getUserId, userId)
                .eq(BookRating::getBookId, bookId)
                .one();
        if (existRating != null) {
            updateScore = score - existRating.getScore();
            // 已评分 -> 更新覆盖
            existRating.setScore(score);
            this.updateById(existRating);
        } else {
            updateScore = score;
            // 未评分 -> 首次新增
            BookRating newRating = new BookRating();
            newRating.setUserId(userId);
            newRating.setBookId(bookId);
            newRating.setScore(score);
            this.save(newRating);
        }
        Result<BookDTO> result = bookFeignClient.getBookById(bookId);
        BookDTO bookDTO = result.getData();
        BookRatingUpdateEvent event = new BookRatingUpdateEvent(bookId, updateScore, LocalDateTime.now());
        Message<BookRatingUpdateEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(RocketMQHeaders.TAGS, "tag-bookRating-update")
                .build();

        rocketMQTemplate.sendOneWay("comment-topic", message);
        Integer ratingCount = bookDTO.getRatingCount();
        Integer totalScore = bookDTO.getTotalScore();
        BigDecimal latestScore = BigDecimal.valueOf(totalScore + updateScore)
                .divide(BigDecimal.valueOf(ratingCount + 1), 1, RoundingMode.HALF_UP);

        // 6. 构造并返回结果
        return BookRatingRespDTO.builder()
                .bookId(bookId)
                .userScore(score)
                .bookLatestScore(latestScore)
                .build();
    }

    @Override
    public BookRatingDetailRespDTO queryBookRatingDetail(Long bookId) {
        List<BookRating> ratingList = this.list();
        Map<Integer, List<BookRating>> collect = ratingList.stream().collect(Collectors.groupingBy(BookRating::getScore));
        Result<BookDTO> result = bookFeignClient.getBookById(bookId);
        BookDTO bookDTO = result.getData();
        BookRatingDetailRespDTO respDTO = new BookRatingDetailRespDTO();
        respDTO.setBookId(bookId);
        respDTO.setScore(bookDTO.getScore());
        respDTO.setRatingCount(bookDTO.getRatingCount());

        BigDecimal ratingCount = new BigDecimal(bookDTO.getRatingCount());
        BigDecimal one = new BigDecimal(collect.get(1).size()).divide(ratingCount, 1, RoundingMode.HALF_UP);
        respDTO.setOneStarPercent(one);
        BigDecimal two = new BigDecimal(collect.get(2).size()).divide(ratingCount, 1, RoundingMode.HALF_UP);
        respDTO.setTwoStarPercent(two);
        BigDecimal three = new BigDecimal(collect.get(3).size()).divide(ratingCount, 1, RoundingMode.HALF_UP);
        respDTO.setThreeStarPercent(three);
        BigDecimal four = new BigDecimal(collect.get(4).size()).divide(ratingCount, 1, RoundingMode.HALF_UP);
        respDTO.setFourStarPercent(four);
        respDTO.setFiveStarPercent(ratingCount.subtract(one).subtract(two).subtract(three).subtract(four));

        Long userId = UserContextHolder.getUserId();
        BookRating userBookRating = this.lambdaQuery()
                .eq(BookRating::getBookId, bookId)
                .eq(BookRating::getUserId, userId)
                .one();
        if (userBookRating != null) {
            respDTO.setUserScore(userBookRating.getScore());

        }
        return respDTO;


    }
}




