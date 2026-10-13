package com.kun.service.comment.controller;


import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.comment.dto.req.*;
import com.kun.service.comment.dto.resp.*;
import com.kun.service.comment.service.CommentInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/comment")
public class CommentInfoController {

    private final CommentInfoService commentInfoService;

    @GetMapping("/list")
    public Result<PageResult<CommentInfoPageRespDTO>> queryCommentInfoPage(CommentInfoPageReqDTO commentInfoPageReqDTO) {
        PageResult<CommentInfoPageRespDTO> result = commentInfoService.queryCommentInfoPage(commentInfoPageReqDTO);
        return Result.success(result);
    }


    @PostMapping("/publish")
    public Result<CommentPublishRespDTO> publishComment(@RequestBody CommentPublishReqDTO commentPublishReqDTO) {
        CommentPublishRespDTO commentPublishRespDTO = commentInfoService.publishComment(commentPublishReqDTO);
        return Result.success(commentPublishRespDTO);
    }

    @GetMapping("/{rootId}/replies")
    public Result<PageResult<CommentRepliesPageRespDTO>> queryCommentReplyPage(@PathVariable("rootId") Long rootId, CommentRepliesPageReqDTO commentRepliesPageReqDTO) {
        PageResult<CommentRepliesPageRespDTO> result = commentInfoService.queryCommentReplyPage(rootId, commentRepliesPageReqDTO);
        return Result.success(result);
    }

    @PostMapping("/reply")
    public Result<CommentReplyRespDTO> replyComment(@RequestBody CommentReplyReqDTO commentReplyReqDTO) {
        CommentReplyRespDTO commentReplyRespDTO = commentInfoService.replyComment(commentReplyReqDTO);
        return Result.success(commentReplyRespDTO);
    }

    @GetMapping("/paragraph/list")
    public Result<PageResult<ParagraphCommentPageRespDTO>> queryParagraphCommentPage( ParagraphCommentPageReqDTO paragraphCommentPageReqDTO) {

        PageResult<ParagraphCommentPageRespDTO> result = commentInfoService.queryParagraphCommentPage(paragraphCommentPageReqDTO);
        return Result.success(result);
    }

    @PostMapping("/{commentId}/like")
    public Result<CommentLikeRespDTO> likeComment(@PathVariable("commentId") Long commentId, @RequestBody CommentLikeReqDTO commentLikeReqDTO) {
        CommentLikeRespDTO commentLikeRespDTO = commentInfoService.likeComment(commentId, commentLikeReqDTO);
        return Result.success(commentLikeRespDTO);
    }

    @DeleteMapping("/{commentId}")
    public Result<Void> deleteComment(@PathVariable("commentId") Long commentId) {
        commentInfoService.deleteComment(commentId);

        return Result.success();

    }


}
