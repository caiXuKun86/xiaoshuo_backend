package com.kun.service.user.stragety.signReward;

import com.kun.common.core.enums.VipLevelEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SignRewardStrategyFactory {

    private final Map<VipLevelEnum, SignRewardStrategy> strategyMap = new ConcurrentHashMap<>();

    // Spring 自动把所有的策略实现类注入进来
    public SignRewardStrategyFactory(List<SignRewardStrategy> strategies) {
        for (SignRewardStrategy strategy : strategies) {
            strategyMap.put(strategy.getVipLevel(), strategy);
        }
    }

    /**
     * 根据 VIP 等级获取对应的计算策略
     */
    public SignRewardStrategy getStrategy(VipLevelEnum vipLevel) {
        SignRewardStrategy strategy = strategyMap.get(vipLevel);
        if (strategy == null) {
            // 兜底策略：找不到对应策略时默认使用普通用户策略
            return strategyMap.get(VipLevelEnum.NORMAL);
        }
        return strategy;
    }
}