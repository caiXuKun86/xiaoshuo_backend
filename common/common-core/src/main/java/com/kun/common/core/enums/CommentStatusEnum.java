package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论状态枚举 (comment_info.status)
 */
@Getter
@AllArgsConstructor
public enum CommentStatusEnum {
    AUDITING(0, "待审核"),
    NORMAL(1, "正常展示"),
    USER_DELETED(2, "用户自删"),
    BANNED(3, "违规下架");

    private final Integer code;
    private final String description;
}
