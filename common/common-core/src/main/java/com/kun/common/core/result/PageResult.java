package com.kun.common.core.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 全局统一分页列表响应实体
 *
 * @param <T> 数据记录泛型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一分页列表结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页数据记录列表")
    private List<T> list;

    @Schema(description = "当前页码 (从 1 开始)", example = "1")
    private Integer page;

    @Schema(description = "每页数据条数", example = "20")
    private Integer pageSize;

    @Schema(description = "符合查询条件的总记录数", example = "1280")
    private Long total;

    @Schema(description = "计算得出的总页数", example = "64")
    private Integer totalPages;

    @Schema(description = "是否还有下一页 (供瀑布流/分页组件判定)", example = "true")
    private Boolean hasNext;

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(List<T> list, Integer page, Integer pageSize, Long total) {
        List<T> safeList = list != null ? list : Collections.emptyList();
        long safeTotal = total != null ? total : 0L;
        int safePage = (page != null && page > 0) ? page : 1;
        int safePageSize = (pageSize != null && pageSize > 0) ? pageSize : 20;
        int calculatedPages = safePageSize > 0 ? (int) Math.ceil((double) safeTotal / safePageSize) : 0;
        boolean next = safePage < calculatedPages;

        return PageResult.<T>builder()
                .list(safeList)
                .page(safePage)
                .pageSize(safePageSize)
                .total(safeTotal)
                .totalPages(calculatedPages)
                .hasNext(next)
                .build();
    }
}
