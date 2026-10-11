package com.kun.service.book.service.admin.stragety;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kun.common.core.enums.ChapterStatusEnum;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.common.redis.util.CacheUtil;
import com.kun.service.book.domain.BookChapter;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.mapper.BookInfoMapper;
import com.kun.service.book.service.BookChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChapterPublishHandler implements ChapterStatusHandler {

    private final BookChapterService bookChapterService;
    private final BookInfoMapper bookInfoMapper;
    private final CacheUtil cacheUtil;

    @Override
    public ChapterStatusEnum getTargetStatus() {
        return ChapterStatusEnum.PUBLISHED; // 对应 status = 2
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(BookChapter bookChapter, ChapterStatusContext context) {
        if (!bookChapter.getStatus().equals(ChapterStatusEnum.AUDITING.getCode())) {
            throw new BusinessException(ResultCode.CHAPTER_ALREADY_AUDITED);
        }

        Long bookId = bookChapter.getBookId();
        boolean exists = bookChapterService.lambdaQuery()
                .eq(BookChapter::getBookId, bookId)
                .eq(BookChapter::getStatus, ChapterStatusEnum.AUDITING.getCode())
                .le(BookChapter::getChapterIndex, bookChapter.getChapterIndex())
                .exists();
        if (exists) {
            throw new BusinessException(ResultCode.CHAPTER_PRE_AWAIT_AUDITING);
        }

        boolean update = bookChapterService.lambdaUpdate()
                .set(BookChapter::getStatus, ChapterStatusEnum.PUBLISHED.getCode())
                .eq(BookChapter::getId, bookChapter.getId())
                .eq(BookChapter::getStatus,ChapterStatusEnum.AUDITING.getCode())
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);

        }

        Long count = bookInfoMapper.selectCount(new LambdaQueryWrapper<BookInfo>()
                .eq(BookInfo::getId, bookId)
                .ge(BookInfo::getLatestChapterId, bookChapter.getId()));
        boolean isNewestChapter = count > 0;
        int update1 = bookInfoMapper.update(
                new LambdaUpdateWrapper<BookInfo>()
                        .eq(BookInfo::getId, bookId)
                        .setSql("word_count=word_count+" + bookChapter.getWordCount())
                        .set(isNewestChapter, BookInfo::getLatestChapterId, bookChapter.getId())
                        .set(isNewestChapter, BookInfo::getLatestChapterName, bookChapter.getChapterName())
                        .set(isNewestChapter, BookInfo::getLatestChapterTime, LocalDateTime.now())
        );
        if (update1 < 1) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);

        }

        cacheUtil.delete(String.format(RedisKeyConstants.CACHE_BOOK_CATALOG, bookId, "ASC"));
        cacheUtil.delete(String.format(RedisKeyConstants.CACHE_BOOK_CATALOG, bookId, "DESC"));
        cacheUtil.delete(String.format(RedisKeyConstants.BOOK_INFO_PREFIX, bookId));
    }
}