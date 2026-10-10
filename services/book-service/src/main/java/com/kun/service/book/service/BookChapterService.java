package com.kun.service.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.service.book.domain.BookChapter;
import com.kun.service.book.dto.req.ChapterExchangeReqDTO;
import com.kun.service.book.dto.req.ChapterPublishReqDTO;
import com.kun.service.book.dto.req.ChapterUpdateReqDTO;
import com.kun.service.book.dto.resp.BookChapterQueryRespDTO;
import com.kun.service.book.dto.resp.ChapterExchangeRespDTO;
import com.kun.service.book.dto.resp.ChapterPublishRespDTO;
import com.kun.service.book.dto.resp.ChapterUpdateRespDTO;

/**
* @author Lenovo
* @description 针对表【book_chapter(章节目录表)】的数据库操作Service
* @createDate 2026-09-25 10:39:19
*/
public interface BookChapterService extends IService<BookChapter> {

    BookChapterQueryRespDTO queryBookChapter(Long bookId, Long chapterId);

    ChapterExchangeRespDTO exchangeChapter(ChapterExchangeReqDTO chapterExchangeReqDTO);

    ChapterPublishRespDTO publishChapter(ChapterPublishReqDTO chapterPublishReqDTO);

    ChapterUpdateRespDTO updateChapter(ChapterUpdateReqDTO chapterUpdateReqDTO,Long chapterId);

}
