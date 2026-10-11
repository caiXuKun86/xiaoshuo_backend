package com.kun.service.book.dto.writer.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // 字段为 null 时不参与 JSON 序列化
public class WriterBookStatsRespDTO implements Serializable {

    /**
     * 作品总数
     */
    private Integer totalBooks;
    /**
     * 发布章节总数
     */
    private Integer publishedChapters;
    /**
     * 作品总字数
     */
    private Long totalWordCount;
    /**
     * 收藏总数
     */
    private Long totalCollectCount;
    /**
     * 待审核章节数
     */
    private Integer pendingAuditChapters;


}
