package com.kun.api.client;

import com.kun.api.config.FeignConfig;
import com.kun.api.dto.shelf.ShelfDTO;
import com.kun.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 图书与内容服务内部 Feign 声明接口
 */
@FeignClient(value = "shelf-service", contextId = "shelfFeignClient", configuration = FeignConfig.class)
public interface ShelfFeignClient {

    /**
     * 根据小说 ID 查询图书基础信息 (书架、评论、订单等校验图书真实性)
     */
    @GetMapping("/inner/shelf/{bookId}")
    Result<ShelfDTO> getShelfByBookId(@PathVariable("bookId") Long bookId);

}
