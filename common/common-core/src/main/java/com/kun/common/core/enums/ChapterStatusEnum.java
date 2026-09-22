package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 章节发布状态枚举 (book_chapter.status)
 */
@Getter
@AllArgsConstructor
public enum ChapterStatusEnum {
    DRAFT(0, "草稿"),
    AUDITING(1, "待审核"),
    PUBLISHED(2, "已发布");

    private final Integer code;
    private final String description;
}
