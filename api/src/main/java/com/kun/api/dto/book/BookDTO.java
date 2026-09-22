package com.kun.api.dto.book;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 图书跨服务传输基础元数据 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "跨服务图书基础元数据传输对象")
public class BookDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "图书主键 ID")
    private Long id;

    @Schema(description = "小说书名")
    private String bookName;

    @Schema(description = "作者 ID")
    private Long authorId;

    @Schema(description = "作者笔名")
    private String authorName;

    @Schema(description = "分类 ID")
    private Integer categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "封面图 OSS 地址")
    private String coverUrl;

    @Schema(description = "作品字数")
    private Integer wordCount;

    @Schema(description = "连载状态 (0:连载中 1:已完结)")
    private Integer bookStatus;

    @Schema(description = "最新章节 ID")
    private Long latestChapterId;

    @Schema(description = "最新章节名")
    private String latestChapterName;

    @Schema(description = "综合评分")
    private BigDecimal score;

    @Schema(description = "运营状态 (0:草稿 1:上架 2:下架封禁)")
    private Integer status;
}
