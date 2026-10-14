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

@Slf4j
@Component
@RequiredArgsConstructor
public class ChapterDraftHandler implements ChapterStatusHandler {

    private final BookChapterService bookChapterService;
    private final BookInfoMapper bookInfoMapper;
    private final CacheUtil cacheUtil;

    @Override
    public ChapterStatusEnum getTargetStatus() {
        return ChapterStatusEnum.DRAFT; // 对应 status = 0
    }

    //修改状态 扣减字数 若最新章节更改名称
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(BookChapter bookChapter, ChapterStatusContext context) {
        Integer status = bookChapter.getStatus();
        if (!status.equals(ChapterStatusEnum.PUBLISHED.getCode())
                && !status.equals(ChapterStatusEnum.AUDITING.getCode())) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }


        boolean update = bookChapterService.lambdaUpdate()
                .set(BookChapter::getStatus, ChapterStatusEnum.DRAFT.getCode())
                .eq(BookChapter::getId, bookChapter.getId())
                .ne(BookChapter::getStatus, ChapterStatusEnum.DRAFT.getCode())
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);

        }

        Long bookId = bookChapter.getBookId();
        Long count = bookInfoMapper.selectCount(new LambdaQueryWrapper<BookInfo>()
                .eq(BookInfo::getId, bookId)
                .eq(BookInfo::getLatestChapterId, bookChapter.getId()));
        boolean isNewestChapter = count > 0;
        BookChapter latestChapter = null;
        if (isNewestChapter) {
            latestChapter = bookChapterService.lambdaQuery()
                    .eq(BookChapter::getBookId, bookId)
                    .eq(BookChapter::getStatus, ChapterStatusEnum.PUBLISHED.getCode())
                    .orderByDesc(BookChapter::getChapterIndex)
                    .last("LIMIT 1")
                    .one();
        }
        if(status.equals(ChapterStatusEnum.PUBLISHED.getCode())){
            int update1 = bookInfoMapper.update(
                    new LambdaUpdateWrapper<BookInfo>()
                            .eq(BookInfo::getId, bookId)
                            .setSql("word_count=word_count-" + bookChapter.getWordCount())
                            .set(isNewestChapter, BookInfo::getLatestChapterId, latestChapter != null ? latestChapter.getId() : null)
                            .set(isNewestChapter, BookInfo::getLatestChapterName, latestChapter != null ? latestChapter.getChapterName() : "")
                            .set(isNewestChapter, BookInfo::getLatestChapterTime, latestChapter != null ? latestChapter.getPublishTime() : null)
            );
            if (update1 < 1) {
                throw new BusinessException(ResultCode.OPERATION_FAILED);

            }
        }

        cacheUtil.delete(String.format(RedisKeyConstants.CACHE_BOOK_CATALOG, bookId, "ASC"));
        cacheUtil.delete(String.format(RedisKeyConstants.CACHE_BOOK_CATALOG, bookId, "DESC"));
        cacheUtil.delete(String.format(RedisKeyConstants.BOOK_INFO_PREFIX, bookId));


    }
}