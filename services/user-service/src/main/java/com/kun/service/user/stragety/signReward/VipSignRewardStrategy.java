package com.kun.service.user.stragety.signReward;

import com.kun.common.core.enums.VipLevelEnum;
import org.springframework.stereotype.Component;

@Component
public class VipSignRewardStrategy implements SignRewardStrategy {

    @Override
    public VipLevelEnum getVipLevel() {
        return VipLevelEnum.VIP;
    }

    @Override
    public int calculatePoints() {
        return 1;

    }
}