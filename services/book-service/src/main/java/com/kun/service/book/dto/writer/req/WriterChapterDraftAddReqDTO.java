package com.kun.service.book.dto.writer.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WriterChapterDraftAddReqDTO implements Serializable {


    /**
     * 所属图书 ID
     */
    private Long bookId;

    /**
     * 章节顺序号 (第几章: 1, 2, 3...)
     */
    private Integer chapterIndex;

    /**
     * 章节标题 (如"第1章 陨落的天才")
     */
    private String chapterName;

    /**
     * 内容
     */
    private String content;
    /**
     * 是否付费章节 (0:免费 1:收费)
     */
    private Integer isCharge;
    /**
     * 需要的积分数量
     */
    private Integer requiredPoints;


}

