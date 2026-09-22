package com.kun.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户跨服务传输基础信息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "跨服务用户基础信息传输对象")
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户唯一主键 ID")
    private Long id;

    @Schema(description = "登录用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "头像 OSS 地址")
    private String avatar;

    @Schema(description = "账户可用积分余额")
    private Long pointBalance;

    @Schema(description = "VIP 标识 (0:非VIP 1:VIP)")
    private Integer isVip;

    @Schema(description = "VIP 到期时间")
    private LocalDateTime vipExpireTime;

    @Schema(description = "是否认证作家 (0:否 1:是)")
    private Integer isWriter;

    @Schema(description = "权限角色 (user/writer/admin/super)")
    private String role;
}
