package com.kun.service.user.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginReqDTO implements Serializable {

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
     * 人机校验通过凭据
     */
    private String captchaTicket;


}
