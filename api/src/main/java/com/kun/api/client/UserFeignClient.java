package com.kun.api.client;

import com.kun.api.config.FeignConfig;
import com.kun.api.dto.user.UserDTO;
import com.kun.api.dto.user.UserPointsUpdateDTO;
import com.kun.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 用户与资产服务内部 Feign 声明接口
 */
@FeignClient(value = "com.kun.service.book.service-user", contextId = "userFeignClient", configuration = FeignConfig.class)
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

    /**
     * 校验指定用户是否已经兑换解锁该章节
     */
    @GetMapping("/inner/user/chapter/check-unlocked")
    Result<Boolean> checkChapterUnlocked(@RequestParam("userId") Long userId,
                                         @RequestParam("chapterId") Long chapterId);
}
