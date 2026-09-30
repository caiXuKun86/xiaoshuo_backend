package com.kun.service.user.controller.inner;

import cn.hutool.core.bean.BeanUtil;
import com.kun.api.dto.user.RegisterWriterDTO;
import com.kun.api.dto.user.UserDTO;
import com.kun.api.dto.user.UserPointsUpdateDTO;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.core.result.Result;
import com.kun.service.user.domain.User;
import com.kun.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController("innerUserController")
@RequestMapping("/inner/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户 ID 查询用户基础资料
     */
    @GetMapping("/{userId}")
    Result<UserDTO> getUserById(@PathVariable("userId") Long userId) {
        User user = userService.getById(userId);
        UserDTO userDTO =new UserDTO();
        BeanUtil.copyProperties(user,userDTO);
        return Result.success(userDTO);
    }

    /**
     * 跨服务变更用户积分 (充值到账、章节兑换扣除等)
     */
    @PostMapping("/points/update")
    Result<Boolean> updatePoints(@RequestBody UserPointsUpdateDTO updateDTO){

        userService.updatePoints(updateDTO);
        return Result.success(true);
    }

    @PostMapping("/writer/register")
    Result<Boolean> registerWriter(@RequestBody RegisterWriterDTO registerWriterDTO) {
        Long userId = UserContextHolder.getUserId();
        boolean update = userService.lambdaUpdate()
                .set(User::getIsWriter, registerWriterDTO.getIsWriter())
                .eq(User::getId, userId)
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR);
        }
        return Result.success(true);
    }

}
