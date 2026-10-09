package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserVipLevelEnum {
    NORMAL(0, "普通用户"),
    VIP(1, "VIP");

    private final int code;
    private final String desc;



    public static UserVipLevelEnum of(Integer code) {
        for (UserVipLevelEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return NORMAL; // 默认普通用户
    }
}