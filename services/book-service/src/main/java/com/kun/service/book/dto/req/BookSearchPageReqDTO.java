package com.kun.service.book.dto.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookSearchPageReqDTO extends com.kun.common.database.page.PageRequest {

    /**
     * 关键词
     */
    private String keyword;


}
