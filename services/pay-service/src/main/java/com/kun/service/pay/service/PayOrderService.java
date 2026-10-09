package com.kun.service.pay.service;

import com.kun.common.database.page.PageResult;
import com.kun.service.pay.domain.PayOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.service.pay.dto.req.OrderCreateReqDTO;
import com.kun.service.pay.dto.req.OrderPageReqDTO;
import com.kun.service.pay.dto.resp.OrderCreateRespDTO;
import com.kun.service.pay.dto.resp.OrderPageRespDTO;
import com.kun.service.pay.dto.resp.OrderStatusQueryRespDTO;

/**
* @author Lenovo
* @description 针对表【pay_order(充值交易订单主表)】的数据库操作Service
* @createDate 2026-10-09 08:48:04
*/
public interface PayOrderService extends IService<PayOrder> {

    OrderCreateRespDTO createOrderAndPay(OrderCreateReqDTO orderCreateReqDTO);

    void completeOrderPay(String orderNo, String totalAmountStr, String channelTradeNo);

    OrderStatusQueryRespDTO queryOrderStatus(String orderNo);

    void closeTimeoutOrder(String orderNo);

    PageResult<OrderPageRespDTO> pageOrder(OrderPageReqDTO orderPageReqDTO);
}
