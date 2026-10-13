package com.kun.service.comment.dto.admin.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentInfoBatchPatchStatusRespDTO {

    /**
     * id集合
     */
    private Integer affectedRows;


}
