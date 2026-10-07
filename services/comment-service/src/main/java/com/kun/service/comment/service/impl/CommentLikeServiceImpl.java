package com.kun.service.comment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.service.comment.domain.CommentLike;
import com.kun.service.comment.mapper.CommentLikeMapper;
import com.kun.service.comment.mq.event.CommentLikeUpdateEvent;
import com.kun.service.comment.service.CommentLikeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Lenovo
* @description 针对表【comment_like(评论点赞记录表)】的数据库操作Service实现
* @createDate 2026-10-02 19:42:59
*/
@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements CommentLikeService{


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processBatch(List<CommentLikeUpdateEvent> events) {
        // 1. 针对 (userId + commentId) 进行折叠，只保留最新时间的事件
        Map<String, CommentLikeUpdateEvent> latestEventMap = new HashMap<>();
        // 2. 统计每条评论的点赞增量 delta (commentId -> delta)
        Map<Long, Integer> commentDeltaMap = new HashMap<>();

        for (CommentLikeUpdateEvent event : events) {
            String key = event.getUserId() + "_" + event.getCommentId();
            CommentLikeUpdateEvent existing = latestEventMap.get(key);
            // 如果本批次中已存在该用户的操作，比对时间戳保留最新的
            if (existing == null || event.getEventTime() >= existing.getEventTime()) {
                latestEventMap.put(key, event);
            }
            // 计算增量：1 点赞 -> +1， 0 取消 -> -1
            int delta = event.getStatus() == 1 ? 1 : -1;
            commentDeltaMap.merge(event.getCommentId(), delta, Integer::sum);
        }
        // 3. 批量更新/插入 明细表 (comment_like)
        List<CommentLikeUpdateEvent> deduplicatedEvents = new ArrayList<>(latestEventMap.values());
        if (!deduplicatedEvents.isEmpty()) {
            baseMapper.batchUpsert(deduplicatedEvents);
        }
        // 4. 批量累加 评论表计数 (comment)
        // 过滤掉增量为 0 的（比如批次内该评论点赞数一增一减刚好抵消）
        Map<Long, Integer> validDeltaMap = commentDeltaMap.entrySet().stream()
                .filter(entry -> entry.getValue() != 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!validDeltaMap.isEmpty()) {
            baseMapper.batchUpdateLikeCount(validDeltaMap);
        }
    }
}




