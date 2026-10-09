package com.kun.service.pay.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkuListQueryRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    List<PointDTO> pointPackages;
    List<VipDTO> vipPackages;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PointDTO {
        /**
         * 套餐主键 ID
         */
        private Long skuId;

        /**
         * 套餐唯一标识编码 (如 POINT_PKG_30, VIP_MONTH)
         */
        private String skuCode;

        /**
         * 套餐显示名称
         */
        private String name;


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
        private Long pointsAmount;

        /**
         * 运营额外赠送积分数
         */
        private Long extraPoints;

        /**
         * 角标文案 (如 "热销首选", "限时赠送")
         */
        private String badgeTag;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VipDTO {
        /**
         * 套餐主键 ID
         */
        private Long skuId;

        /**
         * 套餐唯一标识编码 (如 POINT_PKG_30, VIP_MONTH)
         */
        private String skuCode;

        /**
         * 套餐显示名称
         */
        private String name;

        /**
         * 原价 (单位: 分，用于划线价展示)
         */
        private Integer originalPrice;

        /**
         * 实际售价 (单位: 分，用户应付金额)
         */
        private Integer actualPrice;

        /**
         * 增加的 VIP 有效天数 (月卡30, 季卡90, 年卡365)
         */
        private Integer vipDays;

        /**
         * 角标文案 (如 "热销首选", "限时赠送")
         */
        private String badgeTag;


    }


}

