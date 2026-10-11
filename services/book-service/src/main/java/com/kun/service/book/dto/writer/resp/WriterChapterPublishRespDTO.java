package com.kun.service.book.dto.writer.resp;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class WriterChapterPublishRespDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键Id
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
     * 章节总字数
     */
    private Integer wordCount;

    /**
     * 是否收费
     */
    private Integer isCharge;

    /**
     * 消耗积分
     */
    private Integer requiredPoints;


    /**
     * 章节状态
     */
    private Integer status;
    /**
     * 发布时间
     */
    private LocalDateTime createTime;


}

