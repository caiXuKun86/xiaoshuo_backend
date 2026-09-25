package com.kun.gateway.handler;

import cn.hutool.json.JSONUtil;
import com.kun.common.core.constant.SecurityConstants;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关全局统一异常处理
 * 捕获 404 (路由不存在)、服务不可达、系统未知异常，统一转换为标准 Result JSON 响应
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String traceId = response.getHeaders().getFirst(SecurityConstants.HEADER_TRACE_ID);
        log.error("[网关异常] 路径: {}, 原因: {}", exchange.getRequest().getPath(), ex.getMessage(), ex);

        Result<?> result;
        if (ex instanceof NotFoundException) {
            result = Result.fail(ResultCode.SERVICE_DEGRADED.getCode(), "服务路由不存在或实例未找到");
        } else if (ex instanceof ResponseStatusException responseStatusException) {
            result = Result.fail(responseStatusException.getStatusCode().value(),
                    responseStatusException.getReason() != null ? responseStatusException.getReason() : "网络请求异常");
        } else {
            result = Result.fail(ResultCode.SYSTEM_ERROR.getCode(), "网关服务异常，请稍后重试");
        }
        result.setTraceId(traceId);

        byte[] bytes = JSONUtil.toJsonStr(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
