package com.kun.service.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {
    /**
     * 支付宝网关，沙箱环境: https://openapi-sandbox.dl.alipaydev.com/gateway.do 正式环境: https://openapi.alipay.com/gateway.do
     */
    private String serverUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    /**
     * 应用ID
     */
    private String appId;
    /**
     * 应用私钥
     */
    private String privateKey;
    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;
    /**
     * 参数返回格式，默认 json
     */
    private String format = "json";
    /**
     * 编码字符集，默认 UTF-8
     */
    private String charset = "UTF-8";
    /**
     * 签名类型，默认 RSA2
     */
    private String signType = "RSA2";
    /**
     * 同步跳转通知页面
     */
    private String returnUrl;
    /**
     * 异步通知接口地址
     */
    private String notifyUrl;
}