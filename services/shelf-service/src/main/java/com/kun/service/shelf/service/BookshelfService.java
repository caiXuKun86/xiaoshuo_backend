package com.kun.service.shelf.service;

import com.kun.api.dto.shelf.ShelfDTO;
import com.kun.common.database.page.PageResult;
import com.kun.service.shelf.domain.Bookshelf;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.service.shelf.dto.req.BookShelfAddReqDTO;
import com.kun.service.shelf.dto.req.BookShelfMergeReqDTO;
import com.kun.service.shelf.dto.req.BookShelfPageReqDTO;
import com.kun.service.shelf.dto.req.BookShelfSyncReqDTO;
import com.kun.service.shelf.dto.resp.BookShelfAddRespDTO;
import com.kun.service.shelf.dto.resp.BookShelfQueryRespDTO;
import com.kun.service.shelf.dto.resp.BookShelfSyncRespDTO;
import com.kun.service.shelf.dto.resp.ReadingProgressQueryRespDTO;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【bookshelf(用户书架表)】的数据库操作Service
* @createDate 2026-09-30 20:32:27
*/
public interface BookshelfService extends IService<Bookshelf> {

    PageResult<BookShelfQueryRespDTO> pageBookShelfList(BookShelfPageReqDTO bookShelfPageReqDTO);

    BookShelfAddRespDTO addBook2Shelf(BookShelfAddReqDTO bookShelfAddReqDTO);

    void removeBooks2Shelf(List<Long> ids);

    BookShelfSyncRespDTO syncProgress(BookShelfSyncReqDTO bookShelfSyncReqDTO);

    ReadingProgressQueryRespDTO getProgressByBookId(Long bookId);

    void mergeShelf(BookShelfMergeReqDTO bookShelfMergeReqDTO);

    ShelfDTO getShelfByBookId(Long bookId,Long userId);
}
