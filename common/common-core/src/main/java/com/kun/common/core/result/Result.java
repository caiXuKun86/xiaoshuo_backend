package com.kun.common.core.result;

import com.kun.common.core.enums.IResultCode;
import com.kun.common.core.enums.ResultCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 全局统一 RESTful 响应体
 *
 * @param <T> 响应数据泛型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一接口响应结果包装")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "业务状态码 (200: 成功, 其他: 业务异常码)", example = "200")
    private Integer code;

    @Schema(description = "业务提示消息", example = "操作成功")
    private String message;

    @Schema(description = "业务响应数据负载")
    private T data;

    @Schema(description = "响应时间戳 (毫秒)", example = "1790060000123")
    private Long timestamp;

    @Schema(description = "全链路追踪 ID", example = "tr-8f3a9b1c7d2e4f5a")
    private String traceId;

    // ==================== 快捷成功构建方法 ====================

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return success(ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // ==================== 快捷失败构建方法 ====================

    public static <T> Result<T> fail() {
        return fail(ResultCode.SYSTEM_ERROR);
    }

    public static <T> Result<T> fail(String message) {
        return fail(ResultCode.SYSTEM_ERROR.getCode(), message);
    }

    public static <T> Result<T> fail(IResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMessage());
    }

    public static <T> Result<T> fail(IResultCode resultCode, String customMessage) {
        return fail(resultCode.getCode(), customMessage);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 判断当前响应是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}
