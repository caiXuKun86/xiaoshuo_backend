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
public class ParagraphCommentPageRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    private Integer paragraphIndex;

    /**
     * 评论 ID
     */
    private Long id;
    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 发评人昵称 (冗余快照，避免高频联表查询 user)
     */
    private String userNickname;
    /**
     * 评论文本内容 (已过敏感词过滤)
     */
    private String content;

    /**
     * 点赞总数 (Redis 异步定时批量回写)
     */
    private Integer likeCount;
    /**
     * 回复总数
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

