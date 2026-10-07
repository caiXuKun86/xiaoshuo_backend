package com.kun.service.comment.dto.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CommentPageReqDTO extends com.kun.common.database.page.PageRequest {

    /**
     * 频道筛选
     */
    private Long bookId;

    /**
     * 二级分类 ID
     */
    private Long chapterId;
    /**
     * 评论类型
     */
    private Integer commentType;


}
