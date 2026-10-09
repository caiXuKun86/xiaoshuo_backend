package com.kun.service.pay.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.pay.config.AlipayProperties;
import com.kun.service.pay.dto.req.OrderCreateReqDTO;
import com.kun.service.pay.dto.req.OrderPageReqDTO;
import com.kun.service.pay.dto.resp.OrderCreateRespDTO;
import com.kun.service.pay.dto.resp.OrderPageRespDTO;
import com.kun.service.pay.dto.resp.OrderStatusQueryRespDTO;
import com.kun.service.pay.service.PayOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay/order")
@RequiredArgsConstructor
@Slf4j
public class PayOrderController {
    private final PayOrderService payOrderService;
    private final AlipayProperties alipayProperties;

    @PostMapping("/create")
    public Result<OrderCreateRespDTO> createOrder(@RequestBody OrderCreateReqDTO orderCreateReqDTO) {
        OrderCreateRespDTO orderCreateRespDTO = payOrderService.createOrderAndPay(orderCreateReqDTO);
        return Result.success(orderCreateRespDTO);
    }

    @GetMapping("/status/{orderNo}")
    public Result<OrderStatusQueryRespDTO> queryOrderStatus(@PathVariable String orderNo){
        OrderStatusQueryRespDTO orderStatusQueryRespDTO = payOrderService.queryOrderStatus(orderNo);
        return Result.success(orderStatusQueryRespDTO);
    }



    /**
     * 支付宝异步回调通知 (必须是 POST 请求，且返回纯文本 "success" 或 "fail")
     */
    @PostMapping("/notify/alipay")
    public String handleAlipayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = String.join(",", values);
            params.put(name, valueStr);
        }
        try {
            // 2. 验签 (使用支付宝公钥校验签名合法性)
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );
            if (!signVerified) {
                log.warn("支付宝回调验签失败, params: {}", params);
                return "fail";
            }
            // 3. 业务校验与处理
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");
            String totalAmount = params.get("total_amount");
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 执行充值加积分/VIP及状态变更业务（内部保证幂等性）
                payOrderService.completeOrderPay(outTradeNo, totalAmount, params.get("trade_no"));
            }
            return "success"; // 成功后必须原样返回纯文本 success
        } catch (Exception e) {
            log.error("处理支付宝回调异常", e);
            return "fail";
        }
    }

    @GetMapping("/history")
    public Result<PageResult<OrderPageRespDTO>> pageOrder(OrderPageReqDTO orderPageReqDTO){
        PageResult<OrderPageRespDTO> pageResult = payOrderService.pageOrder(orderPageReqDTO);
        return Result.success(pageResult);
    }


}
