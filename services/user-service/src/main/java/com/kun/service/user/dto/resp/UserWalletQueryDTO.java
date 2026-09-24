package com.kun.service.user.dto.resp;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserWalletQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 积分数量
     */
    private Integer pointBalance;
    /**
     * Vip过期时间
     */
    private LocalDateTime vipExpireTime;
    /**
     * Vip剩余天数
     */
    private Integer vipDaysRemaining;


}
