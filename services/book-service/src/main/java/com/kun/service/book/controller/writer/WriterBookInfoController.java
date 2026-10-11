package com.kun.service.book.controller.writer;

import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.dto.writer.req.WriterBookInfoPageReqDTO;
import com.kun.service.book.dto.writer.req.WriterBookInfoUpdateReqDTO;
import com.kun.service.book.dto.writer.req.WriterBookPublishReqDTO;
import com.kun.service.book.dto.writer.resp.WriterBookInfoDetailRespDTO;
import com.kun.service.book.dto.writer.resp.WriterBookInfoPageRespDTO;
import com.kun.service.book.dto.writer.resp.WriterBookPublishRespDTO;
import com.kun.service.book.dto.writer.resp.WriterBookStatsRespDTO;
import com.kun.service.book.service.writer.WriterBookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book/writer")
@RequiredArgsConstructor
public class WriterBookInfoController {

    private final WriterBookInfoService bookInfoService;


    @GetMapping("/list")
    public Result<PageResult<WriterBookInfoPageRespDTO>> queryMyBookInfoPage(WriterBookInfoPageReqDTO bookInfoPageReqDTO) {
        PageResult<WriterBookInfoPageRespDTO> pageResult = bookInfoService.queryMyBookInfoPage(bookInfoPageReqDTO);
        return Result.success(pageResult);

    }

    @GetMapping("/{bookId}")
    public Result<WriterBookInfoDetailRespDTO> queryBookInfoDetail(@PathVariable Long bookId) {
        WriterBookInfoDetailRespDTO bookInfoDetailRespDTO = bookInfoService.queryBookInfoDetail(bookId);
        return Result.success(bookInfoDetailRespDTO);
    }


    @PostMapping("/publish")
    public Result<WriterBookPublishRespDTO> publishBook(@RequestBody WriterBookPublishReqDTO bookPublishReqDTO) {
        WriterBookPublishRespDTO bookPublishRespDTO = bookInfoService.publishBook(bookPublishReqDTO);
        return Result.success(bookPublishRespDTO);
    }

    @PutMapping("/{bookId}")
    public Result<Void> updateBookInfo(@PathVariable Long bookId, @RequestBody WriterBookInfoUpdateReqDTO bookInfoUpdateReqDTO) {
        bookInfoService.updateBookInfo(bookId, bookInfoUpdateReqDTO);
        return Result.success();
    }

    @PostMapping("/{bookId}/over")
    public Result<Void> overBook(@PathVariable("bookId") Long bookId) {
        bookInfoService.overBook(bookId);
        return Result.success();
    }

    @GetMapping("/dashBoard/stats")
    public Result<WriterBookStatsRespDTO> queryBookStats() {
        WriterBookStatsRespDTO bookStatsRespDTO = bookInfoService.queryBookStats();
        return Result.success(bookStatsRespDTO);
    }


}
