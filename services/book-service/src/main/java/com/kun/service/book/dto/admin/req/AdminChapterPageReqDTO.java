package com.kun.service.book.dto.admin.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminChapterPageReqDTO extends com.kun.common.database.page.PageRequest {


    /**
     * 状态
     */
    private Integer status;
    /**
     * 是否收费
     */
    private Integer isCharge;
    /**
     * 所属图书 ID
     */
    private Long bookId;

    /**
     * 章节标题 (如"第1章 陨落的天才")
     */
    private String chapterName;


}
