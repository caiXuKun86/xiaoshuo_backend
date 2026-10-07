package com.kun.api.client;

import com.kun.api.dto.book.BookDTO;
import com.kun.api.dto.book.ChapterDTO;
import com.kun.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class BookFeignClientFallbackFactory implements FallbackFactory<BookFeignClient> {

    @Override
    public BookFeignClient create(Throwable cause) {
        return new BookFeignClient() {

            @Override
            public Result<BookDTO> getBookById(Long bookId) {
                log.error("调用 service-book 失败，触发降级，原因: {}", cause.getMessage());
                // 返回兜底的默认值或友好提示
                return Result.fail(503, "用户服务不可用(降级响应)");
            }

            @Override
            public Result<List<BookDTO>> getBookListById(Collection<Long> ids) {
                log.error("调用 service-book 失败，触发降级，原因: {}", cause.getMessage());
                // 返回兜底的默认值或友好提示
                return Result.fail(503, "用户服务不可用(降级响应)");
            }

            @Override
            public Result<ChapterDTO> getChapterById(Long chapterId) {
                log.error("调用 service-book 失败，触发降级，原因: {}", cause.getMessage());
                // 返回兜底的默认值或友好提示
                return Result.fail(503, "用户服务不可用(降级响应)");
            }
        };
    }
}