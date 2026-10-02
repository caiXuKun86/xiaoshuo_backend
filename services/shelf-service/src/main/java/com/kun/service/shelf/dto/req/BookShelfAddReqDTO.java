package com.kun.service.shelf.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookShelfAddReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


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


}

