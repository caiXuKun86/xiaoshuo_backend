package com.kun.common.core.interceptor;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.kun.common.core.constant.SecurityConstants;
import com.kun.common.core.context.LoginUser;
import com.kun.common.core.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 下游微服务用户信息与全链路追踪上下文拦截器
 * 1. 从网关透传的请求头中提取 X-Trace-Id 存入 MDC 与响应头，保证日志链路与返回追踪
 * 2. 从网关透传的请求头中提取 X-User-Id、X-User-Role 填充到 UserContextHolder 中
 */
@ConditionalOnClass(HttpServletRequest.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 处理全链路日志追踪 ID (Trace-Id)
        String traceId = request.getHeader(SecurityConstants.HEADER_TRACE_ID);
        if (StrUtil.isBlank(traceId)) {
            traceId = "tr-" + IdUtil.fastSimpleUUID();
        }
        MDC.put(SecurityConstants.HEADER_TRACE_ID, traceId);
        MDC.put("traceId", traceId);
        response.setHeader(SecurityConstants.HEADER_TRACE_ID, traceId);

        // 2. 处理当前登录用户信息 (X-User-Id / X-User-Role)
        String userIdStr = request.getHeader(SecurityConstants.HEADER_USER_ID);
        String role = request.getHeader(SecurityConstants.HEADER_USER_ROLE);

        if (StrUtil.isNotBlank(userIdStr)) {
            try {
                Long userId = Long.valueOf(userIdStr);
                LoginUser loginUser = LoginUser.builder()
                        .userId(userId)
                        .role(role)
                        .build();
                UserContextHolder.set(loginUser);
            } catch (NumberFormatException ignored) {
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后必须清理，防止线程复用导致内存泄漏与数据/日志串号
        UserContextHolder.clear();
        MDC.remove(SecurityConstants.HEADER_TRACE_ID);
        MDC.remove("traceId");
        MDC.clear();
    }
}
