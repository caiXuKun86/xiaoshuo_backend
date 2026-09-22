package com.kun.common.core.constant;

/**
 * 网关与安全鉴权请求头常量规范
 */
public interface SecurityConstants {


    /**
     * 用户认证凭据请求头 (JWT)
     */
    String HEADER_AUTHORIZATION = "Authorization";

    /**
     * JWT 前缀
     */
    String TOKEN_PREFIX = "Bearer ";

    /**
     * 网关向下游透传的用户主键 ID 请求头
     */
    String HEADER_USER_ID = "X-User-Id";

    /**
     * 网关向下游透传的用户角色权限请求头
     */
    String HEADER_USER_ROLES = "X-User-Roles";

    /**
     * 客户端公网真实 IP 请求头
     */
    String HEADER_CLIENT_IP = "X-Client-Ip";

    /**
     * 全链路日志追踪 ID 请求头
     */
    String HEADER_TRACE_ID = "X-Trace-Id";

    /**
     * 防重幂等键请求头
     */
    String HEADER_IDEMPOTENT_KEY = "X-Idempotent-Key";

    /**
     * 人机验证验签 Ticket
     */
    String HEADER_CAPTCHA_TICKET = "X-Captcha-Ticket";
}
