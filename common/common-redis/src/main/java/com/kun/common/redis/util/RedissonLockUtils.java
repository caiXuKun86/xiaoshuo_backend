package com.kun.common.redis.util;

import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redisson 分布式锁工具类
 * 用于防重幂等、充值下单、积分扣减、并发兑换等高并发场景
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonLockUtils {

    private final RedissonClient redissonClient;

    /**
     * 获取分布式锁实例
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    /**
     * 尝试获取锁
     *
     * @param lockKey   锁的 key
     * @param waitTime  最大等待时间
     * @param leaseTime 持锁超时释放时间 (-1 开启看门狗看护机制)
     * @param unit      时间单位
     * @return 是否加锁成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁中断异常 lockKey: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放锁 (具备安全校验：仅当当前线程持有该锁时才释放)
     */
    public void unlock(String lockKey) {
        RLock lock = getLock(lockKey);
        unlock(lock);
    }

    /**
     * 释放指定的 RLock 对象
     */
    public void unlock(RLock lock) {
        if (lock != null && lock.isLocked() && lock.isHeldByCurrentThread()) {
            try {
                lock.unlock();
            } catch (Exception e) {
                log.warn("释放分布式锁异常", e);
            }
        }
    }

    /**
     * 带返回值的分布式锁模板封装执行
     *
     * @param lockKey   锁的 key
     * @param waitTime  最大等待时间
     * @param leaseTime 持锁自动释放时间
     * @param unit      时间单位
     * @param task      业务逻辑
     * @param <T>       返回值泛型
     * @return 业务执行结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> task) {
        RLock lock = getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                throw new BusinessException(ResultCode.IDEMPOTENT_REPEATED);
            }
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ResultCode.IDEMPOTENT_REPEATED.getCode(), "获取锁被中断，请重试");
        } finally {
            if (acquired) {
                unlock(lock);
            }
        }
    }

    /**
     * 无返回值的分布式锁模板封装执行
     */
    public void executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Runnable task) {
        executeWithLock(lockKey, waitTime, leaseTime, unit, () -> {
            task.run();
            return null;
        });
    }
}
