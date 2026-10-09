package com.kun.service.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kun.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 充值套餐与商品SKU配置表
 * @TableName recharge_sku
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="recharge_sku")
@Data
public class RechargeSku extends BaseEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 套餐主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 套餐唯一标识编码 (如 POINT_PKG_30, VIP_MONTH)
     */
    private String skuCode;

    /**
     * 套餐显示名称
     */
    private String name;

    /**
     * 套餐类型 (1:积分包 2:VIP会员卡)
     */
    private Integer skuType;

    /**
     * 原价 (单位: 分，用于划线价展示)
     */
    private Integer originalPrice;

    /**
     * 实际售价 (单位: 分，用户应付金额)
     */
    private Integer actualPrice;

    /**
     * 基础到账积分数
     */
    private Integer pointsAmount;

    /**
     * 运营额外赠送积分数
     */
    private Integer extraPoints;

    /**
     * 增加的 VIP 有效天数 (月卡30, 季卡90, 年卡365)
     */
    private Integer vipDays;

    /**
     * 角标文案 (如 "热销首选", "限时赠送")
     */
    private String badgeTag;

    /**
     * 展示排序权重 (越小越靠前)
     */
    private Integer sort;

    /**
     * 上架状态 (0:下架 1:上架中)
     */
    private Integer status;



}