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
public class OrderStatusQueryRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 平台全局唯一业务订单号 (REC+时间戳+机器码+随机数)
     */
    private String orderNo;


    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 订单状态描述
     */
    private String orderStatusDesc;

    /**
     * 支付金额
     */
    private Integer orderAmount;
    /**
     * 商品名称
     */
    private String skuName;

    /**
     * 支付成功时间
     */
    private LocalDateTime paySuccessTime;
    /**
     * 积分数量
     */
    private Integer pointsAmount;
    /**
     * vip时间
     */
    private Integer vipDays;


}

