package com.kun.service.book.service;

import com.kun.common.database.page.PageResult;
import com.kun.service.book.domain.BookInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.service.book.dto.req.BookPageReqDTO;
import com.kun.service.book.dto.req.BookPublishReqDTO;
import com.kun.service.book.dto.resp.BookCatalogQueryRespDTO;
import com.kun.service.book.dto.resp.BookDetailQueryRespDTO;
import com.kun.service.book.dto.resp.BookPageRespDTO;
import com.kun.service.book.dto.resp.BookPublishRespDTO;

/**
* @author Lenovo
* @description 针对表【book_info(图书信息主表)】的数据库操作Service
* @createDate 2026-09-25 10:39:19
*/
public interface BookInfoService extends IService<BookInfo> {

    PageResult<BookPageRespDTO> pageBook(BookPageReqDTO bookPageReqDTO);

    BookDetailQueryRespDTO queryBookDetailById(Long id);

    BookCatalogQueryRespDTO queryBookCatalogById(Long bookId,String sortOrder);

    BookPublishRespDTO publishBook(BookPublishReqDTO bookPublishReqDTO);
}
