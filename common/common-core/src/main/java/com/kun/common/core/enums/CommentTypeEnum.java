package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论类型枚举 (comment_info.comment_type)
 */
@Getter
@AllArgsConstructor
public enum CommentTypeEnum {
    BOOK(1, "整本书评"),
    CHAPTER(2, "章节评论"),
    PARAGRAPH(3, "段落吐槽/本章说"),
    REPLY(4, "回复");

    private final Integer code;
    private final String description;

    public static CommentTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CommentTypeEnum value : CommentTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
