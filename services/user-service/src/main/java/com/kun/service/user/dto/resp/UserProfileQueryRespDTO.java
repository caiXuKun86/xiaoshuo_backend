package com.kun.service.user.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileQueryRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 登录用户名
     */
    private String username;


    /**
     * 用户昵称 (展示用)
     */
    private String nickName;

    /**
     * 头像 OSS 地址
     */
    private String avatar;

    /**
     * 性别 (0:未知 1:男 2:女)
     */
    private Integer gender;

    /**
     * 账户可用积分余额 (兑换章节，无符号禁止为负)
     */
    private Long pointBalance;

    /**
     * VIP 标识冗余 (0:非VIP 1:VIP)
     */
    private Integer isVip;

    /**
     * VIP 会员到期时间
     */
    private LocalDateTime vipExpireTime;

    /**
     * VIP 会员剩余时间
     */
    private Integer vipDaysRemaining;

    /**
     * 是否为认证作家 (0:否 1:是)
     */
    private Integer isWriter;

    /**
     * 权限角色 (user/writer/admin/super)
     */
    private String role;

    /**
     * 个人专属邀请码
     */
    private String invitationCode;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
