package com.kun.service.pay.mq.comsumer;

import cn.hutool.core.collection.CollUtil;
import com.kun.service.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CloseTimeoutOrderConsumer implements MessageListenerConcurrently, InitializingBean, DisposableBean {

    @Value("${rocketmq.namesrv-addr:127.0.0.1:9876}")
    private String namesrvAddr;

    // 当前消费者专属配置
    private static final String CONSUMER_GROUP = "user-asset-update-consumer-group";
    private static final String TOPIC = "pay_topic";
    private static final String TAG = "tag_order_pay_success";

    private DefaultMQPushConsumer consumer;
    private final PayOrderService payOrderService;


    // 1. Spring 依赖注入完成后自动执行启动逻辑
    @Override
    public void afterPropertiesSet() throws Exception {
        consumer = new DefaultMQPushConsumer(CONSUMER_GROUP);
        consumer.setNamesrvAddr(namesrvAddr);
        consumer.subscribe(TOPIC, TAG);

        // 设置并发线程数
        consumer.setConsumeThreadMin(32);
        consumer.setConsumeThreadMax(32);

        // 设置批量拉取与批量消费
        consumer.setPullBatchSize(1);
        consumer.setConsumeMessageBatchMaxSize(1);

        // 关键：把当前实例 (this) 直接传给监听器
        consumer.registerMessageListener(this);

        consumer.start();
        log.info("【消费者已启动】Topic: {}, Group: {}", TOPIC, CONSUMER_GROUP);
    }

    // 2. 核心业务消费逻辑（原生支持 List<MessageExt> 批量处理）
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> records, ConsumeConcurrentlyContext context) {
        log.info("【收到新消息】数量: {}", records.size());
        if (CollUtil.isEmpty(records)) {
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
        MessageExt messageExt = records.get(0);
        try {
            String orderNo = new String(messageExt.getBody());
            payOrderService.closeTimeoutOrder(orderNo);
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Throwable t) {
            log.error("消费处理异常，触发稍后重试", t);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    // 3. 应用关闭时平滑停机
    @Override
    public void destroy() {
        if (consumer != null) {
            consumer.shutdown();
            log.info("【消费者已平滑关闭】Group: {}", CONSUMER_GROUP);
        }
    }


}