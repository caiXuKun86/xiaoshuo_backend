package com.kun.service.book.service.writer.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.BookStatusEnum;
import com.kun.common.core.enums.ChapterStatusEnum;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.oss.template.OssTemplate;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.common.redis.util.CacheUtil;
import com.kun.service.book.domain.Author;
import com.kun.service.book.domain.BookChapter;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.dto.writer.req.WriterChapterDraftAddReqDTO;
import com.kun.service.book.dto.writer.req.WriterChapterUpdateReqDTO;
import com.kun.service.book.dto.writer.resp.*;
import com.kun.service.book.mapper.AuthorMapper;
import com.kun.service.book.mapper.BookChapterMapper;
import com.kun.service.book.mapper.BookInfoMapper;
import com.kun.service.book.service.writer.WriterBookChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lenovo
 * @description 针对表【book_chapter(章节目录表)】的数据库操作Service实现
 * @createDate 2026-09-25 10:39:19
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class WriterBookChapterServiceImpl extends ServiceImpl<BookChapterMapper, BookChapter> implements WriterBookChapterService {

    private final BookInfoMapper bookInfoMapper;
    private final OssTemplate ossTemplate;
    private final AuthorMapper authorMapper;
    private final CacheUtil cacheUtil;

    @Override
    public List<WriterChapterListRespDTO> queryBookChapterList(Long bookId, String sortOrder) {

        BookInfo bookInfo = validateBookOwnership(bookId);
        if (!sortOrder.equals("ASC") && !sortOrder.equals("DESC")) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        List<BookChapter> bookChapterList = this.lambdaQuery().eq(BookChapter::getBookId, bookInfo.getId())
                .orderBy(true, sortOrder.equals("ASC"), BookChapter::getChapterIndex)
                .list();
        return BeanUtil.copyToList(bookChapterList, WriterChapterListRespDTO.class);
    }

    @Override
    public WriterChapterDetailRespDTO queryBookChapter(Long chapterId) {

        BookChapter bookChapter = this.getById(chapterId);
        if (bookChapter == null) {
            throw new BusinessException(ResultCode.CHAPTER_NOT_FOUND);
        }
        validateBookOwnership(bookChapter.getBookId());
        WriterChapterDetailRespDTO chapterDetailRespDTO = new WriterChapterDetailRespDTO();
        BeanUtil.copyProperties(bookChapter, chapterDetailRespDTO);


        String content = null;
        try {
            content = ossTemplate.readChapterContent(bookChapter.getOssPath());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.OSS_CONTENT_FETCH_FAIL);
        }
        if (StrUtil.isBlank(content)) {
            throw new BusinessException(ResultCode.OSS_CONTENT_FETCH_FAIL);
        }
        chapterDetailRespDTO.setContent(content);
        return chapterDetailRespDTO;

    }

    @Override
    public WriterChapterDraftAddRespDTO addChapterDraft(WriterChapterDraftAddReqDTO chapterDraftAddReqDTO) {
        Long bookId = chapterDraftAddReqDTO.getBookId();
        Integer chapterIndex = chapterDraftAddReqDTO.getChapterIndex();
        String chapterName = chapterDraftAddReqDTO.getChapterName();
        Integer isCharge = chapterDraftAddReqDTO.getIsCharge();
        Integer requiredPoints = chapterDraftAddReqDTO.getRequiredPoints();
        String content = StrUtil.nullToDefault(chapterDraftAddReqDTO.getContent(), "");
        // 1. 基础参数非空校验
        if (ObjUtil.hasNull(bookId, chapterIndex) || StrUtil.isBlank(chapterName)) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        // 2. 作家作品属主校验 (防水平越权)
        validateBookOwnership(bookId);
        //校验是否发布了上一章节
        boolean exists = this.lambdaQuery()
                .eq(BookChapter::getBookId, bookId)
                .eq(chapterIndex > 1, BookChapter::getChapterIndex, chapterIndex - 1)
                .exists();
        if(!exists){
            throw new BusinessException(ResultCode.CHAPTER_LAST_NOT_PUBLISH);
        }
        // 3. 校验该序号在数据库中是否已有记录
        BookChapter oldChapter = this.lambdaQuery()
                .eq(BookChapter::getBookId, bookId)
                .eq(BookChapter::getChapterIndex, chapterIndex)
                .one();
        // 4. 【关键防御】前置校验：如果是已有章节，已发布或待审核状态下严禁通过草稿接口直接覆盖！
        if (oldChapter != null) {
            Integer oldStatus = oldChapter.getStatus();
            if (!ChapterStatusEnum.DRAFT.getCode().equals(oldStatus)) {
                throw new BusinessException(ResultCode.CHAPTER_CANNOT_EDIT, "该章节已提交或待审核"); // 待审核中，禁止覆盖为草稿
            }

        }
        // 5. 校验全部通过后，才执行 OSS 正文上传 (安全防污染)
        String ossPath;
        try {
            ossPath = ossTemplate.uploadChapterContent(
                    String.format(OssTemplate.UPLOAD_CHAPTER, bookId, chapterIndex),
                    content
            );
        } catch (Exception e) {
            log.error("章节草稿上传 OSS 失败, bookId: {}, chapterIndex: {}", bookId, chapterIndex, e);
            throw new BusinessException(ResultCode.UPLOAD_CONTENT_FAILED);
        }
        // 6. 统计字数与段落数
        int wordCount = computeContentLength(content);
        int paragraphCount = countParagraphs(content);
        // 7. 构建待持久化实体
        BookChapter bookChapter = BeanUtil.copyProperties(chapterDraftAddReqDTO, BookChapter.class);
        bookChapter.setOssPath(ossPath);
        bookChapter.setParagraphCount(paragraphCount);
        bookChapter.setWordCount(wordCount);
        bookChapter.setIsCharge(ObjUtil.defaultIfNull(isCharge, 0));
        bookChapter.setRequiredPoints(ObjUtil.defaultIfNull(requiredPoints, 0));
        // 强制显式打上草稿状态
        bookChapter.setStatus(ChapterStatusEnum.DRAFT.getCode());
        LocalDateTime now = LocalDateTime.now();
        bookChapter.setUpdateTime(now);
        if (oldChapter == null) {
            // 新建草稿 (INSERT)
            bookChapter.setCreateTime(now);
            boolean save = this.save(bookChapter);
            if (!save) {
                throw new BusinessException(ResultCode.OPERATION_FAILED);
            }
        } else {
            // 更新草稿 (UPDATE)
            bookChapter.setId(oldChapter.getId());
            boolean update = this.updateById(bookChapter);
            if (!update) {
                throw new BusinessException(ResultCode.OPERATION_FAILED);
            }
        }
        // 8. 组装契约响应数据返回给前端 (提供 ID、字数对齐与同步时间)
        return WriterChapterDraftAddRespDTO.builder()
                .chapterId(bookChapter.getId())
                .bookId(bookId)
                .chapterIndex(chapterIndex)
                .chapterName(chapterName)
                .wordCount(wordCount)
                .status(ChapterStatusEnum.DRAFT.getCode())
                .updateTime(now)
                .build();


    }

    @Override
    public WriterChapterPublishRespDTO publishChapter(WriterChapterDraftAddReqDTO chapterPublishReqDTO) {
        Long bookId = chapterPublishReqDTO.getBookId();
        Integer chapterIndex = chapterPublishReqDTO.getChapterIndex();
        String chapterName = chapterPublishReqDTO.getChapterName();
        String content = chapterPublishReqDTO.getContent();
        Integer isCharge = chapterPublishReqDTO.getIsCharge();
        Integer requiredPoints = chapterPublishReqDTO.getRequiredPoints();

        if (ObjUtil.hasNull(bookId, chapterIndex, chapterName, isCharge)) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        if (StrUtil.isBlank(chapterName)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "章节名称不能为空");
        }
        if (StrUtil.isBlank(content)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "章节正文不能为空");
        }
        //收费必须传入requiredPoints
        if (isCharge == 1 && requiredPoints == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "付费章节必须指定消费积分");
        }
        BookInfo bookInfo = validateBookOwnership(bookId);

        if (bookInfo.getBookStatus().equals(BookStatusEnum.FINISHED.getCode())) {
            throw new BusinessException(ResultCode.BOOK_IS_OVER);
        }

        //校验是否发布了上一章节
        boolean exists = this.lambdaQuery()
                .eq(BookChapter::getBookId, bookId)
                .eq(chapterIndex > 1, BookChapter::getChapterIndex, chapterIndex - 1)
                .exists();
        if(!exists){
            throw new BusinessException(ResultCode.CHAPTER_LAST_NOT_PUBLISH);
        }

        String ossPath;
        try {
            ossPath = ossTemplate.uploadChapterContent(String.format(OssTemplate.UPLOAD_CHAPTER, bookId, chapterIndex), content);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UPLOAD_CONTENT_FAILED);
        }

        BookChapter bookChapter = this.lambdaQuery()
                .eq(BookChapter::getBookId, bookId)
                .eq(BookChapter::getChapterIndex, chapterIndex)
                .eq(BookChapter::getStatus, ChapterStatusEnum.DRAFT.getCode())
                .one();
        int wordCount = computeContentLength(content);
        int paragraphCount = countParagraphs(content);

        if (bookChapter != null) {
            boolean update = this.lambdaUpdate()
                    .set(BookChapter::getChapterName, chapterName)
                    .set(BookChapter::getIsCharge, isCharge)
                    .set(BookChapter::getRequiredPoints, requiredPoints)
                    .set(BookChapter::getWordCount, wordCount)
                    .set(BookChapter::getParagraphCount, paragraphCount)
                    .set(BookChapter::getStatus, ChapterStatusEnum.AUDITING.getCode())
                    .eq(BookChapter::getId, bookChapter.getId())
                    .eq(BookChapter::getStatus, ChapterStatusEnum.DRAFT.getCode())
                    .update();
            if (!update) {
                throw new BusinessException(ResultCode.OPERATION_FAILED);
            }
            WriterChapterPublishRespDTO chapterPublishRespDTO = new WriterChapterPublishRespDTO();
            chapterPublishRespDTO.setChapterId(bookChapter.getId());
            chapterPublishRespDTO.setBookId(bookId);
            chapterPublishRespDTO.setChapterIndex(chapterIndex);
            chapterPublishRespDTO.setWordCount(wordCount);
            chapterPublishRespDTO.setIsCharge(isCharge);
            chapterPublishRespDTO.setRequiredPoints(requiredPoints);
            chapterPublishRespDTO.setStatus(ChapterStatusEnum.AUDITING.getCode());
            chapterPublishRespDTO.setCreateTime(LocalDateTime.now());
            return chapterPublishRespDTO;

        } else {
            bookChapter = new BookChapter();
            BeanUtil.copyProperties(chapterPublishReqDTO, bookChapter);
            bookChapter.setOssPath(ossPath);
            bookChapter.setWordCount(wordCount);
            bookChapter.setParagraphCount(paragraphCount);
            bookChapter.setStatus(ChapterStatusEnum.AUDITING.getCode());
            boolean save = this.save(bookChapter);
            if (!save) {
                throw new BusinessException(ResultCode.OPERATION_FAILED);
            }
            WriterChapterPublishRespDTO dto = new WriterChapterPublishRespDTO();
            BeanUtil.copyProperties(bookChapter, dto);
            dto.setCreateTime(LocalDateTime.now());
            return dto;
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WriterChapterUpdateRespDTO updateChapter(WriterChapterUpdateReqDTO chapterUpdateReqDTO, Long chapterId) {
        String chapterName = chapterUpdateReqDTO.getChapterName();
        String content = chapterUpdateReqDTO.getContent();

        BookChapter bookChapter = this.getById(chapterId);

        if (bookChapter == null) {
            throw new BusinessException(ResultCode.CHAPTER_NOT_FOUND);
        }
        if (!bookChapter.getStatus().equals(ChapterStatusEnum.PUBLISHED.getCode())) {
            throw new BusinessException(ResultCode.CHAPTER_CANNOT_EDIT, "只能编辑已发布得章节");
        }
        Long bookId = bookChapter.getBookId();
        validateBookOwnership(bookId);

        int newCount = computeContentLength(content);
        int newParagraphCount = countParagraphs(content);

        boolean update = this.lambdaUpdate()
                .eq(BookChapter::getId, chapterId)
                .set(StrUtil.isNotBlank(chapterName), BookChapter::getChapterName, chapterName)
                .set(BookChapter::getWordCount, newCount)
                .set(BookChapter::getParagraphCount, newParagraphCount)
                .set(BookChapter::getStatus, ChapterStatusEnum.AUDITING.getCode())
                .eq(BookChapter::getStatus, ChapterStatusEnum.PUBLISHED.getCode())
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }


        Long count = bookInfoMapper.selectCount(new LambdaQueryWrapper<BookInfo>()
                .eq(BookInfo::getId, bookId)
                .le(BookInfo::getLatestChapterId, bookChapter.getId()));
        boolean isNewestChapter = count > 0;
        BookChapter latestChapter = null;
        if (isNewestChapter) {
            latestChapter = this.lambdaQuery()
                    .eq(BookChapter::getBookId, bookId)
                    .eq(BookChapter::getStatus, ChapterStatusEnum.PUBLISHED.getCode())
                    .orderByDesc(BookChapter::getChapterIndex)
                    .last("LIMIT 1")
                    .one();
        }
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

        cacheUtil.delete(String.format(RedisKeyConstants.CACHE_BOOK_CATALOG, bookId, "ASC"));
        cacheUtil.delete(String.format(RedisKeyConstants.CACHE_BOOK_CATALOG, bookId, "DESC"));
        cacheUtil.delete(String.format(RedisKeyConstants.BOOK_INFO_PREFIX, bookId));
        WriterChapterUpdateRespDTO dto = new WriterChapterUpdateRespDTO();
        dto.setChapterId(chapterId);
        dto.setChapterIndex(bookChapter.getChapterIndex());
        dto.setWordCount(newCount);
        dto.setUpdateTime(LocalDateTime.now());
        dto.setChapterName(StrUtil.isNotBlank(chapterName) ? chapterName : bookChapter.getChapterName());
        return dto;
    }

    @Override
    public void deleteChapter(Long chapterId) {
        BookChapter bookChapter = this.getById(chapterId);

        if (bookChapter == null) {
            throw new BusinessException(ResultCode.CHAPTER_NOT_FOUND);
        }
        if (!bookChapter.getStatus().equals(ChapterStatusEnum.DRAFT.getCode())) {
            throw new BusinessException(ResultCode.CHAPTER_CANNOT_EDIT, "只能删除草稿章节");
        }
        Long bookId = bookChapter.getBookId();
        validateBookOwnership(bookId);
        boolean delete = this.removeById(chapterId);
        if (!delete) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "删除失败");
        }
    }

    private int computeContentLength(String content) {
        int count = 0;
        for (char c : content.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fa5') {
                count++;
            }
        }
        return count;
    }

    private int countParagraphs(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return (int) text.lines()
                .filter(line -> !line.isBlank())
                .count();
    }

    /**
     * 获取当前登录用户的作家
     */
    private Author getCurrentAuthor() {
        Long userId = UserContextHolder.get().getUserId();
        Author author = authorMapper.selectOne(
                new LambdaQueryWrapper<Author>().eq(Author::getUserId, userId)
        );
        if (author == null || author.getStatus() == 0) {
            throw new BusinessException(ResultCode.AUTHOR_BANNED);
        }
        return author;
    }

    /**
     * 获取当前登录用户的作家 ID
     */
    private Long getCurrentAuthorId() {
        return getCurrentAuthor().getId();
    }

    /**
     * 校验当前作品是否属于当前作家
     */
    private BookInfo validateBookOwnership(Long bookId) {
        Long authorId = getCurrentAuthorId();
        BookInfo book = bookInfoMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        // 彻底杜绝水平越权：即使黑客篡改了 bookId 也无法操作他人作品
        if (!book.getAuthorId().equals(authorId)) {
            throw new BusinessException(ResultCode.AUTHOR_NOT_PERMITTED); // 403: 无权操作他人作品
        }
        return book;
    }
}




