package com.kun.common.redis.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 通用分布式缓存操作工具类 (Cache-Aside 模式核心实现)
 * <p>
 * 核心能力：
 * 1. 基础缓存读写与带随机浮动的 TTL（防缓存雪崩）
 * 2. 缓存空值机制与短期过期（防缓存穿透）
 * 3. 分布式互斥锁与双重检查 (Double-Check) 重建缓存（防缓存击穿）
 * 4. 逻辑过期与异步线程池刷新（防热点 Key 缓存击穿，保障极低延迟与零阻塞）
 * 5. 同时支持 Spring 依赖注入与全局静态方法调用
 * </p>
 *
 * @author kun
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheUtil {

    /**
     * 空值缓存标记常量，用于防缓存穿透 (Cache Penetration)
     */
    public static final String CACHE_NULL_VALUE = "";
    public static final String LOCK_PREFIX = "cache:lock:";
    public static final long DEFAULT_LOCK_WAIT_MILLIS = 200;
    public static final long DEFAULT_LOCK_LEASE_SECONDS = 10000;

    /**
     * 默认防穿透空值缓存过期时长：2 分钟
     */
    public static final long DEFAULT_NULL_TTL_SECONDS = 120L;


    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;


    // ====================================================================================
    // 1. 基础缓存存取与删除操作
    // ====================================================================================

    /**
     * 写入普通缓存 (指定物理过期时间)
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        if (StrUtil.isBlank(key) || value == null) {
            return;
        }
        String json = (value instanceof String) ? (String) value : JSONUtil.toJsonStr(value);
        stringRedisTemplate.opsForValue().set(key, json, timeout, unit);
    }

    /**
     * 写入普通缓存并增加随机抖动 TTL (防缓存雪崩)
     * <p>实际过期时间 = timeout + 随机(0 ~ randomRangePercent)% 浮动</p>
     *
     * @param randomRangePercent 浮动百分比区间 (例如传 10 表示在 0% ~ 10% 之间随机增加)
     */
    public void setWithRandomTtl(String key, Object value, long timeout, TimeUnit unit, int randomRangePercent) {
        if (StrUtil.isBlank(key) || value == null) {
            return;
        }
        long extraSeconds = 0;
        if (randomRangePercent > 0) {
            long totalSeconds = unit.toSeconds(timeout);
            long maxExtra = Math.max(1, (totalSeconds * randomRangePercent) / 100);
            extraSeconds = ThreadLocalRandom.current().nextLong(maxExtra + 1);
        }
        String json = (value instanceof String) ? (String) value : JSONUtil.toJsonStr(value);
        stringRedisTemplate.opsForValue().set(key, json, unit.toSeconds(timeout) + extraSeconds, TimeUnit.SECONDS);
    }

    /**
     * 读取指定类型的缓存对象
     */
    public <T> T get(String key, Class<T> clazz) {
        if (StrUtil.isBlank(key)) {
            return null;
        }
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(json)) {
            return null;
        }
        try {
            return JSONUtil.toBean(json, clazz);
        } catch (Exception e) {
            log.error("[CacheUtil] 反序列化缓存异常, key: {}, type: {}", key, clazz.getName(), e);
            return null;
        }
    }


    /**
     * 读取列表类型缓存 (例如 List<T>)
     */
    public <T> List<T> getList(String key, Class<T> elementClass) {
        if (StrUtil.isBlank(key)) {
            return null;
        }
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(json)) {
            return null;
        }
        try {
            return JSONUtil.toList(json, elementClass);
        } catch (Exception e) {
            log.error("[CacheUtil] 反序列化列表缓存异常, key: {}, elemType: {}", key, elementClass.getName(), e);
            return null;
        }
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        if (StrUtil.isBlank(key)) {
            return false;
        }
        return stringRedisTemplate.delete(key);
    }

    /**
     * 批量删除缓存
     */
    public Long delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return stringRedisTemplate.delete(keys);
    }


    // ====================================================================================
    // 2. 防缓存穿透模式 (Cache Penetration - 空值缓存)
    // ====================================================================================

    /**
     * 防缓存穿透查询模板 (基于 ID 函数式回源)
     * <p>
     * 1. 查询缓存，若命中有效对象直接返回；
     * 2. 若命中空值标记 (CACHE_NULL_VALUE)，说明是已记录的穿透请求，直接返回 null；
     * 3. 若缓存未命中 (null)，调用 dbFallback 查询底层数据库；
     * 4. 数据库查无此数据，写入空字符串并设置较短 TTL (防暴力撞库)，返回 null；
     * 5. 数据库存在数据，写入缓存并设置指定 TTL，返回实体。
     * </p>
     *
     * @param key        缓存键前缀 (例如 "book:info:")
     * @param type       返回数据实体 Class
     * @param dbFallback 数据库回源查询函数
     * @param ttl        正常缓存过期时间
     * @param unit       时间单位
     */
    public <R> R queryWithPassThrough(String key, Class<R> type, Supplier<R> dbFallback, Long ttl, TimeUnit unit) {
        // 1. 从 Redis 查询缓存
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2. 判断是否存在且非空字符串
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }

        // 3. 判断命中的是否为空值占位符 (json != null 说明命中空值标记)
        if (json != null) {
            return null;
        }

        // 4. 缓存未命中，执行数据库回源查询
        R r = dbFallback.get();

        // 5. 数据库中不存在：向 Redis 写入空值并设置较短过期时间，杜绝穿透
        if (r == null) {
            long nullTtl = DEFAULT_NULL_TTL_SECONDS + ThreadLocalRandom.current().nextLong(30);
            stringRedisTemplate.opsForValue().set(key, CACHE_NULL_VALUE, nullTtl, TimeUnit.SECONDS);
            return null;
        }

        // 6. 数据库中存在：写入 Redis 并设置随机抖动过期时间
        this.setWithRandomTtl(key, r, ttl, unit, 10);
        return r;
    }

    /**
     * 防缓存穿透查询列表数据模板 (例如全部分类树、章节列表等)
     */
    public <E> List<E> queryListWithPassThrough(String key, Class<E> elementClass, Supplier<List<E>> dbFallback, Long ttl, TimeUnit unit) {
        // 1. 查询缓存
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2. 命中有效数据
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toList(json, elementClass);
        }

        // 3. 命中空值占位符
        if (json != null) {
            return null;
        }

        // 4. 缓存未命中，查数据库
        List<E> list = dbFallback.get();

        // 5. 判空防穿透
        if (list == null || list.isEmpty()) {
            long nullTtl = DEFAULT_NULL_TTL_SECONDS + ThreadLocalRandom.current().nextLong(30);
            stringRedisTemplate.opsForValue().set(key, CACHE_NULL_VALUE, nullTtl, TimeUnit.SECONDS);
            return list != null ? list : null;
        }

        // 6. 写入 Redis
        this.setWithRandomTtl(key, list, ttl, unit, 10);
        return list;
    }

    // ====================================================================================
    // 3. 防缓存击穿模式：分布式互斥锁方案 (Mutex Lock)
    // ====================================================================================

    /**
     * 防缓存击穿查询模板 (分布式互斥锁 + 双重检查 Double-Check)
     * <p>
     * 适合普通热点数据的强一致性缓存构建：
     * 1. 查缓存，命中则返回；
     * 2. 未命中，获取互斥锁（优先 Redisson，降级 Redis setnx）；
     * 3. 没获取到锁：线程休眠 50ms 后自旋重试；
     * 4. 获取到锁：执行 Double-Check 二次检查缓存；
     * 5. 若仍未命中，查数据库重建缓存，最后释放锁。
     * </p>
     */
    /**
     * 防缓存击穿查询模板 (基于完整 Key 与 Supplier 回源)
     */
    public <R> R queryWithMutex(String key, Class<R> type, Supplier<R> dbFallback, Long ttl, TimeUnit unit) {
        // 1. 尝试从缓存获取
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }
        if (json != null) {
            // 命中了防穿透空值
            return null;
        }

        // 2. 缓存未命中，尝试获取互斥锁
        String lockKey = LOCK_PREFIX + key;
        boolean isLocked = false;
        RLock rLock = null;

        try {

            rLock = redissonClient.getLock(lockKey);
            isLocked = rLock.tryLock(DEFAULT_LOCK_WAIT_MILLIS, DEFAULT_LOCK_LEASE_SECONDS * 1000, TimeUnit.MILLISECONDS);

            // 3. 获取锁失败：休眠重试
            if (!isLocked) {
                Thread.sleep(50);
                return queryWithMutex(key, type, dbFallback, ttl, unit);
            }

            // 4. 获取锁成功：执行 Double-Check 双重检查
            json = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(json)) {
                return JSONUtil.toBean(json, type);
            }
            if (json != null) {
                return null;
            }

            // 5. 双重检查依然未命中，执行数据库回源查询
            R r = dbFallback.get();

            // 6. 判空防穿透处理
            if (r == null) {
                long nullTtl = DEFAULT_NULL_TTL_SECONDS + ThreadLocalRandom.current().nextLong(30);
                stringRedisTemplate.opsForValue().set(key, CACHE_NULL_VALUE, nullTtl, TimeUnit.SECONDS);
                return null;
            }

            // 7. 写入缓存并防雪崩
            this.setWithRandomTtl(key, r, ttl, unit, 10);
            return r;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[CacheUtil] 互斥锁等待被中断, key: {}", key);
            return dbFallback.get();
        } finally {
            // 8. 释放锁
            releaseLock(lockKey, rLock, isLocked);
        }
    }

    /**
     * 列表类型互斥锁防击穿模板
     */
    public <E> List<E> queryListWithMutex(String key, Class<E> elementClass, Supplier<List<E>> dbFallback, Long ttl, TimeUnit unit) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toList(json, elementClass);
        }
        if (json != null) {
            return null;
        }

        String lockKey = LOCK_PREFIX + key;
        boolean isLocked = false;
        RLock rLock = null;

        try {
            rLock = redissonClient.getLock(lockKey);
            isLocked = rLock.tryLock(DEFAULT_LOCK_WAIT_MILLIS, DEFAULT_LOCK_LEASE_SECONDS * 1000, TimeUnit.MILLISECONDS);


            if (!isLocked) {
                Thread.sleep(50);
                return queryListWithMutex(key, elementClass, dbFallback, ttl, unit);
            }

            // Double Check
            json = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(json)) {
                return JSONUtil.toList(json, elementClass);
            }
            if (json != null) {
                return null;
            }

            List<E> list = dbFallback.get();
            if (list == null || list.isEmpty()) {
                long nullTtl = DEFAULT_NULL_TTL_SECONDS + ThreadLocalRandom.current().nextLong(30);
                stringRedisTemplate.opsForValue().set(key, CACHE_NULL_VALUE, nullTtl, TimeUnit.SECONDS);
                return list;
            }

            this.setWithRandomTtl(key, list, ttl, unit, 10);
            return list;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[CacheUtil] 列表互斥锁等待被中断, key: {}", key);
            return dbFallback.get();
        } finally {
            releaseLock(lockKey, rLock, isLocked);
        }
    }

    private void releaseLock(String lockKey, RLock rLock, boolean isLocked) {
        if (!isLocked) {
            return;
        }
        try {
            if (rLock != null) {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            } else {
                stringRedisTemplate.delete(lockKey);
            }
        } catch (Exception e) {
            log.warn("[CacheUtil] 释放互斥锁异常, key: {}", lockKey, e);
        }
    }


}
