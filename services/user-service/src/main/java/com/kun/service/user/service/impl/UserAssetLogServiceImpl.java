package com.kun.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
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
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lenovo
 * @description 针对表【user_asset_log(用户资产与积分变动流水表)】的数据库操作Service实现
 * @createDate 2026-09-23 09:13:28
 */
@RequiredArgsConstructor
@Service
public class UserAssetLogServiceImpl extends ServiceImpl<UserAssetLogMapper, UserAssetLog> implements UserAssetLogService {

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    private final SignRewardStrategyFactory strategyFactory;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCheckinRespDTO userCheckin() {
        LocalDate date = LocalDate.now();
        int dayOfMonth = date.getDayOfMonth();
        Long userId = UserContextHolder.getUserId();

        // 每日连续签到打卡 BitMap Key: user:signin:{userId}:{yyyyMM}
        String key = RedisKeyConstants.USER_SIGNIN_BITMAP_PREFIX + String.format("%d:%s", userId, date.format(DateTimeFormatter.ofPattern("yyyyMM")));
        try {
            Boolean b = stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
            if (!b) {
                throw new BusinessException(ResultCode.ALREADY_CHECKED_IN);
            }
            //查询用户数据库行级悲观锁（FOR UPDATE）
            User user = userMapper.selectByUserIdForUpdate(userId);
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }
            //根据用户是否是VIP采取不同的签到策略
            SignRewardStrategy strategy = strategyFactory.getStrategy(VipLevelEnum.of(user.getIsVip()));
            int pointsAwarded = strategy.calculatePoints();

            Integer newPointBalance = user.getPointBalance() + pointsAwarded;
            int update = userMapper.update(new LambdaUpdateWrapper<User>().set(User::getPointBalance, newPointBalance).eq(User::getId, userId));
            if (update < 1) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "更新积分失败");
            }
            UserAssetLog userAssetLog = new UserAssetLog();
            userAssetLog.setUserId(userId);
            userAssetLog.setChangeType(AssetChangeTypeEnum.CHECKIN_REWARD.getCode());
            userAssetLog.setBalanceChange(pointsAwarded);
            userAssetLog.setBalanceBefore(user.getPointBalance());
            userAssetLog.setBalanceAfter(newPointBalance);
            userAssetLog.setTitle("每日签到");

            boolean save = this.save(userAssetLog);
            if (!save) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "添加记录失败");
            }
            return new UserCheckinRespDTO(pointsAwarded, newPointBalance);
        } catch (Exception e) {
            stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, false);
            throw new BusinessException(ResultCode.SYSTEM_ERROR);
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




