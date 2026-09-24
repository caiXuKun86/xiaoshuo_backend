package com.kun.service.user.dto.resp;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户Id
     */
    private Long userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户昵称
     */
    private String nickName;

}
