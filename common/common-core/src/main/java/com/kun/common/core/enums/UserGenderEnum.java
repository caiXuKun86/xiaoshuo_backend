package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 用户性别枚举
 */
@Getter
@AllArgsConstructor
public enum UserGenderEnum {
    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");

    private final Integer code;
    private final String description;


    public static UserGenderEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserGenderEnum value : values()) {
            if (Objects.equals(value.getCode(), code)) {
                return value;
            }
        }
        return null;
    }

}
