package com.kun.service.book.dto.admin.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminBookInfoPageReqDTO extends com.kun.common.database.page.PageRequest {

    /**
     * 频道筛选
     */
    private Long channelId;

    /**
     * 二级分类 ID
     */
    private Long categoryId;

    /**
     * 作者笔名 (冗余展示)
     */
    private String authorName;
    /**
     * 连载状态
     */
    private Integer bookStatus;
    /**
     * 运营状态 (0:草稿 1:上架 2:下架封禁)
     */
    private Integer status;
    /**
     * 小说书名
     */
    private String bookName;

    /**
     * 最小总字数
     */
    private Integer minWordCount;

    /**
     * 最大总字数
     */
    private Integer maxWordCount;


}
