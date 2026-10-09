package com.kun.service.pay.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AlipayProperties.class)
public class AliPayClientConfig {

    @Bean
    public AlipayConfig alipayConfig(AlipayProperties alipayProperties) {
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(alipayProperties.getServerUrl());
        alipayConfig.setAppId(alipayProperties.getAppId());
        alipayConfig.setPrivateKey(alipayProperties.getPrivateKey());
        alipayConfig.setFormat(alipayProperties.getFormat());
        alipayConfig.setAlipayPublicKey(alipayProperties.getAlipayPublicKey());
        alipayConfig.setCharset(alipayProperties.getCharset());
        alipayConfig.setSignType(alipayProperties.getSignType());
        return alipayConfig;
    }

    @Bean
    public AlipayClient alipayClient(AlipayConfig alipayConfig) throws AlipayApiException {
        return new DefaultAlipayClient(alipayConfig);
    }
}