package com.kun.service.book.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookChapterQueryRespDTO implements Serializable {

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
     * 是否需要登录
     */
    private Boolean needLogin;
    /**
     * 兑换本章所需积分
     */
    private Boolean isLocked;

    /**
     * 正文在 OSS 中的存储路径
     */
    private String content;

    /**
     * 段落数量
     */
    private Integer paragraphCount;
    /**
     * 上一章ID
     */
    private Long prevChapterId;
    /**
     * 下一章ID
     */
    private Long nextChapterId;


}

