package com.kun.service.user.dto.resp;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetLogPageRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    private Long id;



    /**
     * 变动类型 (1:签到赠送 2:任务奖励 3:充值到账 4:章节兑换 5:管理员调整)
     */
    private Integer changeType;

    /**
     * 变动类型名称
     */
    private String changeName;

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
     * 变动说明快照 (如"兑换第30章：突破")
     */
    private String title;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
