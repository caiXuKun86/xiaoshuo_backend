package com.kun.service.pay.task;


import com.kun.service.pay.domain.PayLocalMessage;
import com.kun.service.pay.service.PayLocalMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayLocalMessageTask {


    private final PayLocalMessageService payLocalMessageService;
    private final DefaultMQProducer defaultMQProducer;

    @Scheduled(fixedDelay = 5000)
    public void scanAndPublishMessages() {
        List<PayLocalMessage> messageList = payLocalMessageService.lambdaQuery()
                .eq(PayLocalMessage::getStatus, 0)
                .le(PayLocalMessage::getNextRetryTime, LocalDateTime.now())
                .orderByAsc(PayLocalMessage::getNextRetryTime)
                .last("LIMIT 50")
                .list();
        if (messageList.isEmpty()) {
            return;
        }
        log.info("扫描到待发送本地事务消息 {} 条，开始投递...", messageList.size());
        // 2. 遍历逐条投递
        for (PayLocalMessage msg : messageList) {
            publishSingleMessage(msg);
        }
    }

    /**
     * 单条消息投递逻辑
     */
    private void publishSingleMessage(PayLocalMessage localMsg) {
        try {
            // 构建 RocketMQ Message
            // key 使用 bizOrderNo，便于排查链路与消费端防重
            Message mqMessage = new Message(
                    localMsg.getTopic(),
                    localMsg.getBizType(),
                    localMsg.getBizOrderNo(),
                    localMsg.getPayload().getBytes(StandardCharsets.UTF_8)
            );
            // 同步发送到 Broker
            SendResult sendResult = defaultMQProducer.send(mqMessage);
            if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                // 投递成功：更新状态为 1: 已发送
                localMsg.setStatus(1);
                localMsg.setUpdateTime(LocalDateTime.now());
                payLocalMessageService.updateById(localMsg);
                log.info("本地消息投递成功, messageId={}, orderNo={}", localMsg.getMessageId(), localMsg.getBizOrderNo());
            } else {
                // 发送状态非 OK（如 FLUSH_DISK_TIMEOUT），走失败重试
                handlePublishFail(localMsg, new RuntimeException("MQ返回状态非OK: " + sendResult.getSendStatus()));
            }
        } catch (Exception e) {
            // 发送异常（如网络超时、Broker不可达），进入重试
            handlePublishFail(localMsg, e);
        }
    }

    private void handlePublishFail(PayLocalMessage localMsg, Exception e) {
        Integer currentRetry = localMsg.getRetryCount();

        int nextRetry = currentRetry + 1;
        log.warn("本地消息投递失败, messageId={}, 当前第 {} 次尝试, 异常原因: {}",
                localMsg.getMessageId(), nextRetry, e.getMessage());

        if (nextRetry >= localMsg.getMaxRetry()) {
            // 超过最大次数（默认5次），标记为 2: 重试超限失败
            localMsg.setStatus(2);
            log.error("【报警】本地消息投递超过最大重试次数！需人工介入, messageId={}, orderNo={}",
                    localMsg.getMessageId(), localMsg.getBizOrderNo());
        } else {
            // 累加重试次数
            localMsg.setRetryCount(nextRetry);
            // 指数退避：计算下次重试时间
            localMsg.setNextRetryTime(calculateNextRetryTime(nextRetry));
        }
        localMsg.setUpdateTime(LocalDateTime.now());
        payLocalMessageService.updateById(localMsg);

    }

    private LocalDateTime calculateNextRetryTime(int retryCount) {
        long delaySeconds = switch (retryCount) {
            case 1 -> 10;
            case 2 -> 30;
            case 3 -> 60;
            case 4 -> 180;
            default -> 600;
        };
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
