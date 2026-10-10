package com.kun.service.book.controller;

import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.dto.req.BookSearchPageReqDTO;
import com.kun.service.book.dto.resp.BookInfoPageRespDTO;
import com.kun.service.book.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/book/search")
@RequiredArgsConstructor
public class BookSearchController {

    private final BookInfoService bookInfoService;

    @GetMapping
    public Result<PageResult<BookInfoPageRespDTO>> searchBookPage(BookSearchPageReqDTO bookSearchPageReqDTO) {
        PageResult<BookInfoPageRespDTO> pageResult = bookInfoService.searchBookPage(bookSearchPageReqDTO);
        return Result.success(pageResult);
    }

}
