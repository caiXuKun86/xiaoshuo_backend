package com.kun.service.comment.mapper;

import com.kun.service.comment.domain.CommentLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kun.service.comment.mq.event.CommentLikeUpdateEvent;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author Lenovo
* @description 针对表【comment_like(评论点赞记录表)】的数据库操作Mapper
* @createDate 2026-10-02 19:42:59
* @Entity com.kun.service.comment.domain.CommentLike
*/
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

    void batchUpsert(@Param("list") List<CommentLikeUpdateEvent> deduplicatedEvents);

    void batchUpdateLikeCount(@Param("deltaMap") Map<Long, Integer> validDeltaMap);
}




