package com.kun.service.comment.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentPageRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    private Long id;
    /**
     * 发评人用户 ID
     */
    private Long userId;

    /**
     * 发评人昵称 (冗余快照，避免高频联表查询 user)
     */
    private String userNickname;

    /**
     * 发评人头像 URL (冗余快照，列表秒级渲染)
     */
    private String userAvatar;


    /**
     * 评论文本内容 (已过敏感词过滤)
     */
    private String content;
    /**
     * 点赞总数 (Redis 异步定时批量回写)
     */
    private Integer likeCount;

    /**
     * 楼中楼回复总数 (仅 root_id = 0 的根评论维护)
     */
    private Integer replyCount;

    /**
     * 是否点赞
     */
    private Boolean isLiked;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}

