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
public class ChapterPublishRespDTO implements Serializable {


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
     * 是否收费
     */
    private Integer isCharge;

    /**
     * 消耗积分
     */
    private Integer requiredPoints;
    /**
     * 封面图 OSS 地址
     */
    private String ossPath;

    /**
     * 书籍状态
     */
    private Integer status;
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;


}

