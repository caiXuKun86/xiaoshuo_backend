package com.kun.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.AssetChangeTypeEnum;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.enums.VipLevelEnum;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.database.page.PageResult;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.service.user.domain.User;
import com.kun.service.user.domain.UserAssetLog;
import com.kun.service.user.dto.req.AssetLogsPageReqDTO;
import com.kun.service.user.dto.resp.AssetLogPageRespDTO;
import com.kun.service.user.dto.resp.CheckinStatusRespDTO;
import com.kun.service.user.dto.resp.UserCheckinRespDTO;
import com.kun.service.user.dto.resp.UserWalletQueryDTO;
import com.kun.service.user.mapper.UserAssetLogMapper;
import com.kun.service.user.mapper.UserMapper;
import com.kun.service.user.service.UserAssetLogService;
import com.kun.service.user.stragety.signReward.SignRewardStrategy;
import com.kun.service.user.stragety.signReward.SignRewardStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Lenovo
 * @description 针对表【user_asset_log(用户资产与积分变动流水表)】的数据库操作Service实现
 * @createDate 2026-09-23 09:13:28
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class UserAssetLogServiceImpl extends ServiceImpl<UserAssetLogMapper, UserAssetLog> implements UserAssetLogService {

    private final StringRedisTemplate stringRedisTemplate;



    private final SignRewardStrategyFactory strategyFactory;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCheckinRespDTO userCheckin() {
        LocalDate date = LocalDate.now();
        int dayOfMonth = date.getDayOfMonth();
        Long userId = UserContextHolder.getUserId();

        // 每日连续签到打卡 BitMap Key: user:signin:{userId}:{yyyyMM}
        String key = RedisKeyConstants.USER_SIGNIN_BITMAP_PREFIX
                + String.format("%d:%s", userId, date.format(DateTimeFormatter.ofPattern("yyyyMM")));

        // 1. 原子操作打卡：SETBIT 返回的是该位置被设置前的值 (旧值)
        Boolean alreadyCheckedIn = stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);

        // 如果旧值就是 true (1)，说明今日已签到过，直接拦截，绝对不能回滚 Redis！
        if (Boolean.TRUE.equals(alreadyCheckedIn)) {
            throw new BusinessException(ResultCode.ALREADY_CHECKED_IN);
        }

        // 顺手兜底设置 90 天过期，防止长期无用 key 撑爆内存 (若是新创建的 key)
        stringRedisTemplate.expire(key, 90, TimeUnit.DAYS);

        try {
            // 2. 查询用户数据库行级悲观锁 (FOR UPDATE)
            User user = userMapper.selectByUserIdForUpdate(userId);
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }

            // 3. 根据是否是 VIP 执行策略计算积分
            SignRewardStrategy strategy = strategyFactory.getStrategy(VipLevelEnum.of(user.getIsVip()));
            int pointsAwarded = strategy.calculatePoints();
            int newPointBalance = user.getPointBalance() + pointsAwarded;

            // 4. 更新积分余额
            int update = userMapper.update(null,
                    new LambdaUpdateWrapper<User>()
                            .set(User::getPointBalance, newPointBalance)
                            .eq(User::getId, userId));
            if (update < 1) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "更新用户积分余额失败");
            }

            // 5. 记录资产变动明细流水
            UserAssetLog userAssetLog = new UserAssetLog();
            userAssetLog.setUserId(userId);
            userAssetLog.setChangeType(AssetChangeTypeEnum.CHECKIN_REWARD.getCode());
            userAssetLog.setBalanceChange(pointsAwarded);
            userAssetLog.setBalanceBefore(user.getPointBalance());
            userAssetLog.setBalanceAfter(newPointBalance);
            userAssetLog.setTitle("每日签到奖励");

            boolean save = this.save(userAssetLog);
            if (!save) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "记录签到流水失败");
            }

            return new UserCheckinRespDTO(pointsAwarded, newPointBalance);

        } catch (Exception e) {
            // 6. 异常补偿机制：只有在后续业务逻辑抛异常时，才将今天的签到位补偿重置为 false
            stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, false);
            log.error("用户签到事务执行失败，已补偿回滚 Redis 签到状态, userId: {}, key: {}, 原因: {}", userId, key, e.getMessage(), e);

            // 7. 必须重新往外抛出异常！
            // 业务异常直接往外抛，系统未知异常包装后往外抛，确保 @Transactional 回滚生效
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "签到失败，请稍后重试");
        }
    }

    @Override
    public CheckinStatusRespDTO queryCheckinStatus() {
        LocalDate date = LocalDate.now();
        int dayOfMonth = date.getDayOfMonth();
        Long userId = UserContextHolder.getUserId();


        String key = RedisKeyConstants.USER_SIGNIN_BITMAP_PREFIX + String.format("%d:%s", userId, date.format(DateTimeFormatter.ofPattern("yyyyMM")));

        Boolean bit = stringRedisTemplate.opsForValue().getBit(key, dayOfMonth - 1);

        // 1. 执行 BITFIELD 命令：BITFIELD key GET u{dayOfMonth} 0
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth))
                        .valueAt(0)
        );
        if (result == null || result.isEmpty() || result.get(0) == null) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "签到信息查询失败");
        }
        int continuousCheckinDays = 0;
        long num = result.get(0);

        while (num > 0) {
            if ((num & 1) == 1) {
                continuousCheckinDays++;
                num >>>= 1;
            } else {
                break;
            }
        }
        num = result.get(0);
        List<Integer> signedDaysThisMouth = new ArrayList<>();
        for (int day = 1; day <= dayOfMonth; day++) {
            // 比如 4天里：1号需要右移 3 位，4号(今天)需要右移 0 位
            int shift = dayOfMonth - day;
            if (((num >> shift) & 1) == 1) {
                signedDaysThisMouth.add(day);
            }
        }
        return new CheckinStatusRespDTO(bit,continuousCheckinDays,signedDaysThisMouth);

    }

    @Override
    public UserWalletQueryDTO queryUserWallet() {
        Long userId = UserContextHolder.getUserId();

        User user = userMapper.selectById(userId);
        if(user==null){
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        UserWalletQueryDTO userWalletQueryDTO = new UserWalletQueryDTO();
        userWalletQueryDTO.setPointBalance(user.getPointBalance());
        userWalletQueryDTO.setVipExpireTime(user.getVipExpireTime());
        Integer isVip = user.getIsVip();
        userWalletQueryDTO.setVipDaysRemaining(isVip == 0 ? 0 : (int) ChronoUnit.DAYS.between(LocalDateTime.now(), user.getVipExpireTime()));

        return userWalletQueryDTO;
    }

    @Override
    public PageResult<AssetLogPageRespDTO> pageAssetLogs(AssetLogsPageReqDTO assetLogsPageReqDTO) {
        Long userId = UserContextHolder.getUserId();
        Page<UserAssetLog> page = assetLogsPageReqDTO.toPage();

        LambdaQueryWrapper<UserAssetLog> queryWrapper = new LambdaQueryWrapper<UserAssetLog>()
                .eq(UserAssetLog::getUserId, userId);

        // 若未指定自定义排序字段，默认按创建时间 (create_time) 倒序排列
        if (!StringUtils.hasText(assetLogsPageReqDTO.getSortField())) {
            queryWrapper.orderByDesc(UserAssetLog::getCreateTime);
        }

        Page<UserAssetLog> userAssetLogPage = this.page(page, queryWrapper);

        return PageResult.of(userAssetLogPage, log -> {
            AssetLogPageRespDTO dto = new AssetLogPageRespDTO();
            BeanUtils.copyProperties(log, dto);
            dto.setChangeName(AssetChangeTypeEnum.getDescByCode(log.getChangeType()));
            return dto;
        });
    }
}




