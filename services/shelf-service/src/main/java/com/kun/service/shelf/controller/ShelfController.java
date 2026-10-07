package com.kun.service.shelf.controller;

import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.shelf.dto.req.BookShelfAddReqDTO;
import com.kun.service.shelf.dto.req.BookShelfMergeReqDTO;
import com.kun.service.shelf.dto.req.BookShelfPageReqDTO;
import com.kun.service.shelf.dto.req.BookShelfSyncReqDTO;
import com.kun.service.shelf.dto.resp.BookShelfAddRespDTO;
import com.kun.service.shelf.dto.resp.BookShelfQueryRespDTO;
import com.kun.service.shelf.dto.resp.BookShelfSyncRespDTO;
import com.kun.service.shelf.dto.resp.ReadingProgressQueryRespDTO;
import com.kun.service.shelf.service.BookshelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shelf")
@RequiredArgsConstructor
public class ShelfController {

    private final BookshelfService bookshelfService;

    @GetMapping("/list")
    public Result<PageResult<BookShelfQueryRespDTO>> pageBookShelfList(BookShelfPageReqDTO bookShelfPageReqDTO) {
        PageResult<BookShelfQueryRespDTO> pageResult = bookshelfService.pageBookShelfList(bookShelfPageReqDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/add")
    public Result<BookShelfAddRespDTO> addBook2Shelf(@RequestBody BookShelfAddReqDTO bookShelfAddReqDTO) {
        BookShelfAddRespDTO bookShelfAddRespDTO = bookshelfService.addBook2Shelf(bookShelfAddReqDTO);
        return Result.success(bookShelfAddRespDTO);
    }

    @DeleteMapping("/remove")
    public Result<Void> removeBooks2Shelf(@RequestParam("bookIds") List<Long> bookIds) {
        bookshelfService.removeBooks2Shelf(bookIds);
        return Result.success();
    }

    @PostMapping("/progress/sync")
    public Result<BookShelfSyncRespDTO> syncProgress(@RequestBody BookShelfSyncReqDTO bookShelfSyncReqDTO) {
        BookShelfSyncRespDTO bookShelfSyncRespDTO = bookshelfService.syncProgress(bookShelfSyncReqDTO);
        return Result.success(bookShelfSyncRespDTO);
    }

    @GetMapping("/progress/{bookId}")
    public Result<ReadingProgressQueryRespDTO> getProgress(@PathVariable("bookId") Long bookId) {
        ReadingProgressQueryRespDTO readingProgressQueryRespDTO = bookshelfService.getProgressByBookId(bookId);
        return Result.success(readingProgressQueryRespDTO);
    }
    @PostMapping("/merge")
    public Result<Void> mergeShelf(@RequestBody BookShelfMergeReqDTO bookShelfMergeReqDTO){
        bookshelfService.mergeShelf(bookShelfMergeReqDTO);
        return Result.success();
    }


}
