package com.kun.service.book.task;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kun.common.core.enums.BookOpStatusEnum;
import com.kun.common.core.enums.BookStatusEnum;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.mapper.BookInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookRankScheduleTask {

    private final StringRedisTemplate stringRedisTemplate;
    private final BookInfoMapper bookInfoMapper;

    /**
     * 每 60 分钟重算并刷新一次排行榜
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void refreshBookRank() {
        log.info("开始定时刷新小说排行榜数据...");
        refreshNewBookRank();
        refreshCompletedRank();
        log.info("小说排行榜数据刷新完成");
    }


    // 1. 新书榜：按创建时间取前 100
    private void refreshNewBookRank() {
        String key = RedisKeyConstants.RANK_NEW_BOOKS;
        LambdaQueryWrapper<BookInfo> qw = new LambdaQueryWrapper<>();
        qw.eq(BookInfo::getStatus, BookOpStatusEnum.ON_SHELF.getCode())
                .eq(BookInfo::getBookStatus, BookStatusEnum.SERIALIZING.getCode())
                .gt(BookInfo::getCreateTime, LocalDateTime.now().minusYears(1))
                .gt(BookInfo::getCollectCount, 10)
                .gt(BookInfo::getWordCount, 10000)
                .last("ORDER BY (collect_count / 10 * 0.35 + score * total_score / 25 * 0.6 + word_count / 1000 * 0.05) DESC LIMIT 100");
        List<BookInfo> list = bookInfoMapper.selectList(qw);

        updateZSetRank(key, list, book -> {
            double collectCount = book.getCollectCount() != null ? book.getCollectCount() : 0.0;
            double score = book.getScore() != null ? book.getScore().doubleValue() : 0.0;
            double totalScore = book.getTotalScore() != null ? book.getTotalScore() : 0.0;
            double wordCount = book.getWordCount() != null ? book.getWordCount() : 0.0;

            return (collectCount / 10.0 * 0.35)
                    + (score * totalScore / 25.0 * 0.6)
                    + (wordCount / 1000.0 * 0.05);
        });
    }

    // 2. 完本榜：评分 收藏数 字数 1000 000
    private void refreshCompletedRank() {
        String key = RedisKeyConstants.RANK_COMPLETED;

        LambdaQueryWrapper<BookInfo> qw = new LambdaQueryWrapper<>();
        qw.eq(BookInfo::getStatus, BookOpStatusEnum.ON_SHELF.getCode())
                .eq(BookInfo::getBookStatus, BookStatusEnum.FINISHED.getCode())
                .gt(BookInfo::getCollectCount, 10)
                .gt(BookInfo::getWordCount, 10000)
                .last("ORDER BY (collect_count / 10 * 0.4 + score * total_score / 25 * 0.5 + word_count / 1000 * 0.1) DESC LIMIT 100");
        List<BookInfo> list = bookInfoMapper.selectList(qw);

        updateZSetRank(key, list, book -> {
            double collectCount = book.getCollectCount() != null ? book.getCollectCount() : 0.0;
            double score = book.getScore() != null ? book.getScore().doubleValue() : 0.0;
            double totalScore = book.getTotalScore() != null ? book.getTotalScore() : 0.0;
            double wordCount = book.getWordCount() != null ? book.getWordCount() : 0.0;

            return (collectCount / 10.0 * 0.4)
                    + (score * totalScore / 25.0 * 0.5)
                    + (wordCount / 1000.0 * 0.1);
        });
    }

    private void updateZSetRank(String key, List<BookInfo> list, ToDoubleFunction<BookInfo> scoreFunc) {
        if (CollUtil.isEmpty(list)) return;
        Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
        for (BookInfo book : list) {
            tuples.add(new DefaultTypedTuple<>(book.getId().toString(), scoreFunc.applyAsDouble(book)));
        }

        // 使用临时 key 双写切换，避免删除瞬间产生空窗期
        String tempKey = key + ":temp";
        stringRedisTemplate.opsForZSet().add(tempKey, tuples);
        stringRedisTemplate.rename(tempKey, key);
        stringRedisTemplate.expire(key, 3, TimeUnit.HOURS); // 3小时兜底过期


    }
}