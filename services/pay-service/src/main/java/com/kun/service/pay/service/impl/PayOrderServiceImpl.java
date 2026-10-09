package com.kun.service.pay.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.OrderStatusEnum;
import com.kun.common.core.enums.PayChannelEnum;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.enums.SkuStatusEnum;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.database.page.PageResult;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.service.pay.config.AlipayProperties;
import com.kun.service.pay.domain.PayFlow;
import com.kun.service.pay.domain.PayLocalMessage;
import com.kun.service.pay.domain.PayOrder;
import com.kun.service.pay.domain.RechargeSku;
import com.kun.service.pay.dto.req.OrderCreateReqDTO;
import com.kun.service.pay.dto.req.OrderPageReqDTO;
import com.kun.service.pay.dto.resp.OrderCreateRespDTO;
import com.kun.service.pay.dto.resp.OrderPageRespDTO;
import com.kun.service.pay.dto.resp.OrderStatusQueryRespDTO;
import com.kun.service.pay.mapper.PayFlowMapper;
import com.kun.service.pay.mapper.PayLocalMessageMapper;
import com.kun.service.pay.mapper.PayOrderMapper;
import com.kun.service.pay.mapper.RechargeSkuMapper;
import com.kun.service.pay.mq.message.PayOrderSuccessEvent;
import com.kun.service.pay.service.PayOrderService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Lenovo
 * @description 针对表【pay_order(充值交易订单主表)】的数据库操作Service实现
 * @createDate 2026-10-09 08:48:04
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder>
        implements PayOrderService {

    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;
    private final RechargeSkuMapper skuMapper;
    private final RedissonClient redissonClient;
    private final PayFlowMapper payFlowMapper;
    private final PayLocalMessageMapper payLocalMessageMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultMQProducer defaultMQProducer;

    @Lazy
    @Resource
    private PayOrderServiceImpl self;

    @Override
    public OrderCreateRespDTO createOrderAndPay(OrderCreateReqDTO orderCreateReqDTO) {
        Long skuId = orderCreateReqDTO.getSkuId();
        Long userId = UserContextHolder.getUserId();
        Integer payChannel = orderCreateReqDTO.getPayChannel();
        String payScene = orderCreateReqDTO.getPayScene();

        RechargeSku rechargeSku = skuMapper.selectById(skuId);
        if (rechargeSku == null || rechargeSku.getStatus().equals(SkuStatusEnum.OFF_SALE.getCode())) {
            throw new BusinessException(ResultCode.PAY_SKU_NOT_FOUND);
        }

        PayOrder payOrder = new PayOrder();
        // 2. 生成全局唯一订单号 (REC + 时间戳 + 随机数)
        String orderNo = "REC" + System.currentTimeMillis() + RandomUtil.randomNumbers(6);
        payOrder.setOrderNo(orderNo);
        payOrder.setUserId(userId);
        payOrder.setSkuId(skuId);
        payOrder.setSkuType(rechargeSku.getSkuType());
        payOrder.setSkuName(rechargeSku.getName());
        payOrder.setOrderAmount(rechargeSku.getOriginalPrice());
        payOrder.setPayAmount(rechargeSku.getActualPrice());
        payOrder.setPointsAmount(rechargeSku.getPointsAmount());
        payOrder.setExtraPoints(rechargeSku.getExtraPoints());
        payOrder.setVipDays(rechargeSku.getVipDays());
        payOrder.setPayChannel(payChannel);
        payOrder.setOrderStatus(OrderStatusEnum.PENDING.getCode());
        payOrder.setExpireTime(LocalDateTime.now().plusMinutes(15));
        boolean save = this.save(payOrder);
        if (!save) {
            throw new BusinessException(ResultCode.ORDER_CREATE_FAILED);
        }

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();

        request.setReturnUrl(alipayProperties.getReturnUrl());
        request.setNotifyUrl(alipayProperties.getNotifyUrl());

        // 设置商户订单号
        model.setOutTradeNo(orderNo);

        // 设置订单总金额
        BigDecimal amountInYuan = BigDecimal.valueOf(rechargeSku.getActualPrice())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        model.setTotalAmount(amountInYuan.toString());

        // 设置订单标题
        model.setSubject(rechargeSku.getName());

        // 设置产品码
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        // 与订单失效时间15分钟保持一致
        model.setTimeoutExpress("15m");

        request.setBizModel(model);

        OrderCreateRespDTO respDTO;
        try {
            // 5. 执行调用生成 PC 端 HTML 表单
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (!response.isSuccess()) {
                throw new BusinessException(ResultCode.ORDER_CREATE_FAILED);
            }
            String formHtml = response.getBody();
            // 6. 组装响应 DTO 返回给前端
            respDTO = new OrderCreateRespDTO();
            respDTO.setOrderNo(orderNo);
            respDTO.setOrderAmount(payOrder.getOrderAmount());
            respDTO.setPayChannel(payChannel);
            respDTO.setExpireTime(payOrder.getExpireTime());
            respDTO.setExpireSecond(15 * 60);

            // 将生成的 HTML 表单放入 payParams 返回
            Map<String, String> payParams = new HashMap<>();
            payParams.put("form", formHtml);
            respDTO.setPayParams(payParams);

        } catch (AlipayApiException e) {
            log.error("调用支付宝电脑网站支付接口异常, orderNo={}", orderNo, e);
            throw new BusinessException(ResultCode.ORDER_CREATE_FAILED);
        }
        sendOrderDelayCloseMessage(orderNo);


        return respDTO;


    }

    private void sendOrderDelayCloseMessage(String orderNo) {
        Message message = new Message(
                "pay_topic",
                "tag_order_timeout",
                orderNo,
                orderNo.getBytes(StandardCharsets.UTF_8)
        );
        message.setDelayTimeSec(15 * 60);
        try {
            SendResult sendResult = defaultMQProducer.send(message);
            if (!sendResult.getSendStatus().equals(SendStatus.SEND_OK)) {
                log.error("发送超时关单延迟消息失败, orderNo={}", orderNo);
            }

        } catch (Exception e) {
            log.error("发送超时关单延迟消息失败, orderNo={}", orderNo, e);
        }
    }

    @Override
    public void completeOrderPay(String orderNo, String totalAmountStr, String channelTradeNo) {
        String lockKey = String.format(RedisKeyConstants.LOCK_PAY_ORDER_COMPLETE, orderNo);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，最多等待 3 秒，上锁后 10 秒自动释放
            boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                return;
            }
            // 执行核心事务
            self.doCompleteOrderPay(orderNo, totalAmountStr, channelTradeNo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("分布式锁加锁被打断, orderNo={}", orderNo, e);
            throw new BusinessException(ResultCode.REQUEST_RATE_LIMIT);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public OrderStatusQueryRespDTO queryOrderStatus(String orderNo) {
        // 1. 查询本地订单
        PayOrder payOrder = this.lambdaQuery()
                .eq(PayOrder::getOrderNo, orderNo)
                .one();
        if (payOrder == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2. 如果本地状态已经不是“待支付”（已支付 / 已取消 / 失败），直接返回，绝不调三方
        if (!OrderStatusEnum.PENDING.getCode().equals(payOrder.getOrderStatus())) {
            return buildOrderStatusQueryRespDTO(payOrder);
        }
        if (shouldCheckAlipay(payOrder)) {
            syncStatusFromAlipay(payOrder);
            // 重新查一次本地最新的状态
            payOrder = this.getById(payOrder.getId());
        }
        return buildOrderStatusQueryRespDTO(payOrder);


    }

    @Override
    public void closeTimeoutOrder(String orderNo) {
        String lockKey = String.format(RedisKeyConstants.LOCK_PAY_ORDER_COMPLETE, orderNo);
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取关单锁失败，当前可能有回调在处理, orderNo={}", orderNo);
                return;
            }

            PayOrder payOrder = this.lambdaQuery()
                    .eq(PayOrder::getOrderNo, orderNo)
                    .one();
            // 如果已经不是待支付状态（已支付/已取消），直接幂等返回
            if (!OrderStatusEnum.PENDING.getCode().equals(payOrder.getOrderStatus())) {
                log.info("订单已处于终态，无需关单, orderNo={}, status={}", orderNo, payOrder.getOrderStatus());
                return;
            }
            boolean isPaid = checkAlipayIsPaid(orderNo);
            if (isPaid) {
                log.warn("订单在超时临界点已付款成功，放弃关单！转入正常入账流程, orderNo={}", orderNo);
                return;
            }
            closeAlipayTrade(orderNo);
            // 5. 【CAS 乐观更新本地订单状态为已取消 (2)】
            boolean updated = this.lambdaUpdate()
                    .set(PayOrder::getOrderStatus, OrderStatusEnum.CANCELLED.getCode())
                    .eq(PayOrder::getOrderNo, orderNo)
                    .eq(PayOrder::getOrderStatus, OrderStatusEnum.PENDING.getCode()) // CAS 兜底条件
                    .update();
            if (updated) {
                log.info("超时未支付订单关闭成功, orderNo={}", orderNo);
            }


        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("关单加锁被打断, orderNo={}", orderNo, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public PageResult<OrderPageRespDTO> pageOrder(OrderPageReqDTO reqDTO) {

        Long userId = UserContextHolder.getUserId();
        LambdaQueryWrapper<PayOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PayOrder::getUserId, userId);
        // 5. 排序规则：若未传自定义排序，默认按更新时间倒序
        if (!StringUtils.hasText(reqDTO.getSortField())) {
            queryWrapper.orderByDesc(PayOrder::getCreateTime);
        }

        // 6. 执行分页查询
        Page<PayOrder> page = this.page(reqDTO.toPage(), queryWrapper);


        return PageResult.of(page, payOrder -> {
            OrderPageRespDTO orderPageRespDTO = new OrderPageRespDTO();
            BeanUtil.copyProperties(payOrder,orderPageRespDTO);
            orderPageRespDTO.setPayChannelName(PayChannelEnum.getByCode(payOrder.getPayChannel()).getDescription());
            orderPageRespDTO.setOrderStatusDesc(OrderStatusEnum.getByCode(payOrder.getOrderStatus()).getDescription());
            return orderPageRespDTO;
        });
    }

    private void closeAlipayTrade(String orderNo) {
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(orderNo);
        request.setBizModel(model);
        try {

            AlipayTradeCloseResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                // 如果返回 ACQ.TRADE_NOT_EXIST，说明用户根本没扫过码、支付宝端无交易，属于正常情况
                log.info("支付宝关单返回: subCode={}, subMsg={}", response.getSubCode(), response.getSubMsg());
            }
        } catch (Exception e) {
            log.warn("调用支付宝统一收单交易关闭接口异常, orderNo={}", orderNo, e);
        }
    }

    private boolean checkAlipayIsPaid(String orderNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(orderNo);
        request.setBizModel(model);
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                // 支付宝返回已支付成功，但本地异步回调可能丢失/延迟，立即触发本地完成入账
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    this.completeOrderPay(
                            orderNo,
                            response.getTotalAmount(),
                            response.getTradeNo()
                    );
                    return true;
                }
            }
        } catch (Exception e){
            log.warn("查单异常, orderNo={}", orderNo, e);
        }
        return false;

    }

    private void syncStatusFromAlipay(PayOrder payOrder) {

        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(payOrder.getOrderNo());
            request.setBizModel(model);
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                // 支付宝返回已支付成功，但本地异步回调可能丢失/延迟，立即触发本地完成入账
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    this.completeOrderPay(
                            payOrder.getOrderNo(),
                            response.getTotalAmount(),
                            response.getTradeNo()
                    );
                }
            } else if ("ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())) {
                // 用户在支付宝页面压根没扫码/没创建交易，正常现象，保持待支付即可
                log.debug("支付宝交易尚未创建: {}", payOrder.getOrderNo());
            }
        } catch (Exception e) {
            log.warn("向支付宝主动查单异常(网络波动忽略), orderNo: {}", payOrder.getOrderNo(), e);
        }


    }

    private OrderStatusQueryRespDTO buildOrderStatusQueryRespDTO(PayOrder payOrder) {
        OrderStatusQueryRespDTO dto = new OrderStatusQueryRespDTO();
        BeanUtil.copyProperties(payOrder, dto);
        String description = OrderStatusEnum.getByCode(payOrder.getOrderStatus()).getDescription();
        dto.setOrderStatusDesc(description);
        dto.setPointsAmount(payOrder.getPointsAmount() + payOrder.getExtraPoints());
        return dto;
    }

    private boolean shouldCheckAlipay(PayOrder payOrder) {
        // 刚下单 10 秒之内，不查支付宝（99%的用户还在掏手机或输密码，查也是浪费）
        if (payOrder.getCreateTime().plusSeconds(10).isAfter(LocalDateTime.now())) {
            return false;
        }
        // 利用 Redis 控制对同一笔订单查询支付宝的频次，至少间隔 5 秒才允许查一次支付宝
        String rateLimitKey = String.format(RedisKeyConstants.RATE_PAY_QUERY_ORDER, payOrder.getOrderNo());
        Boolean b = stringRedisTemplate.opsForValue().setIfAbsent(rateLimitKey, "1", 5, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(b);
    }

    @Transactional(rollbackFor = Exception.class)
    public void doCompleteOrderPay(String orderNo, String totalAmountStr, String channelTradeNo) {
        PayOrder payOrder = this.lambdaQuery()
                .eq(PayOrder::getOrderNo, orderNo)
                .one();
        if (payOrder == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        // 如果状态已经是支付成功，说明已被处理过，直接幂等返回
        if (OrderStatusEnum.SUCCESS.getCode().equals(payOrder.getOrderStatus())) {
            return;
        }
        BigDecimal notifyAmountYuan = new BigDecimal(totalAmountStr);
        int notifyAmountCent = notifyAmountYuan.multiply(new BigDecimal(100)).intValue();
        if (!payOrder.getPayAmount().equals(notifyAmountCent)) {
            throw new BusinessException(ResultCode.ORDER_AMOUNT_NOT_EQUAL);
        }
        boolean updated = this.lambdaUpdate()
                .set(PayOrder::getOrderStatus, OrderStatusEnum.SUCCESS.getCode())
                .set(PayOrder::getPaySuccessTime, LocalDateTime.now())
                .eq(PayOrder::getOrderNo, orderNo)
                .eq(PayOrder::getOrderStatus, OrderStatusEnum.PENDING.getCode()) // CAS 核心条件
                .update();
        if (!updated) {
            return;
        }
        PayFlow payFlow = new PayFlow();
        payFlow.setFlowNo("FLOW" + System.currentTimeMillis() + RandomUtil.randomNumbers(6));
        payFlow.setOrderNo(orderNo);
        payFlow.setUserId(payOrder.getUserId());
        payFlow.setPayChannel(payOrder.getPayChannel());
        payFlow.setChannelTradeNo(channelTradeNo);
        payFlow.setPayAmount(payOrder.getPayAmount());
        payFlow.setFlowStatus(1); // 1: 支付成功
        payFlow.setCallbackTime(LocalDateTime.now());
        payFlowMapper.insert(payFlow);

        PayLocalMessage message = new PayLocalMessage();
        message.setMessageId("MSG_RECHARGE_" + orderNo); // 强规则唯一 ID
        message.setBizType("tag_order_pay_success");
        message.setBizOrderNo(orderNo);
        message.setTopic("pay_topic");

        // 构造发送给 user-service 加积分/加VIP 的消息体
        PayOrderSuccessEvent event = new PayOrderSuccessEvent();

        event.setUserId(payOrder.getUserId());
        event.setSkuType(payOrder.getSkuType());
        event.setPoints(payOrder.getPointsAmount() + payOrder.getExtraPoints());
        event.setVipDays(payOrder.getVipDays());
        event.setOrderNo(orderNo);


        message.setPayload(JSONUtil.toJsonStr(event));

        message.setStatus(0); // 0: 待发送
        message.setRetryCount(0);
        message.setMaxRetry(5);
        message.setNextRetryTime(LocalDateTime.now());
        payLocalMessageMapper.insert(message);
    }


}




