package com.kun.service.comment.service.admin.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.api.client.BookFeignClient;
import com.kun.api.dto.book.BookDTO;
import com.kun.api.dto.book.ChapterDTO;
import com.kun.common.core.enums.*;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.comment.domain.CommentInfo;
import com.kun.service.comment.dto.admin.req.AdminCommentInfoBatchPatchStatusReqDTO;
import com.kun.service.comment.dto.admin.req.AdminCommentInfoPageReqDTO;
import com.kun.service.comment.dto.admin.resp.AdminCommentDashboardRespDTO;
import com.kun.service.comment.dto.admin.resp.AdminCommentInfoBatchPatchStatusRespDTO;
import com.kun.service.comment.dto.admin.resp.AdminCommentInfoDetailPageRespDTO;
import com.kun.service.comment.dto.admin.resp.AdminCommentInfoPageRespDTO;
import com.kun.service.comment.mapper.CommentInfoMapper;
import com.kun.service.comment.service.CommentParagraphStatService;
import com.kun.service.comment.service.admin.AdminCommentInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lenovo
 * @description 针对表【comment_info(评论互动主表)】的数据库操作Service实现
 * @createDate 2026-10-02 19:42:59
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AdminCommentInfoServiceImpl extends ServiceImpl<CommentInfoMapper, CommentInfo> implements AdminCommentInfoService {

    private final BookFeignClient bookFeignClient;
    private final CommentParagraphStatService commentParagraphStatService;

    @Override
    public PageResult<AdminCommentInfoPageRespDTO> queryCommentInfoPage(AdminCommentInfoPageReqDTO reqDTO) {

        Long bookId = reqDTO.getBookId();
        Long chapterId = reqDTO.getChapterId();
        Integer commentType = reqDTO.getCommentType();
        Integer status = reqDTO.getStatus();
        Boolean rootOnly = reqDTO.getRootOnly();
        String keyWord = reqDTO.getKeyWord();
        String userNickName = reqDTO.getUserNickName();
        Long userId = reqDTO.getUserId();

        checkBookAndChapter(bookId, chapterId);
        LambdaQueryWrapper<CommentInfo> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(bookId != null, CommentInfo::getBookId, reqDTO.getBookId())
                .eq(chapterId != null, CommentInfo::getChapterId, chapterId)
                .eq(commentType != null, CommentInfo::getCommentType, commentType)
                .eq(rootOnly != null && rootOnly, CommentInfo::getRootId, 0L)   // 核心：只查顶级根评论！
                .eq(status != null, CommentInfo::getStatus, status)
                .eq(userId != null, CommentInfo::getUserId, userId)
                .likeRight(StrUtil.isNotBlank(keyWord), CommentInfo::getContent, keyWord)
                .likeRight(StrUtil.isNotBlank(userNickName), CommentInfo::getUserNickname, userNickName);

        // 5. 排序规则：若未传自定义排序，默认按更新时间倒序
        if (!StringUtils.hasText(reqDTO.getSortField())) {
            queryWrapper.orderByDesc(CommentInfo::getCreateTime);
        }

        // 6. 执行分页查询
        Page<CommentInfo> page = this.page(reqDTO.toPage(), queryWrapper);
        List<CommentInfo> records = page.getRecords();
        List<Long> rootIds = records.stream().map(CommentInfo::getId).collect(Collectors.toList());
        if (CollUtil.isEmpty(rootIds)) {
            return PageResult.empty();
        }


        return PageResult.of(page, commentInfo -> BeanUtil.copyProperties(commentInfo, AdminCommentInfoPageRespDTO.class));
    }

    @Override
    public AdminCommentInfoDetailPageRespDTO queryCommentInfoDetail(Long commentId) {
        CommentInfo commentInfo = this.getById(commentId);
        if (commentInfo == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        AdminCommentInfoDetailPageRespDTO dto = BeanUtil.copyProperties(commentInfo, AdminCommentInfoDetailPageRespDTO.class);
        if (commentInfo.getReplyCount() > 0) {
            List<CommentInfo> replyList = this.lambdaQuery()
                    .eq(CommentInfo::getParentId, commentInfo.getId())
                    .list();
            if (CollUtil.isNotEmpty(replyList)) {
                dto.setReplies(replyList.stream().map(reply ->
                        BeanUtil.copyProperties(reply, AdminCommentInfoDetailPageRespDTO.Reply.class)
                ).toList());
            }
        }
        return dto;


    }

    @Override
    public void patchCommentInfoStatus(Long commentId, Integer status) {
        if (CommentStatusEnum.getByCode(status) == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        CommentInfo commentInfo = this.getById(commentId);
        if (commentInfo == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        boolean update = this.lambdaUpdate()
                .set(CommentInfo::getId, commentId)
                .set(CommentInfo::getStatus, status)
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
    }

    @Override
    public AdminCommentInfoBatchPatchStatusRespDTO patchCommentInfoStatusBatch(AdminCommentInfoBatchPatchStatusReqDTO commentInfoBatchPatchStatusReqDTO) {

        List<Long> commentIds = commentInfoBatchPatchStatusReqDTO.getCommentIds();
        Integer status = commentInfoBatchPatchStatusReqDTO.getStatus();
        if (CommentStatusEnum.getByCode(status) == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        int update = baseMapper.update(new LambdaUpdateWrapper<CommentInfo>()
                .in(CommentInfo::getId, commentIds)
                .eq(CommentInfo::getStatus, status)
        );
        return new AdminCommentInfoBatchPatchStatusRespDTO(update);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentInfo(Long commentId) {
        CommentInfo commentInfo = this.getById(commentId);
        if (commentInfo == null || commentInfo.getStatus().equals(CommentStatusEnum.NORMAL.getCode())) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        boolean update = this.lambdaUpdate()
                .eq(CommentInfo::getId, commentId)
                .set(CommentInfo::getStatus, CommentStatusEnum.DELETED.getCode())
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        if (commentInfo.getRootId() != 0) {
            //rootId 回复数量+1
            boolean update1 = this.lambdaUpdate()
                    .setSql("reply_count = reply_count - 1")
                    .eq(CommentInfo::getId, commentInfo.getRootId())
                    .update();
            if (!update1) {
                throw new BusinessException(ResultCode.OPERATION_FAILED);
            }
        }
        // 若回复的是段落评论吐槽，同样递增该段落气泡统计
        if (Integer.valueOf(3).equals(commentInfo.getCommentType())
                && commentInfo.getParagraphIndex() != null
                && commentInfo.getParagraphIndex() > 0) {
            commentParagraphStatService.incrParagraphCommentCount(
                    commentInfo.getBookId(),
                    commentInfo.getChapterId(),
                    commentInfo.getParagraphIndex(),
                    false
            );
        }
    }

    @Override
    public AdminCommentDashboardRespDTO queryCommentDashboard() {
        Long totalComments = this.lambdaQuery()
                .count();

        Long auditingComments = this.lambdaQuery()
                .eq(CommentInfo::getStatus,CommentStatusEnum.AUDITING.getCode())
                .count();
        Long paragraphComments = this.lambdaQuery()
                .eq(CommentInfo::getCommentType, CommentTypeEnum.PARAGRAPH.getCode())
                .count();
        return new AdminCommentDashboardRespDTO(auditingComments,totalComments,paragraphComments);

    }

    private void checkBookAndChapter(Long bookId, Long chapterId) {
        if (ObjUtil.hasNull(bookId, chapterId)) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        Result<BookDTO> bookResult = bookFeignClient.getBookById(bookId);
        if (bookResult == null || bookResult.getCode() != 200 || bookResult.getData() == null) {
            throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE, "图书查找失败");
        }
        BookDTO bookDTO = bookResult.getData();
        if (!bookDTO.getStatus().equals(BookOpStatusEnum.ON_SHELF.getCode())) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        if (chapterId != 0) {
            Result<ChapterDTO> chapterResult = bookFeignClient.getChapterById(chapterId);
            if (chapterResult == null || chapterResult.getCode() != 200 || chapterResult.getData() == null) {
                throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE, "章节查找失败");
            }
            ChapterDTO chapterDTO = chapterResult.getData();
            if (!chapterDTO.getStatus().equals(ChapterStatusEnum.PUBLISHED.getCode())) {
                throw new BusinessException(ResultCode.CHAPTER_NOT_FOUND);
            }
        }
    }
}




