package com.kun.service.user.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kun.common.database.entity.BaseEntity;
import lombok.Data;

/**
 * 用户资产与积分变动流水表
 * @TableName user_asset_log
 */
@TableName(value ="user_asset_log")
@Data
public class UserAssetLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID (雪花算法)
     */
    @TableId
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 变动类型 (1:签到赠送 2:任务奖励 3:充值到账 4:章节兑换 5:管理员调整)
     */
    private Integer changeType;

    /**
     * 变动数额 (增加为正 +20，消耗为负 -10)
     */
    private Integer balanceChange;

    /**
     * 变动前可用余额 (用于资金对账)
     */
    private Integer balanceBefore;

    /**
     * 变动后可用余额 (满足 before + change = after)
     */
    private Integer balanceAfter;

    /**
     * 业务关联 ID (如 chapter_id, task_id)
     */
    private Long bizId;

    /**
     * 关联充值订单号 (若是充值类型)
     */
    private String orderNo;

    /**
     * 变动说明快照 (如"兑换第30章：突破")
     */
    private String title;

    /**
     * 防重幂等键 (防重复加减积分)
     */
    private String idempotentKey;


}