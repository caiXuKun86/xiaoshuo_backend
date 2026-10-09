package com.kun.service.user.task;


import cn.hutool.core.collection.CollUtil;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.enums.UserVipLevelEnum;
import com.kun.common.core.exception.BusinessException;
import com.kun.service.user.domain.User;
import com.kun.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class BookRankScheduleTask {

    private final UserService userService;
    private static final int BATCH_SIZE = 500; // 每批处理数量

    private TransactionTemplate transactionTemplate;

    /**
     * 每 60 分钟重算并刷新一次排行榜
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void updateUserVipStatus() {
        long lastId = 0L;
        int totalProcessed = 0;
        while (true) {
            // 1. 按主键游标分批拉取数据
            List<User> expiredList = userService.lambdaQuery()
                    .select(User::getId, User::getVipExpireTime)
                    .eq(User::getIsVip, UserVipLevelEnum.VIP.getCode())
                    .le(User::getVipExpireTime, LocalDateTime.now())
                    .gt(User::getId, lastId)
                    .list();
            if (CollUtil.isEmpty(expiredList)) {
                break; // 没有更多数据，退出循环
            }
            // 2. 更新本批次的游标 lastId
            lastId = expiredList.get(expiredList.size() - 1).getId();
            // 3. 独立小事务处理本批次（更新 DB + 投递通知事件）
            try {
                List<Long> userIds = expiredList.stream().map(User::getId).toList();
                transactionTemplate.executeWithoutResult(status -> {
                    boolean update = userService.lambdaUpdate()
                            .set(User::getIsVip, UserVipLevelEnum.NORMAL.getCode()) // 生成 SET is_vip = 0
                            .eq(User::getIsVip, UserVipLevelEnum.VIP.getCode())
                            .in(User::getId, userIds)                           // 生成 WHERE id IN (...)
                            .update();
                    if (!update) {
                        throw new BusinessException(ResultCode.OPERATION_FAILED);
                    }
                });


                totalProcessed += expiredList.size();
            } catch (Exception e) {
                log.error("处理批次异常, lastId: {}", lastId, e);
                // 遇到单批次异常，可选择告警并继续下一批，防止整个任务中断
            }
            // 4. 友好的流控：短暂休眠 20~50ms，释放 CPU 与数据库 IO
            try {
                Thread.sleep(30);
            } catch (InterruptedException ignored) {
            }
        }
        log.info("VIP 过期与挽留任务执行完毕，共处理 {} 条记录", totalProcessed);
    }
}



