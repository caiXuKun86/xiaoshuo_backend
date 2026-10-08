package com.kun.common.core.exception;

import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局统一异常拦截与处理 (基于 Spring MVC @RestControllerAdvice)
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnClass(HttpServletRequest.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {

    /**
     * 捕获自定义业务异常 (BusinessException)
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常触发 [{} {}] code: {}, message: {}",
                request.getMethod(), request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }




    /**
     * 全局未捕获未知系统异常兜底
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统发生未知未捕获异常 [{} {}]: ", request.getMethod(), request.getRequestURI(), e);
        return Result.fail(ResultCode.SYSTEM_ERROR);
    }
}
