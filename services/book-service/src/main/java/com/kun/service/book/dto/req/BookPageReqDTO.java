package com.kun.service.book.dto.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookPageReqDTO extends com.kun.common.database.page.PageRequest {

    /**
     * 频道筛选
     */
    private Integer channelId;

    /**
     * 二级分类 ID
     */
    private Long categoryId;

    /**
     * 连载状态
     */
    private Integer bookStatus;

    /**
     * 字数区间
     */
    private Integer wordRange;


}
