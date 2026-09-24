package com.kun.common.database.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kun.common.core.constant.NovelConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        int safePage = (page != null && page > 0) ? page : NovelConstants.DEFAULT_PAGE_NUM;
        int safePageSize = (pageSize != null && pageSize > 0) ? pageSize : NovelConstants.DEFAULT_PAGE_SIZE;
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

    /**
     * 构建空分页结果
     */
    public static <T> PageResult<T> empty(Integer page, Integer pageSize) {
        return of(Collections.emptyList(), page, pageSize, 0L);
    }

    /**
     * 构建默认空分页结果
     */
    public static <T> PageResult<T> empty() {
        return empty(NovelConstants.DEFAULT_PAGE_NUM, NovelConstants.DEFAULT_PAGE_SIZE);
    }

    /* ========== 原 PageUtils 方法集成 ========== */

    /**
     * 将 MyBatis-Plus IPage 直接转换为系统的标准 PageResult
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        if (page == null) {
            return empty();
        }
        return of(page.getRecords(), (int) page.getCurrent(), (int) page.getSize(), page.getTotal());
    }

    /**
     * 将 MyBatis-Plus IPage 中的实体集合通过指定映射函数转换为 VO 后的 PageResult
     */
    public static <S, T> PageResult<T> of(IPage<S> page, Function<S, T> converter) {
        if (page == null || page.getRecords() == null) {
            return empty();
        }
        List<T> voList = page.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList());
        return of(voList, (int) page.getCurrent(), (int) page.getSize(), page.getTotal());
    }

    /**
     * 使用已转换好的记录集合与 IPage 分页元数据封装为 PageResult
     */
    public static <T> PageResult<T> of(IPage<?> page, List<T> targetList) {
        if (page == null) {
            return empty();
        }
        return of(targetList, (int) page.getCurrent(), (int) page.getSize(), page.getTotal());
    }

    /* ========== 语义化别名 from(...) 方法 ========== */

    public static <T> PageResult<T> from(IPage<T> page) {
        return of(page);
    }

    public static <S, T> PageResult<T> from(IPage<S> page, Function<S, T> converter) {
        return of(page, converter);
    }

    public static <T> PageResult<T> from(IPage<?> page, List<T> targetList) {
        return of(page, targetList);
    }
}
