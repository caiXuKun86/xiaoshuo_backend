package com.kun.service.book.dto.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 字段为 null 时不参与 JSON 序列化
public class BookPageRespDTO implements Serializable {

    /**
     * 分类主键 ID
     */
    private Long id;

    /**
     * 书籍名称
     */
    private String bookName;

    /**
     * 作者笔名
     */
    private String authorName;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 作品封面
     */
    private String coverUrl;

    /**
     * 作品描述
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
     * 连载状态 (0:连载中 1:完结)
     */
    private Integer bookStatus;


    /**
     * 综合评分 (如 9.6 分)
     */
    private BigDecimal score;

    /**
     * 最新章节名
     */
    private String latestChapterName;

    /**
     * 最新章节发布时间
     */
    private Date latestChapterTime;


}
