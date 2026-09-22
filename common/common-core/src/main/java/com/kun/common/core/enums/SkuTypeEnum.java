package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 充值套餐类型枚举 (recharge_sku.sku_type)
 */
@Getter
@AllArgsConstructor
public enum SkuTypeEnum {
    POINT_PKG(1, "积分充值包"),
    VIP_CARD(2, "VIP会员卡");

    private final Integer code;
    private final String description;
}
