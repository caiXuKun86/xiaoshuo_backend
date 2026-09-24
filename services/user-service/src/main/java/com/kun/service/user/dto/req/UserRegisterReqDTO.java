package com.kun.service.user.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 确认密码
     */
    private String confirmPassword;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 昵称
     */
    private String nickName;
    /**
     * 邀请码
     */
    private String invitationCode;

    /**
     * 人机校验通过凭据
     */
    private String captchaTicket;


}
