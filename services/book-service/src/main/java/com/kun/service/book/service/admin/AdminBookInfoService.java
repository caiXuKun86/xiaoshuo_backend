package com.kun.service.book.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.dto.req.BookFilterPageReqDTO;
import com.kun.service.book.dto.req.BookPublishReqDTO;
import com.kun.service.book.dto.req.BookRankPageReqDTO;
import com.kun.service.book.dto.req.BookSearchPageReqDTO;
import com.kun.service.book.dto.resp.*;

/**
* @author Lenovo
* @description 针对表【book_info(图书信息主表)】的数据库操作Service
* @createDate 2026-09-25 10:39:19
*/
public interface AdminBookInfoService extends IService<BookInfo> {

    PageResult<BookPageRespDTO> pageBook(BookFilterPageReqDTO bookPageReqDTO);

    BookDetailQueryRespDTO queryBookDetailById(Long id);

    BookCatalogQueryRespDTO queryBookCatalogById(Long bookId,String sortOrder);

    BookPublishRespDTO publishBook(BookPublishReqDTO bookPublishReqDTO);

    void overBook(Long bookId);

    PageResult<BookPageRespDTO> searchBookPage(BookSearchPageReqDTO bookSearchPageReqDTO);

    PageResult<BookRankPageRespDTO> rankBookPage(BookRankPageReqDTO bookSearchPageReqDTO);

    BookRankHomeSummaryRespDTO queryRankHomeSummary();

}
