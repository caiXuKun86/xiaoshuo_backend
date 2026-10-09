package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 图书连载状态枚举 (book_info.book_status)
 */
@Getter
@AllArgsConstructor
public enum BookStatusEnum {
    SERIALIZING(0, "连载中"),
    FINISHED(1, "已完结");

    private final Integer code;
    private final String description;

    public static BookStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BookStatusEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
