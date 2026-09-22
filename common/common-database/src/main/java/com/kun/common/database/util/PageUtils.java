package com.kun.common.database.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kun.common.core.constant.NovelConstants;
import com.kun.common.core.result.PageResult;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页转换工具类
 * 连接 MyBatis-Plus 的 IPage 与全站统一规范的 PageResult
 */
public final class PageUtils {

    private PageUtils() {
    }

    /**
     * 构建 MyBatis-Plus 分页对象 (自动处理默认页码与最大条数保护)
     */
    public static <T> Page<T> buildPage(Integer pageNum, Integer pageSize) {
        long current = (pageNum != null && pageNum > 0) ? pageNum : NovelConstants.DEFAULT_PAGE_NUM;
        long size = (pageSize != null && pageSize > 0) ? pageSize : NovelConstants.DEFAULT_PAGE_SIZE;
        if (size > NovelConstants.MAX_PAGE_SIZE) {
            size = NovelConstants.MAX_PAGE_SIZE;
        }
        return new Page<>(current, size);
    }

    /**
     * 将 MyBatis-Plus IPage 直接转换为系统的标准 PageResult
     */
    public static <T> PageResult<T> toPageResult(IPage<T> page) {
        if (page == null) {
            return PageResult.of(Collections.emptyList(), 1, NovelConstants.DEFAULT_PAGE_SIZE, 0L);
        }
        return PageResult.of(page.getRecords(), (int) page.getCurrent(), (int) page.getSize(), page.getTotal());
    }

    /**
     * 将 MyBatis-Plus IPage 中的实体集合通过指定映射函数转换为 VO 后的 PageResult
     */
    public static <T, R> PageResult<R> toPageResult(IPage<T> page, Function<T, R> converter) {
        if (page == null || page.getRecords() == null) {
            return PageResult.of(Collections.emptyList(), 1, NovelConstants.DEFAULT_PAGE_SIZE, 0L);
        }
        List<R> voList = page.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList());
        return PageResult.of(voList, (int) page.getCurrent(), (int) page.getSize(), page.getTotal());
    }

    /**
     * 使用已转换好的记录集合封装为 PageResult
     */
    public static <R> PageResult<R> toPageResult(IPage<?> page, List<R> targetList) {
        if (page == null) {
            return PageResult.of(Collections.emptyList(), 1, NovelConstants.DEFAULT_PAGE_SIZE, 0L);
        }
        return PageResult.of(targetList, (int) page.getCurrent(), (int) page.getSize(), page.getTotal());
    }
}
