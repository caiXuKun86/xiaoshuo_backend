package com.kun.service.comment.dto.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ParagraphCommentPageReqDTO extends com.kun.common.database.page.PageRequest {


    /**
     * 所属章节 ID
     */
    private Long chapterId;

    /**
     * 自然段序号 (第几段)
     */
    private Integer paragraphIndex;


}
