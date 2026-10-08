package com.kun.api.client;

import com.kun.api.client.fallback.BookFeignClientFallbackFactory;
import com.kun.api.dto.book.BookDTO;
import com.kun.api.dto.book.ChapterDTO;
import com.kun.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

/**
 * 图书与内容服务内部 Feign 声明接口
 */
@FeignClient(value = "book-service", contextId = "bookFeignClient",fallbackFactory= BookFeignClientFallbackFactory.class)
public interface BookFeignClient {

    /**
     * 根据小说 ID 查询图书基础信息 (书架、评论、订单等校验图书真实性)
     */
    @GetMapping("/inner/book/{bookId}")
    Result<BookDTO> getBookById(@PathVariable("bookId") Long bookId);

    /**
     * 根据小说 IDs 查询图书基础信息列表 (书架、评论、订单等校验图书真实性)
     * @param ids
     * @return
     */
    @GetMapping("/inner/book/list")
    Result<List<BookDTO>> getBookListById(@RequestParam("ids") Collection<Long> ids);

    /**
     * 根据章节 ID 查询章节信息 (用于单章兑换、计费核验等)
     */
    @GetMapping("/inner/book/chapter/{chapterId}")
    Result<ChapterDTO> getChapterById(@PathVariable("chapterId") Long chapterId);
}
