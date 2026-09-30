package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资产流水变动类型枚举 (user_asset_log)
 */
@Getter
@AllArgsConstructor
public enum AuthorStatusTypeEnum {
    FORBIDDEN(0, "封禁"),
    NORMAL(1, "正常");


    private final Integer code;
    private final String description;

    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "";
        }
        for (AuthorStatusTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type.getDescription();
            }
        }
        return "";
    }
}
