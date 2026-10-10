package com.kun.service.book.dto.admin.req;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 字段为 null 时不参与 JSON 序列化
public class AdminChapterBatchSetChargeReqDTO  {

    /**
     * 所属图书 ID
     */
    private Long bookId;


    private Integer startChapterIndex;
    private Integer endChapterIndex;
    private Integer requiredPoints;


}
