package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论类型枚举 (comment_info.comment_type)
 */
@Getter
@AllArgsConstructor
public enum BookRankTypeEnum {
    NEW_BOOK(1, "新书榜"),
    COMPLETED(2, "完本榜");


    private final Integer code;
    private final String description;

    public static BookRankTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BookRankTypeEnum value : BookRankTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
