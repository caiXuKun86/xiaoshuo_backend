package com.kun.service.book.dto.admin.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminCategoryPageReqDTO extends com.kun.common.database.page.PageRequest {

    /**
     * 分类名称模糊匹配
     */
    private String name;
    /**
     * 父分类ID
     */
    private Integer channelId;
    /**
     * 状态 (0: 隐藏, 1: 显示)
     */
    private Integer status;


}
