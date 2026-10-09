package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 充值套餐类型枚举 (recharge_sku.sku_type)
 */
@Getter
@AllArgsConstructor
public enum SkuStatusEnum {
    ON_SALE(0, "下架"),
    OFF_SALE(1, "上架");

    private final Integer code;
    private final String description;
}
