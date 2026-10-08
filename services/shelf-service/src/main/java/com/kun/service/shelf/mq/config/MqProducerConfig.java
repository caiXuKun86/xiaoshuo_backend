package com.kun.service.shelf.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MqProducerConfig {

    @Value("${rocketmq.namesrv-addr}")
    private String namesrvAddr;

    @Value("${rocketmq.producer.group}")
    private String producerGroup;

    @Value("${rocketmq.producer.send-timeout:3000}")
    private int sendTimeout;

    @Value("${rocketmq.producer.retry-times:2}")
    private int retryTimes;

    /**
     * 注册原生 DefaultMQProducer 为 Spring Bean
     * initMethod = "start": Spring 实例化并填充属性后，自动调用 producer.start()
     * destroyMethod = "shutdown": 应用下线时，自动调用 producer.shutdown() 关闭连接
     */
    @Bean(name = "defaultMQProducer", initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer defaultMQProducer() {
        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(namesrvAddr);
        producer.setSendMsgTimeout(sendTimeout);
        producer.setRetryTimesWhenSendFailed(retryTimes);
        
        // 生产环境若跨网段或容器部署，建议显式关闭 VIP 通道
        producer.setVipChannelEnabled(false);

        log.info("【原生 Producer Bean 配置就绪】Group: {}, NameServer: {}", producerGroup, namesrvAddr);
        return producer;
    }
}