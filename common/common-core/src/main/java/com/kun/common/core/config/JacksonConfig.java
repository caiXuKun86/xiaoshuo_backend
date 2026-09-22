package com.kun.common.core.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.kun.common.core.constant.NovelConstants;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Jackson 统一序列化定制
 * 1. 雪花算法 64 位 Long 精度丢失问题：将 Long 序列化为 String 字符串
 * 2. 日期时间统一格式化为 yyyy-MM-dd HH:mm:ss，时区固定为 GMT+8
 */
@AutoConfiguration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 时区配置
            builder.timeZone(TimeZone.getTimeZone("GMT+8"));

            // Long 类型转 String，杜绝前端 JS (Number.MAX_SAFE_INTEGER) 53位精度丢失
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);

            // LocalDateTime 序列化与反序列化
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(NovelConstants.DATE_TIME_FORMAT);
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

            // LocalDate 序列化与反序列化
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(NovelConstants.DATE_FORMAT);
            builder.serializerByType(LocalDate.class, new LocalDateSerializer(dateFormatter));
            builder.deserializerByType(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        };
    }
}
