package com.kun.service.book.dto.admin.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 字段为 null 时不参与 JSON 序列化
public class AdminBookInfoDetailRespDTO implements Serializable {

    /**
     * 分类主键 ID
     */
    private Long id;

    /**
     * 书籍名称
     */
    private String bookName;

    /**
     * 作者Id
     */
    private Long authorId;

    /**
     * 作者笔名
     */
    private String authorName;

    /**
     * 分类Id
     */
    private Long categoryId;
    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 作品封面
     */
    private String coverUrl;
    /**
     * 简述
     */
    private String description;

    /**
     * 作品标签
     */
    private List<String> tags;

    /**
     * 作品字数
     */
    private Integer wordCount;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 连载状态 (0:连载中 1:完结)
     */
    private Integer bookStatus;


    /**
     * 综合评分 (如 9.6 分)
     */
    private BigDecimal score;
    /**
     * 收藏数量
     */
    private Integer collectCount;
    /**
     * 最新章节名Id
     */
    private Long latestChapterId;

    /**
     * 最新章节名
     */
    private String latestChapterName;

    /**
     * 最新章节发布时间
     */
    private Date latestChapterTime;
    /**
     * 创建时间 (新增时自动填充)
     */

    private LocalDateTime createTime;

    /**
     * 更新时间 (新增与修改时自动填充)
     */
    private LocalDateTime updateTime;


}
