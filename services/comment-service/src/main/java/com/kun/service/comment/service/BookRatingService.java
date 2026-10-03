package com.kun.service.comment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.service.comment.domain.BookRating;
import com.kun.service.comment.dto.req.BookRatingReqDTO;
import com.kun.service.comment.dto.resp.BookRatingDetailRespDTO;
import com.kun.service.comment.dto.resp.BookRatingRespDTO;

/**
* @author Lenovo
* @description 针对表【book_rating(书籍评分表)】的数据库操作Service
* @createDate 2026-10-02 19:42:59
*/
public interface BookRatingService extends IService<BookRating> {

    BookRatingRespDTO bookRating(BookRatingReqDTO bookRatingReqDTO);

    BookRatingDetailRespDTO queryBookRatingDetail(Long bookId);
}
