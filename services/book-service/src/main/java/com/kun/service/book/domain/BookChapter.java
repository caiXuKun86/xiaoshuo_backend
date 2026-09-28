package com.kun.service.book.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kun.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 章节目录表
 * @TableName book_chapter
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="book_chapter")
@Data
public class BookChapter extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 章节主键 ID (雪花算法)
     */
    @TableId
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
     * 段落数量
     */
    private Integer paragraphCount;

    /**
     * 是否付费章节 (0:免费 1:收费)
     */
    private Integer isCharge;

    /**
     * 兑换本章所需积分
     */
    private Integer requiredPoints;

    /**
     * 正文在 OSS 中的存储路径
     */
    private String ossPath;

    /**
     * 发布状态 (0:草稿 1:待审 2:已发布)
     */
    private Integer status;

    /**
     * 发布生效时间 (支持定时发布)
     */
    private LocalDateTime publishTime;


}