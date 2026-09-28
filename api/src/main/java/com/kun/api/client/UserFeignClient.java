package com.kun.api.client;

import com.kun.api.config.FeignConfig;
import com.kun.api.dto.user.UserDTO;
import com.kun.api.dto.user.UserPointsUpdateDTO;
import com.kun.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户与资产服务内部 Feign 声明接口
 */
@FeignClient(value = "user-service", contextId = "userFeignClient", configuration = FeignConfig.class)
public interface UserFeignClient {

    /**
     * 根据用户 ID 查询用户基础资料
     */
    @GetMapping("/inner/user/{userId}")
    Result<UserDTO> getUserById(@PathVariable("userId") Long userId);

    /**
     * 跨服务变更用户积分 (充值到账、章节兑换扣除等)
     */
    @PostMapping("/inner/user/points/update")
    Result<Boolean> updatePoints(@RequestBody UserPointsUpdateDTO updateDTO);


}
