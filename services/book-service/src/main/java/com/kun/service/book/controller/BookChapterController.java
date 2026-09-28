package com.kun.service.book.controller;

import com.kun.common.core.result.Result;
import com.kun.service.book.dto.req.ChapterExchangeReqDTO;
import com.kun.service.book.dto.resp.BookChapterQueryRespDTO;
import com.kun.service.book.dto.resp.ChapterExchangeRespDTO;
import com.kun.service.book.service.BookChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book/chapter")
@RequiredArgsConstructor
public class BookChapterController {

    private final BookChapterService bookChapterService;


    @GetMapping("/{bookId}/{chapterId}/content")
    public Result<BookChapterQueryRespDTO> queryBookChapter(@PathVariable Long bookId, @PathVariable Long chapterId) {
        BookChapterQueryRespDTO bookChapterQueryRespDTO = bookChapterService.queryBookChapter(bookId, chapterId);
        return Result.success(bookChapterQueryRespDTO);
    }

    @PostMapping("/exchange")
    public Result<ChapterExchangeRespDTO> exchangeChapter(@RequestBody ChapterExchangeReqDTO chapterExchangeReqDTO) {
        ChapterExchangeRespDTO chapterExchangeRespDTO = bookChapterService.exchangeChapter(chapterExchangeReqDTO);
        return Result.success(chapterExchangeRespDTO);

    }


}
