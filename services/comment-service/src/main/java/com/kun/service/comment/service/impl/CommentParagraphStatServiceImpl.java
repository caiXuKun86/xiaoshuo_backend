package com.kun.service.comment.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.service.comment.domain.CommentParagraphStat;
import com.kun.service.comment.dto.resp.BubbleCommentCountQueryRespDTO;
import com.kun.service.comment.mapper.CommentParagraphStatMapper;
import com.kun.service.comment.service.CommentParagraphStatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Lenovo
 * @description 针对表【comment_paragraph_stat(段落评论聚合统计表)】的数据库操作Service实现
 * @createDate 2026-10-02 19:42:59
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CommentParagraphStatServiceImpl extends ServiceImpl<CommentParagraphStatMapper, CommentParagraphStat>
        implements CommentParagraphStatService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * * 防穿透空标记 Field
     */
    private static final String EMPTY_CACHE_FLAG = "-1";

    @Override
    public BubbleCommentCountQueryRespDTO getParagraphBubbles(Long chapterId) {
        String cacheKey = String.format(RedisKeyConstants.COMMENT_PARA_STAT_PREFIX, chapterId);
        Map<Object, Object> rawEntries = stringRedisTemplate.opsForHash().entries(cacheKey);

        if (CollUtil.isNotEmpty(rawEntries)) {
            if (rawEntries.containsKey(EMPTY_CACHE_FLAG)) {
                return BubbleCommentCountQueryRespDTO.builder()
                        .chapterId(chapterId)
                        .totalParagraphComments(0)
                        .bubbles(Collections.emptyMap())
                        .build();
            }
            Map<String, Integer> bubbles = new HashMap<>(rawEntries.size());
            int total = 0;
            for (Map.Entry<Object, Object> entry : rawEntries.entrySet()) {
                String paraIdx = entry.getKey().toString();
                try {
                    int count = Integer.parseInt(entry.getValue().toString());
                    if (count > 0) {
                        bubbles.put(paraIdx, count);
                        total += count;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            return BubbleCommentCountQueryRespDTO.builder()
                    .chapterId(chapterId)
                    .totalParagraphComments(total)
                    .bubbles(bubbles)
                    .build();

        }
        // 2. 缓存未命中，回源查询数据库统计表 comment_paragraph_stat
        List<CommentParagraphStat> statList = this.lambdaQuery()
                .eq(CommentParagraphStat::getChapterId, chapterId)
                .gt(CommentParagraphStat::getCommentCount, 0)
                .list();
        // 3. 数据库无任何段评记录时，写入防穿透标记（设置较短 TTL，例如 5 分钟）
        if (CollUtil.isEmpty(statList)) {
            stringRedisTemplate.opsForHash().put(cacheKey, EMPTY_CACHE_FLAG, "0");
            stringRedisTemplate.expire(cacheKey, Duration.ofMinutes(5));
            return BubbleCommentCountQueryRespDTO.builder()
                    .chapterId(chapterId)
                    .totalParagraphComments(0)
                    .bubbles(Collections.emptyMap())
                    .build();
        }
        // 4. 组装数据并回写 Redis Hash
        Map<String, String> cacheMap = new HashMap<>(statList.size());
        Map<String, Integer> bubbles = new HashMap<>(statList.size());
        int total = 0;
        for (CommentParagraphStat stat : statList) {
            String paraIdx = String.valueOf(stat.getParagraphIndex());
            Integer count = stat.getCommentCount();
            if (count != null && count > 0) {
                cacheMap.put(paraIdx, String.valueOf(count));
                bubbles.put(paraIdx, count);
                total += count;
            }
        }
        // 回写缓存，并设置随机过期时间（如 24 小时 + 随机 0~3600 秒防雪崩）
        if (!cacheMap.isEmpty()) {
            stringRedisTemplate.opsForHash().putAll(cacheKey, cacheMap);
            long ttlSeconds = Duration.ofDays(1).toSeconds() + ThreadLocalRandom.current().nextInt(0, 3600);
            stringRedisTemplate.expire(cacheKey, Duration.ofSeconds(ttlSeconds));
        }
        return BubbleCommentCountQueryRespDTO.builder()
                .chapterId(chapterId)
                .totalParagraphComments(total)
                .bubbles(bubbles)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void incrParagraphCommentCount(Long bookId, Long chapterId, Integer paragraphIndex) {
        if (chapterId == null || paragraphIndex == null || paragraphIndex <= 0) {
            return;
        }
        // 1. 持久层原子累加
        CommentParagraphStat stat = this.lambdaQuery()
                .eq(CommentParagraphStat::getChapterId, chapterId)
                .eq(CommentParagraphStat::getParagraphIndex, paragraphIndex)
                .one();
        if (stat == null) {
            stat = new CommentParagraphStat();
            stat.setBookId(bookId);
            stat.setChapterId(chapterId);
            stat.setParagraphIndex(paragraphIndex);
            stat.setCommentCount(1);
            this.save(stat);
        } else {
            this.lambdaUpdate()
                    .setSql("comment_count = comment_count + 1")
                    .eq(CommentParagraphStat::getId, stat.getId())
                    .update();
        }
        // 2. Redis Hash: 仅在 Key 存在时原子自增（HINCRBY），若不存在交由 Cache-Aside 读时懒加载回源
        String cacheKey = String.format(RedisKeyConstants.COMMENT_PARA_STAT_PREFIX, chapterId);
        Boolean hasKey = stringRedisTemplate.hasKey(cacheKey);
        if (hasKey) {
            stringRedisTemplate.opsForHash().delete(cacheKey, EMPTY_CACHE_FLAG);
            stringRedisTemplate.opsForHash().increment(cacheKey, String.valueOf(paragraphIndex), 1);
        }
    }
}




