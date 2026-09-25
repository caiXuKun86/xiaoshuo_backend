package com.kun.service.user.stragety.signReward;

import cn.hutool.core.util.RandomUtil;
import com.kun.common.core.enums.VipLevelEnum;
import org.springframework.stereotype.Component;

@Component
public class VipSignRewardStrategy implements SignRewardStrategy {

    @Override
    public VipLevelEnum getVipLevel() {
        return VipLevelEnum.VIP;
    }

    @Override
    //30
    public int calculatePoints() {
        int i = RandomUtil.randomInt(1, 10);
        if(i<=6){
            return RandomUtil.randomInt(20,30);
        } else if(i<=9) {
            return RandomUtil.randomInt(30,40);
        } else {
            return RandomUtil.randomInt(40,50);
        }

    }
}