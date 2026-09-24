package com.kun.service.user.service;

import com.kun.service.user.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.service.user.dto.req.ChangePasswordReqDTO;
import com.kun.service.user.dto.req.UserLoginReqDTO;
import com.kun.service.user.dto.req.UserProfileUpdateReqDTO;
import com.kun.service.user.dto.req.UserRegisterReqDTO;
import com.kun.service.user.dto.resp.RefreshTokenRespDTO;
import com.kun.service.user.dto.resp.UserLoginRespDTO;
import com.kun.service.user.dto.resp.UserProfileQueryRespDTO;
import com.kun.service.user.dto.resp.UserRegisterRespDTO;

/**
* @author Lenovo
* @description 针对表【user(用户基础信息表)】的数据库操作Service
* @createDate 2026-09-23 09:13:15
*/
public interface UserService extends IService<User> {

    UserRegisterRespDTO register(UserRegisterReqDTO userRegisterReqDTO);

    UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO);

    RefreshTokenRespDTO refreshToken(String refreshToken);

    void logout();

    void changePassword(ChangePasswordReqDTO changePasswordReqDTO);

    UserProfileQueryRespDTO queryUserProfile();

    void updateUserProfile(UserProfileUpdateReqDTO userProfileUpdateReqDTO);
}
