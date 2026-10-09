package com.kun.service.pay.controller;

import com.kun.common.core.result.Result;
import com.kun.service.pay.dto.resp.SkuListQueryRespDTO;
import com.kun.service.pay.service.RechargeSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/sku")
@RequiredArgsConstructor
public class RechargeSkuController {
    private final RechargeSkuService rechargeSkuService;

    @GetMapping("/list")
    public Result<SkuListQueryRespDTO> querySkuList() {
        SkuListQueryRespDTO skuListQueryRespDTO = rechargeSkuService.querySkuList();
        return Result.success(skuListQueryRespDTO);
    }


}
