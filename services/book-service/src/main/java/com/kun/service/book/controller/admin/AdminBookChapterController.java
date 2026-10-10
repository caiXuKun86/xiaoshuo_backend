package com.kun.service.book.controller.admin;

import com.kun.common.core.aop.RequireRole;
import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.dto.admin.req.AdminChapterBatchSetChargeReqDTO;
import com.kun.service.book.dto.admin.req.AdminChapterPageReqDTO;
import com.kun.service.book.dto.admin.resp.AdminChapterBatchSetChargeRespDTO;
import com.kun.service.book.dto.admin.resp.AdminChapterDetailRespDTO;
import com.kun.service.book.dto.admin.resp.AdminChapterPageRespDTO;
import com.kun.service.book.service.admin.AdminBookChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequireRole
@RestController
@RequestMapping("/admin/book/chapter")
@RequiredArgsConstructor
public class AdminBookChapterController {

    private final AdminBookChapterService bookChapterService;


    @GetMapping("/list")
    public Result<PageResult<AdminChapterPageRespDTO>> queryChapterPage(AdminChapterPageReqDTO chapterPageReqDTO) {
        PageResult<AdminChapterPageRespDTO> pageResult = bookChapterService.queryChapterPage(chapterPageReqDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{chapterId}")
    public Result<AdminChapterDetailRespDTO> queryChapterDetail(@PathVariable Long chapterId) {
        AdminChapterDetailRespDTO chapterDetailRespDTO = bookChapterService.queryChapterDetail(chapterId);
        return Result.success(chapterDetailRespDTO);
    }

    @PatchMapping("/{chapterId}/status")
    public Result<Void> patchChapterStatus(@PathVariable Long chapterId) {
        bookChapterService.patchChapterStatus(chapterId);
        return Result.success();
    }

    @GetMapping("/batch-charge")
    public Result<AdminChapterBatchSetChargeRespDTO> setChapterChargeBatch(AdminChapterBatchSetChargeReqDTO chapterBatchSetChargeReqDTO) {
        AdminChapterBatchSetChargeRespDTO chapterBatchSetChargeRespDTO = bookChapterService.setChapterChargeBatch(chapterBatchSetChargeReqDTO);
        return Result.success();

    }


}