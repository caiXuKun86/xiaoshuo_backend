package com.kun.service.comment.mq.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentLikeUpdateEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long Id;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 评论Id
     */
    private Long commentId;
    /**
     * 点赞状态 (0 已取消 1 已点赞)
     */
    private Integer status;
    /**
     * 事件发生时间戳（毫秒），用于防乱序覆盖
     */
    private Long eventTime;


}

