package com.kun.service.pay.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 平台全局唯一业务订单号 (REC+时间戳+机器码+随机数)
     */
    private String orderNo;


    /**
     * 订单应付金额 (单位: 分)
     */
    private Integer orderAmount;


    /**
     * 支付渠道 (0:未选择 1:微信支付[预留] 2:支付宝[首发支持] 3:苹果IAP[预留])
     */
    private Integer payChannel;


    /**
     * 订单支付截止失效时间 (创建后15分钟)
     */
    private LocalDateTime expireTime;
    /**
     * 订单超时时间
     */
    private Integer expireSecond;
    /**
     * 支付参数
     */
    Map<String, String> payParams;


}

