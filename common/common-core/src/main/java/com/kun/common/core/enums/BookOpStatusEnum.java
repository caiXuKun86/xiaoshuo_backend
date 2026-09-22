package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 图书运营状态枚举 (book_info.status)
 */
@Getter
@AllArgsConstructor
public enum BookOpStatusEnum {
    DRAFT(0, "草稿"),
    ON_SHELF(1, "正常上架"),
    BANNED(2, "下架封禁");

    private final Integer code;
    private final String description;
}
