package com.kun.service.book.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChapterExchangeRespDTO implements Serializable {

    /**
     * 章节主键 ID (雪花算法)
     */
    private Long chapterId;

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
     * 需要的积分数量
     */
    private Integer costPoints;
    /**
     * 剩余积分数量
     */
    private Integer remainingPoints;

    /**
     * 解锁时间
     */
    private LocalDateTime unlockedTime;


}

