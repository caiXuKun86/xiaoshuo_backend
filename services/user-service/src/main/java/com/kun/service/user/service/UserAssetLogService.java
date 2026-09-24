package com.kun.service.user.service;

import com.kun.common.database.page.PageResult;
import com.kun.service.user.domain.UserAssetLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.service.user.dto.req.AssetLogsPageReqDTO;
import com.kun.service.user.dto.resp.AssetLogPageRespDTO;
import com.kun.service.user.dto.resp.CheckinStatusRespDTO;
import com.kun.service.user.dto.resp.UserCheckinRespDTO;
import com.kun.service.user.dto.resp.UserWalletQueryDTO;

/**
* @author Lenovo
* @description 针对表【user_asset_log(用户资产与积分变动流水表)】的数据库操作Service
* @createDate 2026-09-23 09:13:28
*/
public interface UserAssetLogService extends IService<UserAssetLog> {

    UserCheckinRespDTO userCheckin();

    CheckinStatusRespDTO queryCheckinStatus();

    UserWalletQueryDTO queryUserWallet();

    PageResult<AssetLogPageRespDTO> pageAssetLogs(AssetLogsPageReqDTO assetLogsPageReqDTO);
}
