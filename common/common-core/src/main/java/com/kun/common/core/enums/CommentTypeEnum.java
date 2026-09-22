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
    PARAGRAPH(3, "段落吐槽/本章说");

    private final Integer code;
    private final String description;
}
