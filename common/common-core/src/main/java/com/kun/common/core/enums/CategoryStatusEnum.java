package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论类型枚举 (comment_info.comment_type)
 */
@Getter
@AllArgsConstructor
public enum CategoryStatusEnum {
    HIDDEN(0, "隐藏"),
    SHOW(1, "显示");


    private final Integer code;
    private final String description;

    public static CategoryStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CategoryStatusEnum value : CategoryStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
