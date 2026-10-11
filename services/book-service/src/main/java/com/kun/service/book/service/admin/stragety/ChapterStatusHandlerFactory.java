package com.kun.service.book.service.admin.stragety;

import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 章节状态处理器工厂 (只负责装配与检索策略，与具体业务解耦)
 */
@Component
public class ChapterStatusHandlerFactory {

    /**
     * 策略注册表
     */
    private final Map<Integer, ChapterStatusHandler> handlerMap;

    /**
     * Spring 启动时自动注入所有 Handler 实现并组装为不可变 Map
     */
    public ChapterStatusHandlerFactory(List<ChapterStatusHandler> handlers) {
        this.handlerMap = Collections.unmodifiableMap(
                handlers.stream().collect(Collectors.toMap(
                        h -> h.getTargetStatus().getCode(),
                        h -> h
                ))
        );
    }

    /**
     * 根据目标状态码获取对应处理器 (内聚了非法参数校验)
     *
     * @param targetStatus 目标状态码
     * @return 对应的策略处理器
     */
    public ChapterStatusHandler getHandler(Integer targetStatus) {
        ChapterStatusHandler handler = handlerMap.get(targetStatus);
        if (handler == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "不支持的目标状态码: " + targetStatus);
        }
        return handler;
    }
}