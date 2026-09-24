package com.kun.common.database.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kun.common.database.page.PageRequest;
import com.kun.common.database.page.PageResult;

import java.util.List;
import java.util.function.Function;

/**
 * 分页转换工具类
 * 连接 MyBatis-Plus 的 IPage 与全站统一规范的 PageResult
 *
 * @deprecated 建议直接使用 {@link PageRequest#toPage()}、{@link PageRequest#buildPage(Integer, Integer)}
 *             以及 {@link PageResult#of(IPage)} 相关方法。
 */
@Deprecated
public final class PageUtils {

    private PageUtils() {
    }

    /**
     * 构建 MyBatis-Plus 分页对象 (自动处理默认页码与最大条数保护)
     */
    public static <T> Page<T> buildPage(Integer pageNum, Integer pageSize) {
        return PageRequest.buildPage(pageNum, pageSize);
    }

    /**
     * 将 MyBatis-Plus IPage 直接转换为系统的标准 PageResult
     */
    public static <T> PageResult<T> toPageResult(IPage<T> page) {
        return PageResult.of(page);
    }

    /**
     * 将 MyBatis-Plus IPage 中的实体集合通过指定映射函数转换为 VO 后的 PageResult
     */
    public static <T, R> PageResult<R> toPageResult(IPage<T> page, Function<T, R> converter) {
        return PageResult.of(page, converter);
    }

    /**
     * 使用已转换好的记录集合封装为 PageResult
     */
    public static <R> PageResult<R> toPageResult(IPage<?> page, List<R> targetList) {
        return PageResult.of(page, targetList);
    }
}
