package com.kun.service.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.service.pay.domain.PayLocalMessage;
import com.kun.service.pay.service.PayLocalMessageService;
import com.kun.service.pay.mapper.PayLocalMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【pay_local_message(可靠事务本地消息表)】的数据库操作Service实现
* @createDate 2026-10-09 08:48:04
*/
@Service
public class PayLocalMessageServiceImpl extends ServiceImpl<PayLocalMessageMapper, PayLocalMessage>
    implements PayLocalMessageService{

}




