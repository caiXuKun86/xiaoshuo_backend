package com.kun.api.dto.book;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 章节跨服务传输基础元数据 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "跨服务章节基础信息传输对象")
public class ChapterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "章节主键 ID")
    private Long id;

    @Schema(description = "所属图书 ID")
    private Long bookId;

    @Schema(description = "章节顺序号 (第几章)")
    private Integer chapterIndex;

    @Schema(description = "章节标题")
    private String chapterName;

    @Schema(description = "本章字数")
    private Integer wordCount;

    @Schema(description = "是否付费章节 (0:免费 1:收费)")
    private Integer isCharge;

    @Schema(description = "兑换所需积分")
    private Integer requiredPoints;

    @Schema(description = "正文在 OSS 的存储键路径")
    private String ossPath;

    @Schema(description = "发布状态 (0:草稿 1:待审 2:已发布)")
    private Integer status;
}
