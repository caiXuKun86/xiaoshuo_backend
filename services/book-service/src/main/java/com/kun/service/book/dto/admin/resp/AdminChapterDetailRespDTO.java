package com.kun.service.book.dto.admin.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminChapterDetailRespDTO implements Serializable {

    /**
     * 章节主键 ID (雪花算法)
     */
    private Long id;

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
     * 本章字数 (如 3200)
     */
    private Integer wordCount;

    /**
     * 是否付费章节 (0:免费 1:收费)
     */
    private Integer isCharge;
    /**
     * 需要的积分数量
     */
    private Integer requiredPoints;

    /**
     * Oss路径
     */
    private String ossPath;

    /**
     * 正文在 OSS 中的存储路径
     */
    private String content;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    /**
     * 更新时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}

