package com.kun.common.redis.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 通用操作工具类
 * 涵盖 String、Hash、Set、ZSet 以及 BitMap (连续签到打卡) 等全场景操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 1. 通用 Key 操作 ====================

    /**
     * 指定缓存失效时间
     */
    public Boolean expire(String key, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                return redisTemplate.expire(key, time, timeUnit);
            }
            return false;
        } catch (Exception e) {
            log.error("Redis expire 异常 key: {}", key, e);
            return false;
        }
    }

    /**
     * 获取指定 key 的过期时间
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断 key 是否存在
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis hasKey 异常 key: {}", key, e);
            return false;
        }
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除缓存
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    // ==================== 2. String 字符操作 ====================

    /**
     * 普通缓存获取
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 泛型获取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * 普通缓存放入
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 普通缓存放入并设置时间
     */
    public void set(String key, Object value, long time, TimeUnit timeUnit) {
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, timeUnit);
        } else {
            set(key, value);
        }
    }

    /**
     * 递增
     */
    public Long incr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     */
    public Long decr(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ==================== 3. Hash 哈希操作 (段评气泡聚合等) ====================

    /**
     * 获取 Hash 表中的特定项
     */
    public Object hGet(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取 Hash 表中的所有键值对
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 向 Hash 表中放入数据
     */
    public void hSet(String key, String item, Object value) {
        redisTemplate.opsForHash().put(key, item, value);
    }

    /**
     * Hash 递增 (如气泡评论数递增)
     */
    public Long hIncrBy(String key, String item, long delta) {
        return redisTemplate.opsForHash().increment(key, item, delta);
    }

    /**
     * 删除 Hash 表中的值
     */
    public Long hDel(String key, Object... item) {
        return redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断 Hash 表中是否存在该项
     */
    public Boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    // ==================== 4. Set 集合操作 (高频点赞去重等) ====================

    /**
     * 将数据放入 set 缓存
     */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 根据 key 获取 Set 中的所有值
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 判断 value 是否存在于 Set 中
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 移除 Set 中的某个值
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 获取 Set 缓存的大小
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // ==================== 5. ZSet 有序集合操作 (热搜词、小说榜单) ====================

    /**
     * 添加元素到 ZSet
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 增加 ZSet 元素的 score (热搜加权)
     */
    public Double zIncrementScore(String key, Object value, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    /**
     * 获取元素在 ZSet 中的分数
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 倒序获取排行榜 (从高到低，如 Top 10)
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 倒序获取排行榜带分数
     */
    public Set<ZSetOperations.TypedTuple<Object>> zReverseRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    // ==================== 6. BitMap 位图操作 (用户每日连续签到打卡) ====================

    /**
     * 设置签到状态 (offset 对应当月天数: 1号填0, 2号填1...)
     */
    public Boolean setBit(String key, long offset, boolean value) {
        return redisTemplate.opsForValue().setBit(key, offset, value);
    }

    /**
     * 获取某天的签到状态
     */
    public Boolean getBit(String key, long offset) {
        return redisTemplate.opsForValue().getBit(key, offset);
    }

    /**
     * 统计指定月份签到的总天数 (BITCOUNT)
     */
    public Long bitCount(String key) {
        return redisTemplate.execute((RedisCallback<Long>) con -> con.stringCommands().bitCount(key.getBytes()));
    }
}
