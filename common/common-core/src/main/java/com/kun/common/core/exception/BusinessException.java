package com.kun.common.core.exception;

import com.kun.common.core.enums.IResultCode;
import com.kun.common.core.enums.ResultCode;
import lombok.Getter;

/**
 * 统一业务运行时异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码
     */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.SYSTEM_ERROR.getCode();
    }

    public BusinessException(IResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(IResultCode resultCode, String customMessage) {
        super(customMessage);
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
