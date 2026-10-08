package com.kun.service.book.dto.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 字段为 null 时不参与 JSON 序列化
public class BookRankHomeSummaryRespDTO implements Serializable {

    /**
     * 新书榜
     */
    private List<BookRankPageRespDTO> newBooks;
    /**
     * 完本榜
     */
    private List<BookRankPageRespDTO> completedBooks;




}
