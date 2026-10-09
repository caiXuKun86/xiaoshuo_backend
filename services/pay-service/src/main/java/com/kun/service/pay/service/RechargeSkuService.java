package com.kun.service.pay.service;

import com.kun.service.pay.domain.RechargeSku;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.service.pay.dto.resp.SkuListQueryRespDTO;

/**
* @author Lenovo
* @description 针对表【recharge_sku(充值套餐与商品SKU配置表)】的数据库操作Service
* @createDate 2026-10-09 08:48:04
*/
public interface RechargeSkuService extends IService<RechargeSku> {

    SkuListQueryRespDTO querySkuList();
}
