package com.kun.service.comment.dto.admin.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentInfoBatchPatchStatusReqDTO {

    /**
     * id集合
     */
    private List<Long> commentIds;
    /**
     * 状态
     */
    private Integer status;


}
