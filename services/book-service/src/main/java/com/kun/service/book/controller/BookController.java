package com.kun.service.book.controller;

import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.dto.req.BookPageReqDTO;
import com.kun.service.book.dto.resp.BookCatalogQueryRespDTO;
import com.kun.service.book.dto.resp.BookDetailQueryRespDTO;
import com.kun.service.book.dto.resp.BookPageRespDTO;
import com.kun.service.book.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookController {

    private final BookInfoService bookInfoService;

    @GetMapping("/filter")
    public Result<PageResult<BookPageRespDTO>> pageBook(BookPageReqDTO bookPageReqDTO) {
        PageResult<BookPageRespDTO> pageResult = bookInfoService.pageBook(bookPageReqDTO);
        return Result.success(pageResult);

    }

    //TODO 远程调用获取isInBookshelf userScore lastReadChapterId lastReadChapterName
    //TODO 缓存
    @GetMapping("/detail/{id}")
    public Result<BookDetailQueryRespDTO> queryBookDetail(@PathVariable Long id) {
        BookDetailQueryRespDTO bookDetailQueryRespDTO = bookInfoService.queryBookDetailById(id);
        return Result.success(bookDetailQueryRespDTO);
    }

    @GetMapping("/detail/{bookId}/catalog")
    public Result<BookCatalogQueryRespDTO> queryBookCatalog(@PathVariable Long bookId, @RequestParam(required = false, defaultValue = "ASC") String sortOrder) {
        BookCatalogQueryRespDTO bookCatalogQueryRespDTO = bookInfoService.queryBookCatalogById(bookId, sortOrder);
        return Result.success(bookCatalogQueryRespDTO);
    }


}
