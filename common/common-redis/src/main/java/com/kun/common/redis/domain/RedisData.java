package com.kun.common.redis.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 带有逻辑过期时间的缓存数据包装实体
 * 用于高并发热点 Key 防击穿 (Logical Expiration 模式)
 *
 * @author kun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisData<T> {

    /**
     * 逻辑过期时间点
     */
    private LocalDateTime expireTime;

    /**
     * 业务数据载体实体
     */
    private T data;
}
