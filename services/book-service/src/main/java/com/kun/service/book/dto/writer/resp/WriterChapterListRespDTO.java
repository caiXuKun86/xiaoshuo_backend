package com.kun.service.book.dto.writer.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WriterChapterListRespDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 章节主键 ID (雪花算法)
     */
    private Long id;

    /**
     * 章节顺序号 (第几章: 1, 2, 3...)
     */
    private Integer chapterIndex;

    /**
     * 章节标题 (如"第1章 陨落的天才")
     */
    private String chapterName;

    /**
     * 本章字数 (如 3200)
     */
    private Integer wordCount;

    /**
     * 是否付费章节 (0:免费 1:收费)
     */
    private Integer isCharge;

    /**
     * 兑换本章所需积分
     */
    private Integer requiredPoints;


    /**
     * 发布生效时间 (支持定时发布)
     */
    private LocalDateTime publishTime;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}

