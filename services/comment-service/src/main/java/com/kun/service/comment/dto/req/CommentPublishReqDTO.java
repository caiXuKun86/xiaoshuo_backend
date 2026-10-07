package com.kun.service.comment.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentPublishReqDTO implements Serializable {


    /**
     * 所属图书 ID
     */
    private Long bookId;

    /**
     * 所属章节 ID (针对全书的书评填 0)
     */
    private Long chapterId;

    /**
     * 评论类型 (1:整本书评 2:章节评论 3:段落吐槽/本章说)
     */
    private Integer commentType;

    /**
     * 段落序号 (从 1 开始；书评/章评填 0)
     */
    private Integer paragraphIndex;


    /**
     * 内容
     */
    private String content;


}

