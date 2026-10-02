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

    private Long id;

    private String bookName;

    private Long authorId;

    private String authorName;

    private Integer categoryId;

    private String categoryName;

    private String coverUrl;

    private Integer wordCount;

    private Integer bookStatus;

    private Long latestChapterId;

    private String latestChapterName;
    private String latestChapterTime;

    private BigDecimal score;

    private Integer status;
}
