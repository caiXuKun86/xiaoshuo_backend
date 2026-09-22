package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付渠道枚举 (pay_order.pay_channel)
 */
@Getter
@AllArgsConstructor
public enum PayChannelEnum {
    NONE(0, "未选择"),
    WECHAT(1, "微信支付"),
    ALIPAY(2, "支付宝"),
    APPLE_IAP(3, "苹果应用内购买(IAP)");

    private final Integer code;
    private final String description;
}
