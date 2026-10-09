package com.kun.service.pay.mq.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayOrderSuccessEvent {

    private Long userId;
    private Integer skuType;
    private Integer points;
    private Integer vipDays;
    private String orderNo;
}
