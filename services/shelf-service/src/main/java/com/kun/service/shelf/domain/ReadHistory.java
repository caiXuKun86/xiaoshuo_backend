package com.kun.service.shelf.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kun.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户阅读历史足迹表
 *
 * @TableName read_history
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "read_history")
@Data
public class ReadHistory extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键 ID (自增或雪花算法)
     */
    @TableId(type = IdType.AUTO)
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
     * 读到的最后章节 ID
     */
    private Long lastReadChapterId;

    /**
     * 读到的最后章节序号
     */
    private Integer lastReadChapterIndex;

    /**
     * 读到的章节标题 (冗余快照)
     */
    private String lastReadChapterName;

    /**
     * 读到的段落序号
     */
    private Integer lastReadParagraph;

    /**
     * 本章阅读百分比
     */
    private BigDecimal readPercent;

    /**
     * 最后一次阅读时间
     */
    private LocalDateTime lastReadTime;


}