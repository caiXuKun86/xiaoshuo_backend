package com.kun.service.comment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.service.comment.domain.CommentParagraphStat;
import com.kun.service.comment.dto.resp.BubbleCommentCountQueryRespDTO;

/**
* @author Lenovo
* @description 针对表【comment_paragraph_stat(段落评论聚合统计表)】的数据库操作Service
* @createDate 2026-10-02 19:42:59
*/
public interface CommentParagraphStatService extends IService<CommentParagraphStat> {

    BubbleCommentCountQueryRespDTO getParagraphBubbles(Long chapterId);

    void incrParagraphCommentCount(Long bookId, Long chapterId, Integer paragraphIndex);
}
