package com.kun.service.user.dto.resp;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCheckinRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 签到获得积分数
     */
    private Integer pointsAwarded;
    /**
     * 签到后积分数
     */
    private Integer newPointBalance;


}
