package com.kun.common.core.enums;

public enum VipLevelEnum {
    NORMAL(0, "普通用户"),
    VIP(1, "VIP");

    private final int code;
    private final String desc;

    VipLevelEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static VipLevelEnum of(Integer code) {
        for (VipLevelEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return NORMAL; // 默认普通用户
    }
}