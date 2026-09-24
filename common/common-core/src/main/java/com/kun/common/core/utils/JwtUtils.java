package com.kun.common.core.utils;

import com.kun.common.core.context.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;


public final class JwtUtils {

    // 建议在配置文件中配置，且至少 256 位（32 字符以上）
    private static final String SECRET_KEY_STR = "xiaoshuo-secret-key-must-be-very-long-and-secure-32chars!";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STR.getBytes(StandardCharsets.UTF_8));

    // AccessToken 有效期 2 小时
    public static final long ACCESS_TOKEN_EXPIRE = 2 * 60 * 60 * 1000L;
    // RefreshToken 有效期 14 天
    public static final long REFRESH_TOKEN_EXPIRE = 14 * 24 * 60 * 60 * 1000L;

    /**
     * 生成短期的 AccessToken（携带业务 Claims）
     */
    public static String generateAccessToken(LoginUser loginUser) {
        return Jwts.builder()
                .subject(String.valueOf(loginUser.getUserId()))
                .claim("username", loginUser.getUserName())
                .claim("role", loginUser.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 生成长期的 RefreshToken（尽量少存业务字段，仅保留用户主体）
     */
    public static String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .id(UUID.randomUUID().toString()) // 随机唯一标识
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 解析并校验 Claims
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}