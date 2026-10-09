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
 * 充值交易订单主表
 * @TableName pay_order
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="pay_order")
@Data
public class PayOrder extends BaseEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 主键 ID (雪花算法)
     */
    @TableId
    private Long id;

    /**
     * 平台全局唯一业务订单号 (REC+时间戳+机器码+随机数)
     */
    private String orderNo;

    /**
     * 下单充值用户 ID
     */
    private Long userId;

    /**
     * 关联充值套餐 ID
     */
    private Long skuId;

    /**
     * 套餐类型 (1:积分包 2:VIP会员卡)
     */
    private Integer skuType;

    /**
     * 套餐名称快照
     */
    private String skuName;

    /**
     * 订单应付金额 (单位: 分)
     */
    private Integer orderAmount;

    /**
     * 最终实付金额 (单位: 分)
     */
    private Integer payAmount;

    /**
     * 订单基础到账积分快照
     */
    private Integer pointsAmount;

    /**
     * 订单赠送积分快照
     */
    private Integer extraPoints;

    /**
     * 对应 VIP 天数快照
     */
    private Integer vipDays;

    /**
     * 支付渠道 (0:未选择 1:微信支付[预留] 2:支付宝[首发支持] 3:苹果IAP[预留])
     */
    private Integer payChannel;

    /**
     * 订单状态 (0:待支付 1:支付成功 2:已取消 3:失败；当前不支持退款)
     */
    private Integer orderStatus;

    /**
     * 订单支付截止失效时间 (创建后15分钟)
     */
    private LocalDateTime expireTime;

    /**
     * 第三方回调写入的支付成功时间
     */
    private LocalDateTime paySuccessTime;


}