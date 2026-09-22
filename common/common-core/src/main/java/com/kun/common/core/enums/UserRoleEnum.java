package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {
    USER("user", "普通用户"),
    WRITER("writer", "认证作家"),
    ADMIN("admin", "管理员");
    private final String code;
    private final String description;
}
