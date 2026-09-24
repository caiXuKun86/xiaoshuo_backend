package com.kun.service.user.stragety.signReward;

import com.kun.common.core.enums.VipLevelEnum;
import org.springframework.stereotype.Component;

@Component
public class NormalSignRewardStrategy implements SignRewardStrategy {

    @Override
    public VipLevelEnum getVipLevel() {
        return VipLevelEnum.NORMAL;
    }

    @Override
    public int calculatePoints() {
        return 1;
    }
}