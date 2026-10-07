package com.kun.api.client;

import com.kun.api.dto.shelf.ShelfDTO;
import com.kun.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ShlefFeignClientFallbackFactory implements FallbackFactory<ShelfFeignClient> {

    @Override
    public ShelfFeignClient create(Throwable cause) {
        return new ShelfFeignClient() {
            @Override
            public Result<ShelfDTO> getShelfByBookId(Long bookId) {
                log.error("调用 service-user 失败，触发降级，原因: {}", cause.getMessage());
                // 返回兜底的默认值或友好提示
                return Result.fail(503, "用户服务不可用(降级响应)");
            }


        };
    }
}