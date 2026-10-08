package com.kun.api.client;

import com.kun.api.client.fallback.UserFeignClientFallbackFactory;
import com.kun.api.dto.user.RegisterWriterDTO;
import com.kun.api.dto.user.UserDTO;
import com.kun.api.dto.user.UserPointsUpdateDTO;
import com.kun.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

/**
 * 用户与资产服务内部 Feign 声明接口
 */
@FeignClient(value = "user-service", contextId = "userFeignClient",fallbackFactory = UserFeignClientFallbackFactory.class)
public interface UserFeignClient {

    /**
     * 根据用户 ID 查询用户基础资料
     */
    @GetMapping("/inner/user/{userId}")
    Result<UserDTO> getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/inner/user/list")
    Result<List<UserDTO>> getUserByIds(@RequestParam("userIds") Collection<Long> userIds);

    /**
     * 跨服务变更用户积分 (充值到账、章节兑换扣除等)
     */
    @PostMapping("/inner/user/points/update")
    Result<Boolean> updatePoints(@RequestBody UserPointsUpdateDTO updateDTO);

    /**
     * 更新作家
     */
    @PostMapping("/inner/user/writer/register")
    Result<Boolean> registerWriter(@RequestBody RegisterWriterDTO registerWriterDTO);
}
