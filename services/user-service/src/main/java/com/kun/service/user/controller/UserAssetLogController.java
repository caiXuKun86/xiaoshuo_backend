package com.kun.service.user.controller;

import com.kun.common.database.page.PageResult;
import com.kun.common.core.result.Result;
import com.kun.service.user.dto.req.AssetLogsPageReqDTO;
import com.kun.service.user.dto.resp.AssetLogPageRespDTO;
import com.kun.service.user.dto.resp.CheckinStatusRespDTO;
import com.kun.service.user.dto.resp.UserCheckinRespDTO;
import com.kun.service.user.dto.resp.UserWalletQueryDTO;
import com.kun.service.user.service.UserAssetLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户资产与打卡服务", description = "提供签到打卡、资产钱包查询及积分变动流水明细接口")
@RestController
@RequestMapping("/user/asset")
@RequiredArgsConstructor
public class UserAssetLogController {

    private final UserAssetLogService userAssetLogService;

    @Operation(summary = "用户每日签到打卡")
    @PostMapping("/checkin")
    public Result<UserCheckinRespDTO> userCheckin() {
        UserCheckinRespDTO userCheckinRespDTO = userAssetLogService.userCheckin();
        return Result.success(userCheckinRespDTO);
    }

    @Operation(summary = "查询用户本月签到状态及签到日历")
    @GetMapping("/checkin/status")
    public Result<CheckinStatusRespDTO> queryCheckinStatus() {
        CheckinStatusRespDTO checkinStatusRespDTO = userAssetLogService.queryCheckinStatus();
        return Result.success(checkinStatusRespDTO);
    }

    @Operation(summary = "查询当前用户钱包与积分资产概况")
    @GetMapping("/wallet")
    public Result<UserWalletQueryDTO> queryUserWallet() {
        UserWalletQueryDTO userWalletQueryDTO = userAssetLogService.queryUserWallet();
        return Result.success(userWalletQueryDTO);
    }

    @Operation(summary = "分页查询个人资产与积分变动流水", description = "默认按创建时间 (create_time) 倒序排列")
    @GetMapping("/logs")
    public Result<PageResult<AssetLogPageRespDTO>> pageAssetLogs(AssetLogsPageReqDTO assetLogsPageReqDTO){
        PageResult<AssetLogPageRespDTO> pageResult = userAssetLogService.pageAssetLogs(assetLogsPageReqDTO);
        return Result.success(pageResult);
    }

}
