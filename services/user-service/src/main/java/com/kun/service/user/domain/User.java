package com.kun.service.user.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kun.common.database.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户基础信息表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User extends BaseEntity   {

    private static final long serialVersionUID = 1L;

    /**
     * 用户主键 ID (雪花算法)
     */
    @TableId
    private Long id;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 加盐密码 (BCrypt 散列)
     */
    private String password;

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
    private Integer pointBalance;

    /**
     * VIP 标识冗余 (0:非VIP 1:VIP)
     */
    private Integer isVip;

    /**
     * VIP 会员到期时间
     */
    private LocalDateTime vipExpireTime;

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




}