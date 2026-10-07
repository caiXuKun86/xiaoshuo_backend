package com.kun.service.comment.controller;


import com.kun.common.core.result.Result;
import com.kun.service.comment.dto.resp.BubbleCommentCountQueryRespDTO;
import com.kun.service.comment.service.CommentParagraphStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/comment/paragraph/bubbles")
public class CommentParagraphStatController {
    private final CommentParagraphStatService commentParagraphStatService;


    @GetMapping("/{chapterId}")
    public Result<BubbleCommentCountQueryRespDTO> getParagraphBubbles(@PathVariable("chapterId") Long chapterId) {
        BubbleCommentCountQueryRespDTO result = commentParagraphStatService.getParagraphBubbles(chapterId);
        return Result.success(result);
    }
}
