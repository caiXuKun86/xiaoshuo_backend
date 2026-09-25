package com.kun.service.user.stragety.signReward;

import cn.hutool.core.util.RandomUtil;
import com.kun.common.core.enums.VipLevelEnum;
import org.springframework.stereotype.Component;

@Component
public class NormalSignRewardStrategy implements SignRewardStrategy {

    @Override
    public VipLevelEnum getVipLevel() {
        return VipLevelEnum.NORMAL;
    }

    @Override
    //5.75
    public int calculatePoints() {
        int i = RandomUtil.randomInt(1, 10);
        if(i<=9){
            return RandomUtil.randomInt(3,9);
        } else {
            return RandomUtil.randomInt(10,15);
        }
    }
}