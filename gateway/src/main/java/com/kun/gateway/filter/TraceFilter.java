package com.kun.gateway.filter;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.kun.common.core.constant.SecurityConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全链路日志追踪过滤器
 * 负责在请求头中生成与传递 X-Trace-Id，并同步写入响应头
 */
@Component
public class TraceFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String traceId = request.getHeaders().getFirst(SecurityConstants.HEADER_TRACE_ID);

        if (StrUtil.isBlank(traceId)) {
            // 生成规范格式 traceId: tr-xxxxxxxxxxxx
            traceId = "tr-" + IdUtil.fastSimpleUUID();
        }

        // 1. 将 Trace-Id 注入请求头向下游微服务传递
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(SecurityConstants.HEADER_TRACE_ID, traceId)
                .build();

        // 2. 将 Trace-Id 注入响应头，方便前端/移动端排查链路问题
        exchange.getResponse().getHeaders().set(SecurityConstants.HEADER_TRACE_ID, traceId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        // 最高优先级，最先执行
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
