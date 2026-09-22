package com.kun.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 跨服务用户积分变更传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "跨服务积分变更传输对象")
public class UserPointsUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "积分变动数值 (增加为正 +20，扣减为负 -10)")
    private Long balanceChange;

    @Schema(description = "变动类型 (1:签到 2:任务 3:充值 4:章节兑换 5:管理员调整)")
    private Integer changeType;

    @Schema(description = "业务关联 ID (如 chapter_id, task_id)")
    private Long bizId;

    @Schema(description = "变动说明摘要")
    private String title;

    @Schema(description = "防重幂等键 (选填)")
    private String idempotentKey;
}
