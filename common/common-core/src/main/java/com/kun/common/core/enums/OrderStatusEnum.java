package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易订单状态枚举 (pay_order.order_status)
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    PENDING(0, "待支付"),
    SUCCESS(1, "支付成功"),
    CANCELLED(2, "已取消"),
    FAILED(3, "支付失败"),
    REFUNDED(4, "已退款");

    private final Integer code;
    private final String description;
}
