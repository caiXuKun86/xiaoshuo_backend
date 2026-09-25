package com.kun.gateway.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.kun.common.core.constant.SecurityConstants;
import com.kun.common.core.enums.IResultCode;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.result.Result;
import com.kun.common.core.utils.JwtUtils;
import com.kun.gateway.config.GatewaySecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关全局统一安全认证过滤器
 * 1. 清洗客户端可能伪造的 X-User-Id / X-User-Role 请求头
 * 2. 校验免登录白名单
 * 3. 校验并解析 JWT Token
 * 4. 向下游微服务请求头中透传 X-User-Id, X-User-Role, X-Client-Ip
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final GatewaySecurityProperties gatewaySecurityProperties;
    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private static final String USER_TOKEN_PREFIX = "user:token:";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 1. 安全防范：清理客户端可能伪造的内部敏感头
        ServerHttpRequest.Builder mutateBuilder = request.mutate();
        mutateBuilder.headers(headers -> {
            headers.remove(SecurityConstants.HEADER_USER_ID);
            headers.remove(SecurityConstants.HEADER_USER_ROLE);
        });

        // 2. 注入客户端真实 IP (X-Client-Ip)
        String clientIp = getClientIp(request);
        mutateBuilder.header("X-Client-Ip", clientIp);

        // 3. 判断是否在免登录白名单中
        boolean isWhitelisted = isWhitelist(path);

        // 4. 提取 Authorization Header 中的 JWT Token
        String authHeader = request.getHeaders().getFirst(SecurityConstants.HEADER_AUTHORIZATION);
        String token = null;
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            token = authHeader.substring(SecurityConstants.TOKEN_PREFIX.length()).trim();
        }

        // 5. 如果是白名单接口且未携带 Token，直接放行
        if (isWhitelisted && StrUtil.isBlank(token)) {
            return chain.filter(exchange.mutate().request(mutateBuilder.build()).build());
        }

        // 6. 如果不是白名单接口且未携带 Token，直接拦截提示未登录
        if (!isWhitelisted && StrUtil.isBlank(token)) {
            return writeErrorResponse(exchange, ResultCode.UNAUTHORIZED);
        }

        // 7. 校验并解析 Token
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token);
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: path={}, ip={}", path, clientIp);
            if (isWhitelisted) {
                // 白名单接口即使 token 过期也作为访客直接放行
                return chain.filter(exchange.mutate().request(mutateBuilder.build()).build());
            }
            return writeErrorResponse(exchange, ResultCode.ACCESS_TOKEN_EXPIRED);
        } catch (Exception e) {
            log.warn("Token 无效或篡改: path={}, ip={}, error={}", path, clientIp, e.getMessage());
            if (isWhitelisted) {
                return chain.filter(exchange.mutate().request(mutateBuilder.build()).build());
            }
            return writeErrorResponse(exchange, ResultCode.TOKEN_INVALID);
        }


        String userId = claims.getSubject();
        String role = claims.get("role", String.class);
        String finalToken = token;

        //第二重校验,以防用户注销或顶号

        String redisKey = USER_TOKEN_PREFIX + userId;


        return reactiveStringRedisTemplate.opsForValue().get(redisKey)
                .flatMap(cachedToken -> {
                    // 比对客户端传来的 Token 与 Redis 中存储的是否一致
                    if (!StrUtil.equals(finalToken, cachedToken)) {
                        log.warn("Token 与 Redis 记录不一致 (可能在其他设备登录被顶号), userId: {}", userId);
                        return writeErrorResponse(exchange, ResultCode.TOKEN_INVALID);
                    }
                    // 校验完全通过，注入下游上下文
                    mutateBuilder.header(SecurityConstants.HEADER_USER_ID, userId);
                    if (StrUtil.isNotBlank(role)) {
                        mutateBuilder.header(SecurityConstants.HEADER_USER_ROLE, role);
                    }
                    return chain.filter(exchange.mutate().request(mutateBuilder.build()).build());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Redis 中查不到此 Key：说明用户已调用 logout 退出登录，或者 Redis Key 已过期
                    log.warn("Redis 中未查询到有效 Token (可能已主动注销), userId: {}", userId);
                    if (isWhitelisted) {
                        return chain.filter(exchange.mutate().request(mutateBuilder.build()).build());
                    }
                    return writeErrorResponse(exchange, ResultCode.ACCESS_TOKEN_EXPIRED);
                }));
    }

    /**
     * 判断当前路径是否命中免登录白名单
     */
    private boolean isWhitelist(String path) {
        List<String> whitelist = gatewaySecurityProperties.getWhitelist();
        if (whitelist == null || whitelist.isEmpty()) {
            return false;
        }
        for (String pattern : whitelist) {
            if (antPathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取客户端真实公网 IP
     */
    private String getClientIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("X-Forwarded-For");
        if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(",");
            return index != -1 ? ip.substring(0, index).trim() : ip.trim();
        }
        ip = headers.getFirst("X-Real-IP");
        if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null) {
            InetAddress address = remoteAddress.getAddress();
            if (address != null) {
                return address.getHostAddress();
            }
        }
        return "127.0.0.1";
    }

    /**
     * 响应式写入标准 JSON 错误格式（HTTP 200 OK，内嵌统一业务错误码）
     */
    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, IResultCode resultCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String traceId = response.getHeaders().getFirst(SecurityConstants.HEADER_TRACE_ID);
        Result<?> result = Result.fail(resultCode);
        result.setTraceId(traceId);

        byte[] bytes = JSONUtil.toJsonStr(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 在 TraceFilter 之后执行，但在路由转发之前执行
        return -100;
    }
}
