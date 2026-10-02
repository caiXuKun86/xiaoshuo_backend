package com.kun.service.shelf.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadingProgressSyncEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 小说图书 ID
     */
    private Long bookId;
    private Long userId;

    /**
     * 读到的最后章节 ID (与图书最新章节比对红点)
     */
    private Long chapterId;

    /**
     * 最后阅读章节序号 (前台展示: 读至第X章)
     */
    private Integer chapterIndex;

    /**
     * 最后阅读章节名称 (冗余快照，避免频繁联表)
     */
    private String chapterName;

    /**
     * 最后阅读自然段索引 (段落号，跨端精确定位)
     */
    private Integer paragraphIndex;

    /**
     * 章节内阅读百分比 (如 45.50%)
     */
    private BigDecimal readPercent;
    /**
     * 同步时间
     */
    private LocalDateTime syncTime;


}

