package com.kun.service.book.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChapterUpdateRespDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 图书主键 ID (雪花算法)
     */
    private Long chapterId;

    /**
     * 图书Id
     */
    private Long bookId;
    /**
     * 序号
     */
    private Integer chapterIndex;


    /**
     * 全书总字数
     */
    private Integer wordCount;


    /**
     * 修改时间
     */
    private LocalDateTime updateTime;


}

