package com.kun.service.comment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.api.client.BookFeignClient;
import com.kun.api.dto.book.BookDTO;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.core.result.Result;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.service.comment.domain.BookRating;
import com.kun.service.comment.dto.req.BookRatingReqDTO;
import com.kun.service.comment.dto.resp.BookRatingDetailRespDTO;
import com.kun.service.comment.dto.resp.BookRatingRespDTO;
import com.kun.service.comment.mapper.BookRatingMapper;
import com.kun.service.comment.mq.event.BookRatingUpdateEvent;
import com.kun.service.comment.service.BookRatingService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
    private final RedissonClient redissonClient;

    @Override
    public BookRatingRespDTO bookRating(BookRatingReqDTO bookRatingReqDTO) {
        Long userId = UserContextHolder.getUserId();
        Long bookId = bookRatingReqDTO.getBookId();
        Integer score = bookRatingReqDTO.getScore();
        if (score < 1 || score > 5) {
            throw new BusinessException(ResultCode.RATING_SCORE_ILLEGAL);
        }
        Result<BookDTO> result = bookFeignClient.getBookById(bookId);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE);
        }
        BookDTO bookDTO = result.getData();
        RLock lock = redissonClient.getLock(String.format(RedisKeyConstants.LOCK_COMMENT_RATING, userId, bookId));
        boolean b = lock.tryLock();
        if (!b) {
            throw new BusinessException(ResultCode.REQUEST_RATE_LIMIT);
        }
        BigDecimal latestScore;
        try {
            Long count = this.lambdaQuery()
                    .eq(BookRating::getUserId, userId)
                    .eq(BookRating::getBookId, bookId)
                    .count();
            if (count > 0) {
                throw new BusinessException(ResultCode.ALREADY_RATED);
            }
            BookRating newRating = new BookRating();
            newRating.setUserId(userId);
            newRating.setBookId(bookId);
            newRating.setScore(score);
            newRating.setUserId(userId);
            this.save(newRating);

            BookRatingUpdateEvent event = new BookRatingUpdateEvent(bookId, score, LocalDateTime.now());

            rocketMQTemplate.syncSend("comment-topic:tag-bookRating-update", event);
            Integer ratingCount = bookDTO.getRatingCount();
            Integer totalScore = bookDTO.getTotalScore();
            latestScore = BigDecimal.valueOf(totalScore + score)
                    .divide(BigDecimal.valueOf(ratingCount + 1), 1, RoundingMode.HALF_UP);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }


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
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        BookDTO bookDTO = result.getData();
        BookRatingDetailRespDTO respDTO = new BookRatingDetailRespDTO();
        respDTO.setBookId(bookId);
        respDTO.setScore(bookDTO.getScore());
        respDTO.setRatingCount(bookDTO.getRatingCount());

        BigDecimal ratingCount = new BigDecimal(bookDTO.getRatingCount());
        BigDecimal one = new BigDecimal(collect.get(1).size() * 100).divide(ratingCount, 1, RoundingMode.HALF_UP);
        respDTO.setOneStarPercent(one);
        BigDecimal two = new BigDecimal(collect.get(2).size() * 100).divide(ratingCount, 1, RoundingMode.HALF_UP);
        respDTO.setTwoStarPercent(two);
        BigDecimal three = new BigDecimal(collect.get(3).size() * 100).divide(ratingCount, 1, RoundingMode.HALF_UP);
        respDTO.setThreeStarPercent(three);
        BigDecimal four = new BigDecimal(collect.get(4).size() * 100).divide(ratingCount, 1, RoundingMode.HALF_UP);
        respDTO.setFourStarPercent(four);
        respDTO.setFiveStarPercent(new BigDecimal(100).subtract(one).subtract(two).subtract(three).subtract(four));

        return respDTO;


    }
}




