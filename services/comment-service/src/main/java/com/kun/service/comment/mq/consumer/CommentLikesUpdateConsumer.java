package com.kun.service.comment.mq.consumer;

import cn.hutool.core.collection.CollUtil;
import com.kun.service.comment.mq.event.CommentLikeUpdateEvent;
import com.kun.service.comment.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;

import java.util.List;

@Slf4j
//@Component
@Deprecated
//@RocketMQMessageListener(
//        topic = "comment-topic",
//        consumerGroup = "comment-commentLike-update-consumer-group",
//        consumeMode = ConsumeMode.ORDERLY,
//        selectorExpression = "tag-commentLike-update")
@RequiredArgsConstructor
public class CommentLikesUpdateConsumer implements RocketMQListener<List<CommentLikeUpdateEvent>>, RocketMQPushConsumerLifecycleListener {

    private final CommentLikeService commentLikeService;

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        // 设置每次批量消费的最大消息数 (例如攒够 64 条或达到超时时间推过来)
        consumer.setConsumeMessageBatchMaxSize(200);
    }

    @Override
    public void onMessage(List<CommentLikeUpdateEvent> events) {

        if (CollUtil.isEmpty(events)) {
            return;
        }
        try {
            // 批量处理
            commentLikeService.processBatch(events);
        } catch (Exception e) {
            log.error("批量消费点赞消息失败, size: {}", events.size(), e);
            // 抛出异常触发重试机制
            throw e;
        }


    }
}