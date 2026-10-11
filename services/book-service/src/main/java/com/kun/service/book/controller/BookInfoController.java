package com.kun.service.book.controller;

import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.dto.req.BookInfoPageReqDTO;
import com.kun.service.book.dto.resp.BookInfoDetailRespDTO;
import com.kun.service.book.dto.resp.BookInfoPageRespDTO;
import com.kun.service.book.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookInfoController {

    private final BookInfoService bookInfoService;

    @GetMapping("/filter")
    public Result<PageResult<BookInfoPageRespDTO>> queryBookInfoPage(BookInfoPageReqDTO bookInfoPageReqDTO) {
        PageResult<BookInfoPageRespDTO> pageResult = bookInfoService.queryBookInfoPage(bookInfoPageReqDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/detail/{id}")
    public Result<BookInfoDetailRespDTO> queryBookInfoDetail(@PathVariable("id") Long id) {
        BookInfoDetailRespDTO bookDetailQueryRespDTO = bookInfoService.queryBookInfoDetail(id);
        return Result.success(bookDetailQueryRespDTO);
    }




}
