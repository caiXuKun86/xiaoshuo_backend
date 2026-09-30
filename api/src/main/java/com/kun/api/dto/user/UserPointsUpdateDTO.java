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

    private Long userId;

    private Integer balanceChange;

    private Integer changeType;

    private Long bizId;
    private String orderNo;

    private String title;
    private Integer balanceBefore;
    private Integer balanceAfter;


}
