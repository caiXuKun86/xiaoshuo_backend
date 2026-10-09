package com.kun.service.pay.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.core.enums.SkuStatusEnum;
import com.kun.common.core.enums.SkuTypeEnum;
import com.kun.service.pay.domain.RechargeSku;
import com.kun.service.pay.dto.resp.SkuListQueryRespDTO;
import com.kun.service.pay.mapper.RechargeSkuMapper;
import com.kun.service.pay.service.RechargeSkuService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lenovo
 * @description 针对表【recharge_sku(充值套餐与商品SKU配置表)】的数据库操作Service实现
 * @createDate 2026-10-09 08:48:04
 */
@Service
public class RechargeSkuServiceImpl extends ServiceImpl<RechargeSkuMapper, RechargeSku> implements RechargeSkuService {

    @Override
    public SkuListQueryRespDTO querySkuList() {
        List<RechargeSku> list = this.lambdaQuery()
                .eq(RechargeSku::getStatus, SkuStatusEnum.OFF_SALE)
                .orderByAsc(RechargeSku::getSort)
                .list();
        Map<Integer, List<RechargeSku>> collect = list.stream()
                .collect(Collectors.groupingBy(
                        RechargeSku::getSkuType,
                        LinkedHashMap::new,       // 指定使用 LinkedHashMap 保留遇到顺序
                        Collectors.toList()
                ));
        SkuListQueryRespDTO dto = new SkuListQueryRespDTO();
        List<RechargeSku> PointSkus = collect.get(SkuTypeEnum.POINT_PKG.getCode());
        dto.setPointPackages(PointSkus.stream().map(pointSku ->
                BeanUtil.copyProperties(pointSku, SkuListQueryRespDTO.PointDTO.class)
        ).toList());

        List<RechargeSku> VipSkus = collect.get(SkuTypeEnum.VIP_CARD.getCode());
        dto.setVipPackages(VipSkus.stream().map(vipSku ->
                BeanUtil.copyProperties(vipSku, SkuListQueryRespDTO.VipDTO.class)
        ).toList());
        return dto;

    }
}




