package com.kun.service.user.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * accessToken
     */
    private String accessToken;
    /**
     * refreshToken
     */
    private String refreshToken;
    /**
     * accessToken过期时间
     */
    private Long expiresIn;
}
