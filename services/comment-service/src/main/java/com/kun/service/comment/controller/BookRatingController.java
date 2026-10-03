package com.kun.service.comment.controller;

import com.kun.common.core.result.Result;
import com.kun.service.comment.dto.req.BookRatingReqDTO;
import com.kun.service.comment.dto.resp.BookRatingDetailRespDTO;
import com.kun.service.comment.dto.resp.BookRatingRespDTO;
import com.kun.service.comment.service.BookRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/comment/rating")
public class BookRatingController {

    private final BookRatingService bookRatingService;

    @PostMapping
    public Result<BookRatingRespDTO> bookRating(@RequestBody BookRatingReqDTO bookRatingReqDTO) {
        BookRatingRespDTO bookRatingRespDTO = bookRatingService.bookRating(bookRatingReqDTO);
        return Result.success(bookRatingRespDTO);
    }
    @GetMapping("/{bookId}")
    public Result<BookRatingDetailRespDTO> queryBookRatingDetail(@PathVariable("bookId")Long bookId){
        BookRatingDetailRespDTO bookRatingDetailRespDTO = bookRatingService.queryBookRatingDetail(bookId);
        return Result.success(bookRatingDetailRespDTO);
    }

}
