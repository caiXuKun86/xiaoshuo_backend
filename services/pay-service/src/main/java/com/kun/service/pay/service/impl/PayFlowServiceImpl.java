package com.kun.service.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.service.pay.domain.PayFlow;
import com.kun.service.pay.service.PayFlowService;
import com.kun.service.pay.mapper.PayFlowMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【pay_flow(第三方支付渠道流水表)】的数据库操作Service实现
* @createDate 2026-10-09 08:48:04
*/
@Service
public class PayFlowServiceImpl extends ServiceImpl<PayFlowMapper, PayFlow>
    implements PayFlowService{

}




