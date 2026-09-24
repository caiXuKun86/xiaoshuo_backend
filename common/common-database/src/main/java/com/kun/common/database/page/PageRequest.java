package com.kun.common.database.page;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kun.common.core.constant.NovelConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * 通用分页请求基础类
 */
@Data
@Schema(description = "通用分页请求参数")
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 默认分页参数与安全上限（引用系统常量）
     */
    public static final int DEFAULT_PAGE_NUM = NovelConstants.DEFAULT_PAGE_NUM;
    public static final int DEFAULT_PAGE_SIZE = NovelConstants.DEFAULT_PAGE_SIZE;
    public static final int MAX_PAGE_SIZE = NovelConstants.MAX_PAGE_SIZE;

    @Schema(description = "当前页码（从 1 开始）", example = "1")
    private Integer pageNum = DEFAULT_PAGE_NUM;

    @Schema(description = "每页展示条数（最大不超过 100）", example = "20")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    @Schema(description = "排序字段（数据库列名或实体字段名）", example = "create_time")
    private String sortField;

    @Schema(description = "是否升序：true-升序(ASC)，false-降序(DESC)", example = "false")
    private Boolean isAsc = false;

    /* ========== 容错与防御式 Getter（防止前端传 null 导致 NPE） ========== */

    public Integer getPageNum() {
        if (this.pageNum == null || this.pageNum < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return this.pageNum;
    }

    public Integer getPageSize() {
        if (this.pageSize == null || this.pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        // 强制上限保护，防止通过非校验路径传大数值导致 OOM
        if (this.pageSize > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        return this.pageSize;
    }

    /**
     * 计算原生 SQL 分页的 offset（偏移量）
     * 对应 SQL: LIMIT #{offset}, #{pageSize}
     */
    public long getOffset() {
        return (long) (getPageNum() - 1) * getPageSize();
    }

    /* ========== 原 PageUtils 方法集成 ========== */

    /**
     * 将当前请求对象直接转换为 MyBatis-Plus 的 Page 分页对象（支持自动附加排序规则）
     */
    public <T> Page<T> toPage() {
        Page<T> page = new Page<>(getPageNum(), getPageSize());
        if (StringUtils.hasText(this.sortField)) {
            // 防 SQL 注入校验（只允许大小写字母、数字和下划线）
            if (this.sortField.matches("^[a-zA-Z0-9_]+$")) {
                page.addOrder(Boolean.TRUE.equals(this.isAsc)
                        ? OrderItem.asc(this.sortField)
                        : OrderItem.desc(this.sortField));
            }
        }
        return page;
    }

    /**
     * 兼容方法别名：同 toPage()
     */
    public <T> Page<T> toMpPage() {
        return toPage();
    }

    /**
     * 静态快捷方法：构建 MyBatis-Plus 分页对象 (自动处理默认页码与最大条数保护)
     * (原 PageUtils.buildPage 方法)
     */
    public static <T> Page<T> buildPage(Integer pageNum, Integer pageSize) {
        long current = (pageNum != null && pageNum > 0) ? pageNum : DEFAULT_PAGE_NUM;
        long size = (pageSize != null && pageSize > 0) ? pageSize : DEFAULT_PAGE_SIZE;
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        return new Page<>(current, size);
    }
}