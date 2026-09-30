package com.kun.service.book.controller;

import com.kun.common.core.result.Result;
import com.kun.service.book.dto.req.ChapterExchangeReqDTO;
import com.kun.service.book.dto.req.ChapterPublishReqDTO;
import com.kun.service.book.dto.req.ChapterUpdateReqDTO;
import com.kun.service.book.dto.resp.BookChapterQueryRespDTO;
import com.kun.service.book.dto.resp.ChapterExchangeRespDTO;
import com.kun.service.book.dto.resp.ChapterPublishRespDTO;
import com.kun.service.book.dto.resp.ChapterUpdateRespDTO;
import com.kun.service.book.service.BookChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book/chapter")
@RequiredArgsConstructor
public class BookChapterController {

    private final BookChapterService bookChapterService;


    @GetMapping("/{bookId}/{chapterId}/content")
    public Result<BookChapterQueryRespDTO> queryBookChapter(@PathVariable("bookId") Long bookId, @PathVariable("chapterId") Long chapterId) {
        BookChapterQueryRespDTO bookChapterQueryRespDTO = bookChapterService.queryBookChapter(bookId, chapterId);
        return Result.success(bookChapterQueryRespDTO);
    }

    @PostMapping("/exchange")
    public Result<ChapterExchangeRespDTO> exchangeChapter(@RequestBody ChapterExchangeReqDTO chapterExchangeReqDTO) {
        ChapterExchangeRespDTO chapterExchangeRespDTO = bookChapterService.exchangeChapter(chapterExchangeReqDTO);
        return Result.success(chapterExchangeRespDTO);

    }

    @PostMapping("/publish")
    public Result<ChapterPublishRespDTO> publishChapter(@RequestBody ChapterPublishReqDTO chapterPublishReqDTO) {
        ChapterPublishRespDTO chapterPublishRespDTO = bookChapterService.publishChapter(chapterPublishReqDTO);
        return Result.success(chapterPublishRespDTO);
    }

    @PutMapping("/{chapterId}")
    public Result<ChapterUpdateRespDTO> updateChapter(@RequestBody ChapterUpdateReqDTO chapterUpdateReqDTO, @PathVariable("chapterId") Long chapterId) {
        ChapterUpdateRespDTO chapterUpdateRespDTO = bookChapterService.updateChapter(chapterUpdateReqDTO,chapterId);
        return Result.success(chapterUpdateRespDTO);
    }


}
