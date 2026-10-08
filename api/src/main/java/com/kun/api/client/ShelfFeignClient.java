package com.kun.api.client;

import com.kun.api.client.fallback.ShelfFeignClientFallbackFactory;
import com.kun.api.dto.shelf.ShelfDTO;
import com.kun.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 图书与内容服务内部 Feign 声明接口
 */
@FeignClient(value = "shelf-service", contextId = "shelfFeignClient",fallbackFactory = ShelfFeignClientFallbackFactory.class)
public interface ShelfFeignClient {

    /**
     * 查询章节信息
     */
    @GetMapping("/inner/shelf/info")
    Result<ShelfDTO> getShelfDTO(Long bookId,Long userId);

}
