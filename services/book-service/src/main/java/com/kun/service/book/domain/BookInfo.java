package com.kun.service.book.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 图书信息主表
 * @TableName book_info
 */
@TableName(value ="book_info")
@Data
public class BookInfo implements Serializable {
    /**
     * 图书主键 ID (雪花算法)
     */
    @TableId
    private Long id;

    /**
     * 小说书名
     */
    private String bookName;

    /**
     * 作者 ID
     */
    private Long authorId;

    /**
     * 作者笔名 (冗余展示)
     */
    private String authorName;

    /**
     * 分类 ID
     */
    private Integer categoryId;

    /**
     * 分类名称 (冗余展示)
     */
    private String categoryName;

    /**
     * 封面图 OSS 地址
     */
    private String coverUrl;

    /**
     * 作品简介
     */
    private String description;

    /**
     * 标签 (逗号隔开，如"穿越,系统")
     */
    private String tags;

    /**
     * 全书总字数
     */
    private Integer wordCount;

    /**
     * 连载状态 (0:连载中 1:完结)
     */
    private Integer bookStatus;

    /**
     * 最新章节 ID (冗余，方便书架对比红点)
     */
    private Long latestChapterId;

    /**
     * 最新章节名 (冗余)
     */
    private String latestChapterName;

    /**
     * 最新章节发布时间 (冗余)
     */
    private Date latestChapterTime;

    /**
     * 总点击阅读量
     */
    private Long viewCount;

    /**
     * 总书架收藏量
     */
    private Integer collectCount;

    /**
     * 综合评分 (如 9.6 分)
     */
    private BigDecimal score;

    /**
     * 运营状态 (0:草稿 1:上架 2:下架封禁)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}