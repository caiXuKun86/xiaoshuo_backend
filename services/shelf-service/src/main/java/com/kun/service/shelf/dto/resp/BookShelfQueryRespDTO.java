package com.kun.service.shelf.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookShelfQueryRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 小说图书 ID
     */
    private Long bookId;

    /**
     * 小说名称
     */
    private String bookName;

    /**
     * 作者名称
     */
    private String authorName;

    /**
     * 最新章节 ID (与图书最新章节比对红点)
     */
    private Long latestChapterId;

    /**
     * 最新章节名称 (冗余快照，避免频繁联表)
     */
    private String latestChapterName;
    /**
     * 最新章节更新时间 (冗余快照，避免频繁联表)
     */
    private String latestChapterTime;


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

