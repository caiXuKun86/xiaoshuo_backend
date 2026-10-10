package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 图书运营状态枚举 (book_info.status)
 */
@Getter
@AllArgsConstructor
public enum BookOpStatusEnum {
    BANNED(0, "下架封禁"),
    ON_SHELF(1, "正常上架");

    private final Integer code;
    private final String description;

    public static BookOpStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BookOpStatusEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
