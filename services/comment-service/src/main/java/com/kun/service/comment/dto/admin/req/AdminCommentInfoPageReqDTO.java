package com.kun.service.comment.dto.admin.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminCommentInfoPageReqDTO extends com.kun.common.database.page.PageRequest {

    /**
     * 频道筛选
     */
    private Long bookId;

    /**
     * 章节 ID
     */
    private Long chapterId;
    /**
     * 评论类型
     */
    private Integer commentType;
    /**
     * 评论状态
     */
    private Integer status;
    /**
     * 是否是Root
     */
    private Boolean rootOnly;
    /**
     * 关键字
     */
    private String keyWord;

    /**
     * 发布人昵称
     */
    private String userNickName;
    /**
     * 发布人Id
     */
    private Long userId;


}
