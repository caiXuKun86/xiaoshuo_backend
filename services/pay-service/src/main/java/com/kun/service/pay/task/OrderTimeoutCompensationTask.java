package com.kun.service.pay.task;

import com.kun.common.core.enums.OrderStatusEnum;
import com.kun.service.pay.domain.PayOrder;
import com.kun.service.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTimeoutCompensationTask {

    private final PayOrderService payOrderService;

    /**
     * 每 2 分钟执行一次超时订单对账补偿
     */
    @Scheduled(cron = "0 0/2 * * * ?")
    public void compensateTimeoutOrders() {

        // 捞取超时未关单的数据（命中 idx_expire_status 索引）
        List<PayOrder> timeoutOrders = payOrderService.lambdaQuery()
                .eq(PayOrder::getOrderStatus, OrderStatusEnum.PENDING.getCode())
                .le(PayOrder::getExpireTime, LocalDateTime.now())
                .last("LIMIT 100")
                .list();

        if (timeoutOrders.isEmpty()) {
            return;
        }

        log.warn("【补偿触发】扫描到 {} 笔超时未关闭订单，开始补偿关单...", timeoutOrders.size());
        for (PayOrder order : timeoutOrders) {
            // 统一复用你的安全关单逻辑
            payOrderService.closeTimeoutOrder(order.getOrderNo());
        }


    }
}