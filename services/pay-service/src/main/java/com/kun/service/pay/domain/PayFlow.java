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
 * 第三方支付渠道流水表
 * @TableName pay_flow
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="pay_flow")
@Data
public class PayFlow extends BaseEntity {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 主键 ID (雪花算法)
     */
    @TableId
    private Long id;

    /**
     * 内部支付流水号
     */
    private String flowNo;

    /**
     * 关联的平台业务订单号
     */
    private String orderNo;

    /**
     * 付款用户 ID
     */
    private Long userId;

    /**
     * 支付渠道 (1:微信支付[预留] 2:支付宝[当前首发实现] 3:苹果IAP[预留])
     */
    private Integer payChannel;

    /**
     * 第三方渠道外部交易号 (如支付宝 trade_no / 预留微信 transaction_id)
     */
    private String channelTradeNo;

    /**
     * 渠道实际扣款金额 (单位: 分)
     */
    private Integer payAmount;

    /**
     * 付款人账号标识 (如支付宝 buyer_id / 预留微信 openid)
     */
    private String payerAccount;

    /**
     * 流水状态 (0:发起中 1:支付成功 2:支付失败；当前不支持退款，3:已退款预留)
     */
    private Integer flowStatus;

    /**
     * 第三方回调通知时间
     */
    private LocalDateTime callbackTime;

    /**
     * 第三方异步回调原始报文 (JSON/表单快照，存底审计)
     */
    private String notifyPayload;




}