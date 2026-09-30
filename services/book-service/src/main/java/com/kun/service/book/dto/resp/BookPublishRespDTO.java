package com.kun.service.book.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookPublishRespDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 图书主键 ID (雪花算法)
     */
    private Long bookId;

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
     * 分类 ID
     */
    private Integer channelId;

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
    private List<String> tags;

    /**
     * 全书总字数
     */
    private Integer wordCount;

    /**
     * 连载状态 (0:连载中 1:完结)
     */
    private Integer bookStatus;

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
}

