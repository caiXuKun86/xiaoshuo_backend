package com.kun.common.core.enums;

/**
 * 响应状态码接口契约
 */
public interface IResultCode {

    /**
     * 获取状态码数值
     */
    Integer getCode();

    /**
     * 获取状态描述信息
     */
    String getMessage();
}
