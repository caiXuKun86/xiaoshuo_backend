package com.kun.service.shelf.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kun.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户书架表
 *
 * @TableName bookshelf
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "bookshelf")
@Data
public class Bookshelf extends BaseEntity {


    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键 ID (自增或雪花算法)
     */
    @TableId
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 小说图书 ID
     */
    private Long bookId;

    /**
     * 读到的最后章节 ID (与图书最新章节比对红点)
     */
    private Long lastReadChapterId;

    /**
     * 最后阅读章节序号 (前台展示: 读至第X章)
     */
    private Integer lastReadChapterIndex;

    /**
     * 最后阅读章节名称 (冗余快照，避免频繁联表)
     */
    private String lastReadChapterName;

    /**
     * 最后阅读自然段索引 (段落号，跨端精确定位)
     */
    private Integer lastReadParagraph;

    /**
     * 章节内阅读百分比 (如 45.50%)
     */
    private BigDecimal readPercent;

    /**
     * 最后阅读时间 (排序字段)
     */
    private LocalDateTime lastReadTime;


}