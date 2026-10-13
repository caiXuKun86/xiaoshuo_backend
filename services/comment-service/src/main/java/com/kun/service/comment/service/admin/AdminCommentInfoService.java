package com.kun.service.comment.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.common.database.page.PageResult;
import com.kun.service.comment.domain.CommentInfo;
import com.kun.service.comment.dto.admin.req.AdminCommentInfoBatchPatchStatusReqDTO;
import com.kun.service.comment.dto.admin.req.AdminCommentInfoPageReqDTO;
import com.kun.service.comment.dto.admin.resp.AdminCommentDashboardRespDTO;
import com.kun.service.comment.dto.admin.resp.AdminCommentInfoBatchPatchStatusRespDTO;
import com.kun.service.comment.dto.admin.resp.AdminCommentInfoDetailPageRespDTO;
import com.kun.service.comment.dto.admin.resp.AdminCommentInfoPageRespDTO;

/**
 * @author Lenovo
 * @description 针对表【comment_info(评论互动主表)】的数据库操作Service
 * @createDate 2026-10-02 19:42:59
 */
public interface AdminCommentInfoService extends IService<CommentInfo> {


    PageResult<AdminCommentInfoPageRespDTO> queryCommentInfoPage(AdminCommentInfoPageReqDTO commentInfoPageReqDTO);

    AdminCommentInfoDetailPageRespDTO queryCommentInfoDetail(Long commentId);

    void patchCommentInfoStatus(Long commentId, Integer status);

    AdminCommentInfoBatchPatchStatusRespDTO patchCommentInfoStatusBatch(AdminCommentInfoBatchPatchStatusReqDTO commentInfoBatchPatchStatusReqDTO);

    void deleteCommentInfo(Long commentId);

    AdminCommentDashboardRespDTO queryCommentDashboard();
}
