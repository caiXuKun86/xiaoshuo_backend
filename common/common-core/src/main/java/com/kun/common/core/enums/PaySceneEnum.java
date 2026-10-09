package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付场景 (pay_order.pay_channel)
 */
@Getter
@AllArgsConstructor
public enum PaySceneEnum {
    PAGE(0, "电脑网站支付"),
    NATIVE(1, "PC扫码/当面付");

    private final Integer code;
    private final String description;
}
