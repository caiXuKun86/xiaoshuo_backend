package com.kun.service.book.controller;

import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.dto.req.BookRankPageReqDTO;
import com.kun.service.book.dto.resp.BookRankHomeSummaryRespDTO;
import com.kun.service.book.dto.resp.BookRankPageRespDTO;
import com.kun.service.book.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/book/rank")
@RequiredArgsConstructor
public class BookRankController {

    private final BookInfoService bookInfoService;

    @GetMapping
    public Result<PageResult<BookRankPageRespDTO>> rankBookPage(BookRankPageReqDTO bookSearchPageReqDTO) {
        PageResult<BookRankPageRespDTO> pageResult = bookInfoService.rankBookPage(bookSearchPageReqDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/home-summary")
    public Result<BookRankHomeSummaryRespDTO> queryRankHomeSummary(){
        BookRankHomeSummaryRespDTO result = bookInfoService.queryRankHomeSummary();
        return Result.success(result);

    }

}
