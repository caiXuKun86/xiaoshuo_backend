package com.kun.common.core.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 当前登录用户信息载体 (下游服务透传)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一主键 ID
     */
    private Long userId;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    /**
     * 客户端公网 IP
     */
    private String clientIp;
}
