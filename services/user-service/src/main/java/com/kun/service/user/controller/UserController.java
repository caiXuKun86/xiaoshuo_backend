package com.kun.service.user.controller;

import com.kun.common.core.result.Result;
import com.kun.common.oss.template.OssTemplate;
import com.kun.service.user.dto.req.ChangePasswordReqDTO;
import com.kun.service.user.dto.req.UserLoginReqDTO;
import com.kun.service.user.dto.req.UserProfileUpdateReqDTO;
import com.kun.service.user.dto.req.UserRegisterReqDTO;
import com.kun.service.user.dto.resp.RefreshTokenRespDTO;
import com.kun.service.user.dto.resp.UserLoginRespDTO;
import com.kun.service.user.dto.resp.UserProfileQueryRespDTO;
import com.kun.service.user.dto.resp.UserRegisterRespDTO;
import com.kun.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Tag(name = "用户认证与个人中心", description = "提供用户注册、登录、Token刷新、退出、修改密码、个人资料查询与修改等接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;
    private final OssTemplate ossTemplate;

    @Operation(summary = "用户账号注册", description = "新用户注册，需提供用户名、密码、确认密码及人机验证凭据")
    @PostMapping("/register")
    public Result<UserRegisterRespDTO> register(@RequestBody UserRegisterReqDTO userRegisterReqDTO) {
        UserRegisterRespDTO userRegisterRespDTO = userService.register(userRegisterReqDTO);
        return Result.success(userRegisterRespDTO);
    }

    @Operation(summary = "账号密码登录", description = "用户通过用户名与密码登录，成功后颁发 AccessToken 与 RefreshToken")
    @PostMapping("/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO userLoginReqDTO) {
        UserLoginRespDTO userLoginRespDTO = userService.login(userLoginReqDTO);
        return Result.success(userLoginRespDTO);
    }

    @Operation(summary = "双 Token 刷新令牌", description = "AccessToken 过期后，使用有效的 RefreshToken 无感置换新的凭据")
    @PostMapping("/refresh-token")
    public Result<RefreshTokenRespDTO> refreshToken(
            @Parameter(description = "刷新凭证 RefreshToken") @RequestBody String refreshToken) {
        RefreshTokenRespDTO refreshTokenRespDTO = userService.refreshToken(refreshToken);
        return Result.success(refreshTokenRespDTO);
    }

    @Operation(summary = "用户退出登录", description = "主动注销当前用户的 AccessToken 与 RefreshToken")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success();
    }

    @Operation(summary = "修改登录密码", description = "已登录用户修改密码，需提供原密码及新密码并做一致性校验")
    @PostMapping("/password")
    public Result<Void> changePassword(@RequestBody ChangePasswordReqDTO changePasswordReqDTO) {
        userService.changePassword(changePasswordReqDTO);
        return Result.success();
    }

    @Operation(summary = "获取当前登录用户资料", description = "根据当前请求上下文中的用户ID，查询个人基本资料（昵称、头像、简介、VIP状态等）")
    @GetMapping("/profile")
    public Result<UserProfileQueryRespDTO> queryUserProfile() {
        UserProfileQueryRespDTO userProfileQueryRespDTO = userService.queryUserProfile();
        return Result.success(userProfileQueryRespDTO);
    }

    @Operation(summary = "修改个人基本资料", description = "修改当前登录用户的昵称、头像、个人简介、性别等基本信息")
    @PutMapping("/profile")
    public Result<Void> updateUserProfile(@RequestBody UserProfileUpdateReqDTO userProfileUpdateReqDTO) {
        userService.updateUserProfile(userProfileUpdateReqDTO);
        return Result.success();
    }

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @PostMapping("/avatar/upload")
    public Result<String> uploadCover(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }
        // 2. 校验文件大小 (不超过 2MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.fail("图片大小不能超过 2MB");
        }
        // 3. 校验文件后缀名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.fail("上传文件缺少后缀名");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(suffix)) {
            return Result.fail("仅支持 JPG、PNG、WEBP 格式的图片");
        }
        // 4. (推荐) 校验 MIME 类型，防止恶意修改文件后缀上传
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            return Result.fail("非法图片格式");
        }
        // 5. 规划 OSS 存储路径并上传
        String objectName = "cover/" + UUID.randomUUID().toString().replace("-", "") + suffix;
        String fileUrl = ossTemplate.uploadFile(objectName, file.getInputStream());
        return Result.success(fileUrl);
    }


}
