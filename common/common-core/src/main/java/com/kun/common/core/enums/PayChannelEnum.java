package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付渠道枚举 (pay_order.pay_channel)
 */
@Getter
@AllArgsConstructor
public enum PayChannelEnum {
    NONE(0, "支付宝"),
    WECHAT(1, "微信支付");

    private final Integer code;
    private final String description;

    public static PayChannelEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayChannelEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
