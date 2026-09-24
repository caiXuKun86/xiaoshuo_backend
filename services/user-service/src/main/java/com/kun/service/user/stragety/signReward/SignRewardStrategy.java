package com.kun.service.user.stragety.signReward;

import com.kun.common.core.enums.VipLevelEnum;

public interface SignRewardStrategy {

    /**
     * 当前策略支持的 VIP 等级
     */
    VipLevelEnum getVipLevel();


    int calculatePoints();
}