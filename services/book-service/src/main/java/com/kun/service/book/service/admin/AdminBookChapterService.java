package com.kun.service.book.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.domain.BookChapter;
import com.kun.service.book.dto.admin.req.AdminChapterBatchSetChargeReqDTO;
import com.kun.service.book.dto.admin.req.AdminChapterPageReqDTO;
import com.kun.service.book.dto.admin.resp.AdminChapterBatchSetChargeRespDTO;
import com.kun.service.book.dto.admin.resp.AdminChapterDetailRespDTO;
import com.kun.service.book.dto.admin.resp.AdminChapterPageRespDTO;

/**
 * @author Lenovo
 * @description 针对表【book_chapter(章节目录表)】的数据库操作Service
 * @createDate 2026-09-25 10:39:19
 */
public interface AdminBookChapterService extends IService<BookChapter> {


    PageResult<AdminChapterPageRespDTO> queryChapterPage(AdminChapterPageReqDTO chapterPageReqDTO);

    AdminChapterDetailRespDTO queryChapterDetail(Long chapterId);

    void patchChapterStatus(Long chapterId);

    AdminChapterBatchSetChargeRespDTO setChapterChargeBatch(AdminChapterBatchSetChargeReqDTO chapterBatchSetChargeReqDTO);
}
