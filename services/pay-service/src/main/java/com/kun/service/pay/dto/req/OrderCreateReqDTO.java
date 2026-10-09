package com.kun.service.pay.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 套餐主键 ID
     */
    private Long skuId;
    /**
     * 渠道
     */
    private Integer payChannel;

    /**
     * 支付场景
     */
    private String payScene;

}

