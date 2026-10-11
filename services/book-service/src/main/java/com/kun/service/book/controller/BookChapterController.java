package com.kun.service.book.controller;

import com.kun.common.core.result.Result;
import com.kun.service.book.dto.req.ChapterExchangeReqDTO;
import com.kun.service.book.dto.resp.ChapterDetailRespDTO;
import com.kun.service.book.dto.resp.ChapterExchangeRespDTO;
import com.kun.service.book.dto.resp.ChapterListRespDTO;
import com.kun.service.book.service.BookChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/book/chapter")
@RequiredArgsConstructor
public class BookChapterController {

    private final BookChapterService bookChapterService;


    @GetMapping("/{bookId}/list")
    public Result<List<ChapterListRespDTO>> queryBookChapterList(@PathVariable("bookId") Long bookId, @RequestParam(name = "sortOrder", required = false, defaultValue = "ASC") String sortOrder) {
        List<ChapterListRespDTO> bookCatalogQueryRespDTO = bookChapterService.queryBookChapterList(bookId, sortOrder);
        return Result.success(bookCatalogQueryRespDTO);
    }
    @GetMapping("/{bookId}/{chapterId}/content")
    public Result<ChapterDetailRespDTO> queryChapterDetail(@PathVariable("bookId") Long bookId, @PathVariable("chapterId") Long chapterId) {
        ChapterDetailRespDTO bookChapterQueryRespDTO = bookChapterService.queryChapterDetail(bookId, chapterId);
        return Result.success(bookChapterQueryRespDTO);
    }

    @PostMapping("/exchange")
    public Result<ChapterExchangeRespDTO> exchangeChapter(@RequestBody ChapterExchangeReqDTO chapterExchangeReqDTO) {
        ChapterExchangeRespDTO chapterExchangeRespDTO = bookChapterService.exchangeChapter(chapterExchangeReqDTO);
        return Result.success(chapterExchangeRespDTO);

    }




}
