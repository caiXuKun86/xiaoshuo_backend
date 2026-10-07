package com.kun.service.comment.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentLikeRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 评论Id
     */
    private Long commentId;
    /**
     * 是否点赞
     */
    private Boolean isLiked;
    /**
     * 点赞数量
     */
    private Integer currentLikeCount;


}

