package com.kun.service.book.service.writer;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.dto.writer.req.WriterBookInfoPageReqDTO;
import com.kun.service.book.dto.writer.req.WriterBookInfoUpdateReqDTO;
import com.kun.service.book.dto.writer.req.WriterBookPublishReqDTO;
import com.kun.service.book.dto.writer.resp.WriterBookInfoDetailRespDTO;
import com.kun.service.book.dto.writer.resp.WriterBookInfoPageRespDTO;
import com.kun.service.book.dto.writer.resp.WriterBookPublishRespDTO;
import com.kun.service.book.dto.writer.resp.WriterBookStatsRespDTO;

/**
 * @author Lenovo
 * @description 针对表【book_info(图书信息主表)】的数据库操作Service
 * @createDate 2026-09-25 10:39:19
 */
public interface WriterBookInfoService extends IService<BookInfo> {


    PageResult<WriterBookInfoPageRespDTO> queryMyBookInfoPage(WriterBookInfoPageReqDTO bookInfoPageReqDTO);

    WriterBookInfoDetailRespDTO queryBookInfoDetail(Long bookId);

    WriterBookPublishRespDTO publishBook(WriterBookPublishReqDTO bookPublishReqDTO);

    void updateBookInfo(Long bookId, WriterBookInfoUpdateReqDTO bookInfoUpdateReqDTO);

    void overBook(Long bookId);

    WriterBookStatsRespDTO queryBookStats();
}
