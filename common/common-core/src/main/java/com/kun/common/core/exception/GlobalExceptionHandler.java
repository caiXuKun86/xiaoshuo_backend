package com.kun.common.core.exception;

import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局统一异常拦截与处理 (基于 Spring MVC @RestControllerAdvice)
 */
@Slf4j
@RestControllerAdvice
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
     * 捕获 POST 请求 @RequestBody 参数校验异常 (MethodArgumentNotValidException)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 [{} {}]: {}", request.getMethod(), request.getRequestURI(), errorMsg);
        return Result.fail(ResultCode.PARAM_INVALID.getCode(), errorMsg);
    }

    /**
     * 捕获 GET/表单参数绑定校验异常 (BindException)
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("表单参数绑定失败 [{} {}]: {}", request.getMethod(), request.getRequestURI(), errorMsg);
        return Result.fail(ResultCode.PARAM_INVALID.getCode(), errorMsg);
    }

    /**
     * 捕获单个参数校验异常 (@RequestParam / @PathVariable 校验失败)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String errorMsg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("字段校验失败 [{} {}]: {}", request.getMethod(), request.getRequestURI(), errorMsg);
        return Result.fail(ResultCode.PARAM_INVALID.getCode(), errorMsg);
    }

    /**
     * 捕获缺少必须的请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        String errorMsg = String.format("缺少必填请求参数: %s", e.getParameterName());
        log.warn("缺少请求参数 [{} {}]: {}", request.getMethod(), request.getRequestURI(), errorMsg);
        return Result.fail(ResultCode.PARAM_INVALID.getCode(), errorMsg);
    }

    /**
     * 捕获非法参数异常 (IllegalArgumentException)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数 [{} {}]: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return Result.fail(ResultCode.PARAM_INVALID.getCode(), e.getMessage());
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
