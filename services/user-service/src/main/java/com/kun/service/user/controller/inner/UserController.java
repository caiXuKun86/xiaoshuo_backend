package com.kun.service.user.controller.inner;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.kun.api.client.UserFeignClient;
import com.kun.api.dto.user.RegisterWriterDTO;
import com.kun.api.dto.user.UserDTO;
import com.kun.api.dto.user.UserPointsUpdateDTO;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.core.result.Result;
import com.kun.service.user.domain.User;
import com.kun.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController("innerUserController")
@RequestMapping("/inner/user")
@RequiredArgsConstructor
public class UserController implements UserFeignClient {

    private final UserService userService;

    /**
     * 根据用户 ID 查询用户基础资料
     */
    @GetMapping("/{userId}")
    public Result<UserDTO> getUserById(@PathVariable("userId") Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.success();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        return Result.success(userDTO);
    }

    @GetMapping("/list")
    public Result<List<UserDTO>> getUserByIds(@RequestParam("userIds") Collection<Long> userIds) {
        if(CollUtil.isEmpty(userIds)){
            return Result.success(Collections.emptyList());

        }
        List<User> userList = userService.listByIds(userIds);
        if(CollUtil.isEmpty(userList)){
            return Result.success(Collections.emptyList());
        }
        List<UserDTO> userDTOS = BeanUtil.copyToList(userList, UserDTO.class);
        return Result.success(userDTOS);
    }

    /**
     * 跨服务变更用户积分 (充值到账、章节兑换扣除等)
     */
    @PostMapping("/points/update")
    public Result<Boolean> updatePoints(@RequestBody UserPointsUpdateDTO updateDTO) {

        userService.updatePoints(updateDTO);
        return Result.success(true);
    }

    @PostMapping("/writer/register")
    public Result<Boolean> registerWriter(@RequestBody RegisterWriterDTO registerWriterDTO) {
        boolean update = userService.lambdaUpdate()
                .set(User::getIsWriter, registerWriterDTO.getIsWriter())
                .eq(User::getId, registerWriterDTO.getUserId())
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        return Result.success(true);
    }

}
