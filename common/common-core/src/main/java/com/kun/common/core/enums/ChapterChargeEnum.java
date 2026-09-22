package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 章节收费状态枚举 (book_chapter.is_charge)
 */
@Getter
@AllArgsConstructor
public enum ChapterChargeEnum {
    FREE(0, "免费章节"),
    CHARGE(1, "付费章节");

    private final Integer code;
    private final String description;
}
