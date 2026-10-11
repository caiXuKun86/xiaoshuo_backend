package com.kun.service.book.dto.writer.resp;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class WriterChapterDraftAddRespDTO implements Serializable {


    /**
     * 所属图书 ID
     */
    private Long bookId;

    /**
     * 章节Id
     */
    private Long chapterId;
    /**
     * 章节顺序号 (第几章: 1, 2, 3...)
     */
    private Integer chapterIndex;

    /**
     * 章节标题 (如"第1章 陨落的天才")
     */
    private String chapterName;

    /**
     * 字数
     */
    private Integer wordCount;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}

