package com.kun.service.book.controller.admin;

import com.kun.common.core.aop.RequireRole;
import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.dto.admin.req.AdminBookInfoPageReqDTO;
import com.kun.service.book.dto.admin.resp.AdminBookInfoDetailRespDTO;
import com.kun.service.book.dto.admin.resp.AdminBookInfoPageRespDTO;
import com.kun.service.book.service.admin.AdminBookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequireRole
@RestController
@RequestMapping("/admin/book")
@RequiredArgsConstructor
public class AdminBookInfoController {

    private final AdminBookInfoService bookInfoService;

    @GetMapping("/list")
    public Result<PageResult<AdminBookInfoPageRespDTO>> queryBookInfoPage(AdminBookInfoPageReqDTO bookInfoPageReqDTO) {
        PageResult<AdminBookInfoPageRespDTO> pageResult = bookInfoService.queryBookInfoPage(bookInfoPageReqDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{bookId}")
    public Result<AdminBookInfoDetailRespDTO> queryBookInfoDetail(@PathVariable Long bookId) {
        AdminBookInfoDetailRespDTO bookInfoDetailRespDTO = bookInfoService.queryBookInfoDetail(bookId);
        return Result.success(bookInfoDetailRespDTO);
    }

    @PatchMapping("/{bookId}/status")
    public Result<Void> patchBookInfoStatus(@PathVariable Long bookId, Integer status) {
        bookInfoService.patchBookInfoStatus(bookId, status);
        return Result.success();

    }

    @DeleteMapping("/{bookId}")
    public Result<Void> deleteBookInfo(@PathVariable Long bookId) {
        bookInfoService.deleteBookInfo(bookId);
        return Result.success();
    }


}
