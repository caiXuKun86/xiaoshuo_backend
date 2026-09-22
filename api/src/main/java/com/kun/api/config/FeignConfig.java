package com.kun.api.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

/**
 * OpenFeign 全局配置
 * 1. 注册请求头透传拦截器
 * 2. 配置日志级别
 * 3. 自动扫描 api 模块下的 @FeignClient
 */
@AutoConfiguration
@EnableFeignClients(basePackages = "com.kun.api.client")
public class FeignConfig {

    /**
     * Feign 请求头透传拦截器
     */
    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }

    /**
     * Feign 日志级别 (生产建议 BASIC，开发排查建议 FULL)
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
