package com.kun.service.user.dto.resp;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckinStatusRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 今日是否签到
     */
    private Boolean isCheckedToday;
    /**
     * 连续签到天数
     */
    private Integer continuousCheckinDays;
    /**
     * 本月签到信息
     */
    private List<Integer> signedDaysThisMouth;


}
