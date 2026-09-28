package com.kun.service.user.controller.inner;

import cn.hutool.core.bean.BeanUtil;
import com.kun.api.dto.user.UserDTO;
import com.kun.api.dto.user.UserPointsUpdateDTO;
import com.kun.common.core.result.Result;
import com.kun.service.user.domain.User;
import com.kun.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inner/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户 ID 查询用户基础资料
     */
    @GetMapping("/inner/user/{userId}")
    Result<UserDTO> getUserById(@PathVariable("userId") Long userId) {
        User user = userService.getById(userId);
        UserDTO userDTO =new UserDTO();
        BeanUtil.copyProperties(user,userDTO);
        return Result.success(userDTO);
    }

    /**
     * 跨服务变更用户积分 (充值到账、章节兑换扣除等)
     */
    @PostMapping("/inner/user/points/update")
    Result<Boolean> updatePoints(@RequestBody UserPointsUpdateDTO updateDTO){

        userService.updatePoints(updateDTO);
        return Result.success(true);
    }

}
