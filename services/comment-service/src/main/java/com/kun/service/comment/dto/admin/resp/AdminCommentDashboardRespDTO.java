package com.kun.service.comment.dto.admin.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentDashboardRespDTO {

    /**
     * 待审核评论数量
     */
    private Long auditingComments;
    /**
     * 总评论数量
     */
    private Long totalComments;
    /**
     * 段评数量
     */
    private Long paragraphComments;


}
