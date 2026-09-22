package com.kun.api.client;

import com.kun.api.config.FeignConfig;
import com.kun.api.dto.book.BookDTO;
import com.kun.api.dto.book.ChapterDTO;
import com.kun.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 图书与内容服务内部 Feign 声明接口
 */
@FeignClient(value = "service-book", contextId = "bookFeignClient", configuration = FeignConfig.class)
public interface BookFeignClient {

    /**
     * 根据小说 ID 查询图书基础信息 (书架、评论、订单等校验图书真实性)
     */
    @GetMapping("/inner/book/{bookId}")
    Result<BookDTO> getBookById(@PathVariable("bookId") Long bookId);

    /**
     * 根据章节 ID 查询章节信息 (用于单章兑换、计费核验等)
     */
    @GetMapping("/inner/book/chapter/{chapterId}")
    Result<ChapterDTO> getChapterById(@PathVariable("chapterId") Long chapterId);
}
