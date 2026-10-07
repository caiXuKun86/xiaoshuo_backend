package com.kun.service.comment.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BubbleCommentCountQueryRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 章节Id
     */
    private Long chapterId;
    /**
     * 评论总数
     */
    private Integer totalParagraphComments;
    /**
     * 各段的评论数量
     */
    private Map<String, Integer> bubbles;


}

