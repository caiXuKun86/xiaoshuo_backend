package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资产流水变动类型枚举 (user_asset_log)
 */
@Getter
@AllArgsConstructor
public enum AssetChangeTypeEnum {
    CHECKIN_REWARD(1, "每日签到赠送"),
    RECHARGE(2, "充值到账"),
    CHAPTER_EXCHANGE(3, "章节兑换消耗"),
    ADMIN_ADJUST(4, "管理员调整");

    private final Integer code;
    private final String description;
}
