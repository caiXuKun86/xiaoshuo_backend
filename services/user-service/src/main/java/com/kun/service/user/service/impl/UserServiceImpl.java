package com.kun.service.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.core.context.LoginUser;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.enums.UserGenderEnum;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.core.utils.JwtUtils;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.service.user.domain.User;
import com.kun.service.user.dto.req.ChangePasswordReqDTO;
import com.kun.service.user.dto.req.UserLoginReqDTO;
import com.kun.service.user.dto.req.UserProfileUpdateReqDTO;
import com.kun.service.user.dto.req.UserRegisterReqDTO;
import com.kun.service.user.dto.resp.RefreshTokenRespDTO;
import com.kun.service.user.dto.resp.UserLoginRespDTO;
import com.kun.service.user.dto.resp.UserProfileQueryRespDTO;
import com.kun.service.user.dto.resp.UserRegisterRespDTO;
import com.kun.service.user.mapper.UserMapper;
import com.kun.service.user.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * @author Lenovo
 * @description 针对表【user(用户基础信息表)】的数据库操作Service实现
 * @createDate 2026-09-23 09:13:15
 */
@RequiredArgsConstructor
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public UserRegisterRespDTO register(UserRegisterReqDTO userRegisterReqDTO) {
        String username = userRegisterReqDTO.getUsername();
        String password = userRegisterReqDTO.getPassword();
        String confirmPassword = userRegisterReqDTO.getConfirmPassword();
        String nickName = userRegisterReqDTO.getNickName();

        if (StrUtil.hasBlank(username, password, confirmPassword)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "用户名和密码不能为空");
        }
        if (username.length() < 4 || username.length() > 20) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "用户名必须4~20位");
        }
        if (password.length() < 8 || password.length() > 20) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "密码必须8~20位");
        }
        if (StrUtil.isNotBlank(nickName) && (nickName.length() < 4 || nickName.length() > 20)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "昵称必须4~20位");
        }
        if (!ObjUtil.equals(password, confirmPassword)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "密码与确认密码不一致");
        }

        Long count = this.lambdaQuery().eq(User::getUsername, username).count();
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw(password));
        Integer gender = userRegisterReqDTO.getGender();
        user.setGender(gender != null && UserGenderEnum.getByCode(gender) != null ? gender : UserGenderEnum.UNKNOWN.getCode());
        user.setNickName(StrUtil.isNotBlank(nickName) ? nickName : String.format("书友_%s", RandomUtil.randomNumbers(6)));
        user.setRole("user");

        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        return new UserRegisterRespDTO(user.getId(), user.getUsername(), user.getNickName());
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO) {
        String username = userLoginReqDTO.getUsername();
        String password = userLoginReqDTO.getPassword();
        if (StrUtil.hasBlank(username, password)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "用户名和密码不能为空");
        }
        if (username.length() < 4 || username.length() > 20) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "用户名必须4~20位");
        }
        if (password.length() < 8 || password.length() > 20) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "密码必须8~20位");
        }

        User user = this.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        boolean isMatch = BCrypt.checkpw(password, user.getPassword());
        if (!isMatch) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        Long userId = user.getId();
        LoginUser loginUser = LoginUser.builder()
                .userId(userId)
                .userName(user.getUsername())
                .role(user.getRole())
                .build();
        String accessToken = JwtUtils.generateAccessToken(loginUser);
        String refreshToken = JwtUtils.generateRefreshToken(userId);

        String accessTokenRedisKey = RedisKeyConstants.USER_TOKEN_PREFIX + userId;
        String refreshTokenRedisKey = RedisKeyConstants.USER_REFRESH_TOKEN_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(
                accessTokenRedisKey,
                accessToken,
                2,
                TimeUnit.HOURS
        );
        stringRedisTemplate.opsForValue().set(
                refreshTokenRedisKey,
                refreshToken,
                14,
                TimeUnit.DAYS
        );

        UserLoginRespDTO userLoginRespDTO = new UserLoginRespDTO();
        userLoginRespDTO.setUserId(userId);
        userLoginRespDTO.setUserName(user.getUsername());
        userLoginRespDTO.setNickName(user.getNickName());
        userLoginRespDTO.setAccessToken(accessToken);
        userLoginRespDTO.setRefreshToken(refreshToken);
        return userLoginRespDTO;


    }

    @Override
    public RefreshTokenRespDTO refreshToken(String refreshToken) {
        Claims claims;
        try {
            claims = JwtUtils.parseToken(refreshToken);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_EXPIRED);
        }

        Long userId = Long.valueOf(claims.getSubject());
        String accessTokenRedisKey = RedisKeyConstants.USER_TOKEN_PREFIX + userId;
        String refreshTokenRedisKey = RedisKeyConstants.USER_REFRESH_TOKEN_PREFIX + userId;

        String cachedRefreshToken = stringRedisTemplate.opsForValue().get(refreshTokenRedisKey);

        // 校验 Redis 中的 RefreshToken 是否匹配（防伪造、防已被注销）
        if (cachedRefreshToken == null || !cachedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_EXPIRED);
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 签发新的 AccessToken 和 RefreshToken（滑动刷新）
        LoginUser loginUser = LoginUser.builder()
                .userId(userId)
                .userName(user.getUsername())
                .role(user.getRole())
                .build();
        String newAccessToken = JwtUtils.generateAccessToken(loginUser);
        String newRefreshToken = JwtUtils.generateRefreshToken(userId);
        // 更新 Redis 中的凭证
        stringRedisTemplate.opsForValue().set(
                accessTokenRedisKey,
                newAccessToken,
                2,
                TimeUnit.HOURS
        );
        stringRedisTemplate.opsForValue().set(
                refreshTokenRedisKey,
                newRefreshToken,
                14,
                TimeUnit.DAYS
        );
        return new RefreshTokenRespDTO(newAccessToken, newRefreshToken, 7200L);

    }

    @Override
    public void logout() {
        Long userId = UserContextHolder.getUserId();
        // 1. 删掉当前用户的访问令牌
        stringRedisTemplate.delete(RedisKeyConstants.USER_TOKEN_PREFIX + userId);
        // 2. 删掉刷新凭证
        stringRedisTemplate.delete(RedisKeyConstants.USER_REFRESH_TOKEN_PREFIX + userId);
    }

    @Override
    public void changePassword(ChangePasswordReqDTO changePasswordReqDTO) {
        Long userId = UserContextHolder.getUserId();

        String oldPassword = changePasswordReqDTO.getOldPassword();
        String newPassword = changePasswordReqDTO.getNewPassword();
        String confirmPassword = changePasswordReqDTO.getConfirmPassword();

        if (StrUtil.hasBlank(oldPassword, newPassword, confirmPassword)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "密码不能为空");
        }
        if (newPassword.length() < 8 || newPassword.length() > 20) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "密码必须8~20位");
        }

        if (!ObjUtil.equals(newPassword, confirmPassword)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "密码与确认密码不一致");
        }
        if (ObjUtil.equals(oldPassword, newPassword)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "修改失败,密码与原密码一致");
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        boolean isMatch = BCrypt.checkpw(oldPassword, user.getPassword());
        if (!isMatch) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "原密码输入不正确");
        }

        boolean update = this.lambdaUpdate()
                .set(User::getPassword, BCrypt.hashpw(newPassword))
                .eq(User::getId, userId)
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "修改失败");
        }

        // 1. 删掉当前用户的访问令牌
        stringRedisTemplate.delete(RedisKeyConstants.USER_TOKEN_PREFIX + userId);
        // 2. 删掉刷新凭证
        stringRedisTemplate.delete(RedisKeyConstants.USER_REFRESH_TOKEN_PREFIX + userId);

    }

    @Override
    public UserProfileQueryRespDTO queryUserProfile() {
        Long userId = UserContextHolder.getUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        UserProfileQueryRespDTO userProfileQueryRespDTO = new UserProfileQueryRespDTO();
        BeanUtil.copyProperties(user, userProfileQueryRespDTO);
        Integer isVip = user.getIsVip();
        userProfileQueryRespDTO.setVipDaysRemaining(isVip == 0 ? 0 : (int) ChronoUnit.DAYS.between(LocalDateTime.now(), user.getVipExpireTime()));

        return userProfileQueryRespDTO;
    }

    @Override
    public void updateUserProfile(UserProfileUpdateReqDTO userProfileUpdateReqDTO) {

        Long userId = UserContextHolder.getUserId();

        String nickName = userProfileUpdateReqDTO.getNickname();
        String avatar = userProfileUpdateReqDTO.getAvatar();
        Integer gender = userProfileUpdateReqDTO.getGender();

        if (nickName.length() < 4 || nickName.length() > 20) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "昵称必须4~20位");
        }
        if (gender != null && UserGenderEnum.getByCode(gender) == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "性别不存在");
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        boolean update = this.lambdaUpdate()
                .set(StrUtil.isNotBlank(nickName), User::getNickName, nickName)
                .set(StrUtil.isNotBlank(avatar), User::getAvatar, avatar)
                .set(gender != null, User::getGender, gender)
                .eq(User::getId, userId)
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "修改失败");
        }

    }
}




