package com.kun.service.comment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.common.database.page.PageResult;
import com.kun.service.comment.domain.CommentInfo;
import com.kun.service.comment.dto.req.*;
import com.kun.service.comment.dto.resp.*;

/**
* @author Lenovo
* @description 针对表【comment_info(评论互动主表)】的数据库操作Service
* @createDate 2026-10-02 19:42:59
*/
public interface CommentInfoService extends IService<CommentInfo> {


    PageResult<CommentInfoPageRespDTO> queryCommentInfoPage(CommentInfoPageReqDTO commentPageReqDTO);

    CommentPublishRespDTO publishComment(CommentPublishReqDTO commentPublishReqDTO);

    PageResult<CommentRepliesPageRespDTO> queryCommentReplyPage(Long rootId, CommentRepliesPageReqDTO commentRepliesPageReqDTO);

    CommentReplyRespDTO replyComment(CommentReplyReqDTO commentReplyReqDTO);

    PageResult<ParagraphCommentPageRespDTO> queryParagraphCommentPage(ParagraphCommentPageReqDTO paragraphCommentPageReqDTO);

    CommentLikeRespDTO likeComment(Long commentId, CommentLikeReqDTO commentLikeReqDTO);

    void deleteComment(Long commentId);
}
