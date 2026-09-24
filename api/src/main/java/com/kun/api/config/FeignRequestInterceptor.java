package com.kun.api.config;

import com.kun.common.core.constant.SecurityConstants;
import com.kun.common.core.context.UserContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

/**
 * OpenFeign 远程调用请求头透传拦截器
 * 将当前请求的上下文（如 X-User-Id, X-User-Roles, X-Trace-Id, X-Client-Ip 等）透传给下游微服务
 */
@Slf4j
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    // 仅向下游透传安全与全链路追踪核心 Header，避免将 Content-Length / Host 等冲突 Header 透传
                    if (isPassHeader(name)) {
                        String value = request.getHeader(name);
                        template.header(name, value);
                    }
                }
            }
        }

        // 如果在非 Web 请求线程中（如异步任务/MQ监听），或者 Web 请求中没有 X-User-Id Header，兜底尝试从 UserContextHolder 获取
        Long userId = UserContextHolder.getUserId();
        if (userId != null && !template.headers().containsKey(SecurityConstants.HEADER_USER_ID)) {
            template.header(SecurityConstants.HEADER_USER_ID, String.valueOf(userId));
        }
    }

    /**
     * 判断是否是需要透传的核心请求头
     */
    private boolean isPassHeader(String headerName) {
        if (!StringUtils.hasText(headerName)) {
            return false;
        }
        return headerName.equalsIgnoreCase(SecurityConstants.HEADER_AUTHORIZATION)
                || headerName.equalsIgnoreCase(SecurityConstants.HEADER_USER_ID)
                || headerName.equalsIgnoreCase(SecurityConstants.HEADER_USER_ROLE)
                || headerName.equalsIgnoreCase(SecurityConstants.HEADER_TRACE_ID)
                || headerName.equalsIgnoreCase(SecurityConstants.HEADER_IDEMPOTENT_KEY)
                || headerName.equalsIgnoreCase(SecurityConstants.HEADER_CAPTCHA_TICKET);
    }
}
