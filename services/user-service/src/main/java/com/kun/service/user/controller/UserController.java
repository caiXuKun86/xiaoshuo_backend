package com.kun.service.user.controller;

import com.kun.common.core.result.Result;
import com.kun.service.user.dto.req.ChangePasswordReqDTO;
import com.kun.service.user.dto.req.UserLoginReqDTO;
import com.kun.service.user.dto.req.UserProfileUpdateReqDTO;
import com.kun.service.user.dto.req.UserRegisterReqDTO;
import com.kun.service.user.dto.resp.RefreshTokenRespDTO;
import com.kun.service.user.dto.resp.UserLoginRespDTO;
import com.kun.service.user.dto.resp.UserProfileQueryRespDTO;
import com.kun.service.user.dto.resp.UserRegisterRespDTO;
import com.kun.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserRegisterRespDTO> register(@RequestBody UserRegisterReqDTO userRegisterReqDTO) {
        UserRegisterRespDTO userRegisterRespDTO = userService.register(userRegisterReqDTO);
        return Result.success(userRegisterRespDTO);
    }

    @PostMapping("/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO userLoginReqDTO) {
        UserLoginRespDTO userLoginRespDTO = userService.login(userLoginReqDTO);
        return Result.success(userLoginRespDTO);
    }

    @PostMapping("/refresh-token")
    public Result<RefreshTokenRespDTO> refreshToken(@RequestBody String refreshToken) {
        RefreshTokenRespDTO refreshTokenRespDTO = userService.refreshToken(refreshToken);
        return Result.success(refreshTokenRespDTO);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success();
    }

    @PostMapping("/password")
    public Result<Void> changePassword(@RequestBody ChangePasswordReqDTO changePasswordReqDTO) {
        userService.changePassword(changePasswordReqDTO);
        return Result.success();
    }

    @GetMapping("/profile")
    public Result<UserProfileQueryRespDTO> queryUserProfile() {
        UserProfileQueryRespDTO userProfileQueryRespDTO = userService.queryUserProfile();
        return Result.success(userProfileQueryRespDTO);
    }

    @PutMapping("/profile")
    public Result<Void> updateUserProfile(@RequestBody UserProfileUpdateReqDTO userProfileUpdateReqDTO) {
        userService.updateUserProfile(userProfileUpdateReqDTO);
        return Result.success();
    }

}
