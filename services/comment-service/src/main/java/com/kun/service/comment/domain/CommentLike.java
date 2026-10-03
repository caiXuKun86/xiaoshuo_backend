package com.kun.service.comment.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kun.common.database.entity.BaseEntity;
import lombok.Data;

import java.io.Serial;

/**
 * 评论点赞记录表
 * @TableName comment_like
 */
@TableName(value ="comment_like")
@Data
public class CommentLike extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键 ID (雪花算法)
     */
    @TableId
    private Long id;

    /**
     * 点赞用户 ID
     */
    private Long userId;

    /**
     * 被点赞的评论 ID
     */
    private Long commentId;

    /**
     * 点赞状态 (1:已点赞 0:已取消)
     */
    private Integer status;


}