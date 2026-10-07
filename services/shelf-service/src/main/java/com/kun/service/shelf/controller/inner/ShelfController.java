package com.kun.service.shelf.controller.inner;

import com.kun.api.dto.shelf.ShelfDTO;
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

@RestController("innerShelfController")
@RequiredArgsConstructor
public class ShelfController {

    private final BookshelfService bookshelfService;

    @GetMapping("/inner/shelf/{bookId}")
    Result<ShelfDTO> getShelfByBookId(@PathVariable("bookId") Long bookId) {
        ShelfDTO shelfDTO = bookshelfService.getShelfByBookId(bookId);
        return Result.success(shelfDTO);
    }


}
