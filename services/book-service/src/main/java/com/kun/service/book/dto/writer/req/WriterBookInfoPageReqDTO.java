package com.kun.service.book.dto.writer.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WriterBookInfoPageReqDTO extends com.kun.common.database.page.PageRequest {

    /**
     * 状态
     */
    private Integer status;
    /**
     * 连载状态
     */
    private Integer bookStatus;

    /**
     * 关键词
     */
    private String keyword;


}
