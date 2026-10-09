package com.kun.service.pay.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kun.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 可靠事务本地消息表
 * @TableName pay_local_message
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="pay_local_message")
@Data
public class PayLocalMessage extends BaseEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 主键 ID (雪花算法)
     */
    @TableId
    private Long id;

    /**
     * 消息全局唯一 ID (防重幂等消费)
     */
    private String messageId;

    /**
     * 业务类型 (如 ORDER_PAY_SUCCESS)
     */
    private String bizType;

    /**
     * 关联业务订单号 (order_no)
     */
    private String bizOrderNo;

    /**
     * 目标 MQ Topic 名称
     */
    private String topic;

    /**
     * 消息体内容 (JSON 格式：包含 userId, points, vipDays, orderNo 等)
     */
    private String payload;

    /**
     * 发送状态 (0:待发送 1:已发送 2:重试超限失败 3:死信)
     */
    private Integer status;

    /**
     * 已重试投递次数
     */
    private Integer retryCount;

    /**
     * 最大允许重试次数
     */
    private Integer maxRetry;

    /**
     * 下次重试投递时间 (指数退避算法)
     */
    private LocalDateTime nextRetryTime;


}