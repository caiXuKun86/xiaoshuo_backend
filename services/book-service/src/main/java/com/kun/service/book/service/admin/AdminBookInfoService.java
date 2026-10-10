package com.kun.service.book.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.dto.admin.req.AdminBookInfoPageReqDTO;
import com.kun.service.book.dto.admin.resp.AdminBookInfoDetailRespDTO;
import com.kun.service.book.dto.admin.resp.AdminBookInfoPageRespDTO;

/**
* @author Lenovo
* @description 针对表【book_info(图书信息主表)】的数据库操作Service
* @createDate 2026-09-25 10:39:19
*/
public interface AdminBookInfoService extends IService<BookInfo> {


    PageResult<AdminBookInfoPageRespDTO> queryBookInfoPage(AdminBookInfoPageReqDTO bookInfoPageReqDTO);

    AdminBookInfoDetailRespDTO queryBookInfoDetail(Long bookId);

    void patchBookInfoStatus(Long bookId, Integer status);

    void deleteBookInfo(Long bookId);
}
