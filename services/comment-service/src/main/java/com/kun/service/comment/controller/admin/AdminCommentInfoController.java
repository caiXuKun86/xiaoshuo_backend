package com.kun.service.comment.controller.admin;


import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.comment.dto.admin.req.AdminCommentInfoBatchPatchStatusReqDTO;
import com.kun.service.comment.dto.admin.req.AdminCommentInfoPageReqDTO;
import com.kun.service.comment.dto.admin.resp.AdminCommentDashboardRespDTO;
import com.kun.service.comment.dto.admin.resp.AdminCommentInfoBatchPatchStatusRespDTO;
import com.kun.service.comment.dto.admin.resp.AdminCommentInfoDetailPageRespDTO;
import com.kun.service.comment.dto.admin.resp.AdminCommentInfoPageRespDTO;
import com.kun.service.comment.service.admin.AdminCommentInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/comment/admin")
public class AdminCommentInfoController {

    private final AdminCommentInfoService commentInfoService;

    @GetMapping("/list")
    public Result<PageResult<AdminCommentInfoPageRespDTO>> queryCommentInfoPage(AdminCommentInfoPageReqDTO commentInfoPageReqDTO) {
        PageResult<AdminCommentInfoPageRespDTO> result = commentInfoService.queryCommentInfoPage(commentInfoPageReqDTO);
        return Result.success(result);
    }

    @GetMapping("/{commentId}")
    public Result<AdminCommentInfoDetailPageRespDTO> queryCommentInfoDetail(@PathVariable Long commentId) {
        AdminCommentInfoDetailPageRespDTO commentInfoDetailPageRespDTO = commentInfoService.queryCommentInfoDetail(commentId);
        return Result.success(commentInfoDetailPageRespDTO);
    }


    @PatchMapping("/{commentId}/status")
    public Result<Void> patchCommentInfoStatus(@PathVariable Long commentId, Integer status) {
        commentInfoService.patchCommentInfoStatus(commentId, status);
        return Result.success();
    }

    @PatchMapping("/batch-status")
    public Result<AdminCommentInfoBatchPatchStatusRespDTO> patchCommentInfoStatusBatch(@RequestBody AdminCommentInfoBatchPatchStatusReqDTO commentInfoBatchPatchStatusReqDTO) {
        AdminCommentInfoBatchPatchStatusRespDTO respDTO = commentInfoService.patchCommentInfoStatusBatch(commentInfoBatchPatchStatusReqDTO);
        return Result.success(respDTO);
    }

    @DeleteMapping("/{commentId}")
    public Result<Void> deleteCommentInfo(@PathVariable Long commentId){
        commentInfoService.deleteCommentInfo(commentId);
        return Result.success();
    }

    @GetMapping("/dashboard/summary")
    public Result<AdminCommentDashboardRespDTO> queryCommentDashboard(){
        AdminCommentDashboardRespDTO commentDashboardRespDTO = commentInfoService.queryCommentDashboard();
        return Result.success(commentDashboardRespDTO);
    }






}
