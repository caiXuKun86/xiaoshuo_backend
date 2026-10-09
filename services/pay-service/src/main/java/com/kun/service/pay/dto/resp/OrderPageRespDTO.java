package com.kun.service.pay.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPageRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 平台全局唯一业务订单号 (REC+时间戳+机器码+随机数)
     */
    private String orderNo;

    /**
     * sku名称
     */
    private String skuName;
    /**
     * 订单应付金额 (单位: 分)
     */
    private Integer orderAmount;


    /**
     * 支付渠道 (0:未选择 1:微信支付[预留] 2:支付宝[首发支持] 3:苹果IAP[预留])
     */
    private Integer payChannel;

    /**
     * 支付渠道 (0:未选择 1:微信支付[预留] 2:支付宝[首发支持] 3:苹果IAP[预留])
     */
    private String payChannelName;
    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 订单状态描述
     */
    private String orderStatusDesc;


    /**
     * 支付成功时间
     */
    private LocalDateTime paySuccessTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}

