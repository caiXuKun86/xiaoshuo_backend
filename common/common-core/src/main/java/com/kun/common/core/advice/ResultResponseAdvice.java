package com.kun.common.core.advice;

import cn.hutool.core.util.StrUtil;
import com.kun.common.core.constant.SecurityConstants;
import com.kun.common.core.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局统一响应体增强切面
 * 自动为所有 Controller 输出的 Result 响应体注入全链路追踪 traceId
 */
@RestControllerAdvice
@ConditionalOnClass(HttpServletRequest.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ResultResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 拦截所有返回内容，并在 beforeBodyWrite 中判断是否为 Result
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof Result<?> result) {
            // 如果业务代码未显式指定 traceId，自动从 MDC 或请求头中提取并填充
            if (StrUtil.isBlank(result.getTraceId())) {
                String traceId = MDC.get(SecurityConstants.HEADER_TRACE_ID);
                if (StrUtil.isBlank(traceId)) {
                    traceId = request.getHeaders().getFirst(SecurityConstants.HEADER_TRACE_ID);
                }
                result.setTraceId(traceId);
            }
        }
        return body;
    }
}
