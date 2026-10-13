package com.kun.service.book.service.writer.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.BookStatusEnum;
import com.kun.common.core.enums.ChapterStatusEnum;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.database.page.PageResult;
import com.kun.common.oss.constants.OSSConstants;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.common.redis.util.CacheUtil;
import com.kun.service.book.domain.Author;
import com.kun.service.book.domain.BookChapter;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.domain.Category;
import com.kun.service.book.dto.writer.req.WriterBookInfoPageReqDTO;
import com.kun.service.book.dto.writer.req.WriterBookInfoUpdateReqDTO;
import com.kun.service.book.dto.writer.req.WriterBookPublishReqDTO;
import com.kun.service.book.dto.writer.resp.WriterBookInfoDetailRespDTO;
import com.kun.service.book.dto.writer.resp.WriterBookInfoPageRespDTO;
import com.kun.service.book.dto.writer.resp.WriterBookPublishRespDTO;
import com.kun.service.book.dto.writer.resp.WriterBookStatsRespDTO;
import com.kun.service.book.mapper.AuthorMapper;
import com.kun.service.book.mapper.BookChapterMapper;
import com.kun.service.book.mapper.BookInfoMapper;
import com.kun.service.book.mapper.CategoryMapper;
import com.kun.service.book.service.writer.WriterBookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * @author Lenovo
 * @description 针对表【book_info(图书信息主表)】的数据库操作Service实现
 * @createDate 2026-09-25 10:39:19
 */

@RequiredArgsConstructor
@Service
public class WriterBookInfoServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements WriterBookInfoService {

    private final CacheUtil cacheUtil;
    private final AuthorMapper authorMapper;
    private final CategoryMapper categoryMapper;

    private final BookChapterMapper bookChapterMapper;

    @Override
    public PageResult<WriterBookInfoPageRespDTO> queryMyBookInfoPage(WriterBookInfoPageReqDTO reqDTO) {

        Integer status = reqDTO.getStatus();
        Integer bookStatus = reqDTO.getBookStatus();
        String keyword = reqDTO.getKeyword();
        Long authorId = getCurrentAuthorId();
        LambdaQueryWrapper<BookInfo> queryWrapper = new LambdaQueryWrapper<>();
        // 1. 业务硬约束：只展示已上架的书籍 (status = 1)
        queryWrapper.eq(status != null, BookInfo::getStatus, status)
                .eq(bookStatus != null, BookInfo::getBookStatus, bookStatus)
                .eq(BookInfo::getAuthorId, authorId);

        queryWrapper.and(wq -> wq.like(BookInfo::getBookName, keyword)
                .or().like(BookInfo::getTags, keyword));


        // 5. 排序规则：若未传自定义排序，默认按更新时间倒序
        if (!StringUtils.hasText(reqDTO.getSortField())) {
            queryWrapper.orderByDesc(BookInfo::getLatestChapterTime);
        }

        // 6. 执行分页查询
        Page<BookInfo> page = this.page(reqDTO.toPage(), queryWrapper);

        return PageResult.of(page, bookInfo -> {
            WriterBookInfoPageRespDTO dto = new WriterBookInfoPageRespDTO();
            BeanUtils.copyProperties(bookInfo, dto);
            if (StringUtils.hasText(bookInfo.getTags())) {
                dto.setTags(List.of(bookInfo.getTags().split(",")));
            } else {
                dto.setTags(Collections.emptyList());
            }
            return dto;
        });
    }

    @Override
    public WriterBookInfoDetailRespDTO queryBookInfoDetail(Long bookId) {
        BookInfo bookInfo = validateBookOwnership(bookId);
        WriterBookInfoDetailRespDTO bookInfoDetailRespDTO = new WriterBookInfoDetailRespDTO();

        BeanUtil.copyProperties(bookInfo, bookInfoDetailRespDTO);
        bookInfoDetailRespDTO.setTags(List.of(bookInfo.getTags().split(",")));
        return bookInfoDetailRespDTO;

    }

    @Override
    public WriterBookPublishRespDTO publishBook(WriterBookPublishReqDTO bookPublishReqDTO) {

        String bookName = bookPublishReqDTO.getBookName();
        Integer channelId = bookPublishReqDTO.getChannelId();
        Integer categoryId = bookPublishReqDTO.getCategoryId();
        List<String> tags = bookPublishReqDTO.getTags();
        Integer status = bookPublishReqDTO.getStatus();
        String coverUrl = bookPublishReqDTO.getCoverUrl();


        if (ObjUtil.hasEmpty(channelId, categoryId, status)) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        if (StrUtil.isBlank(bookName)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "书名不能为空");
        }

        if (coverUrl == null) {
            coverUrl = OSSConstants.DEFAULT_COVER_URL;
        }
        Author author = getCurrentAuthor();

        Long count = this.lambdaQuery()
                .eq(BookInfo::getAuthorId, author.getId())
                .eq(BookInfo::getBookName, bookName)
                .count();
        if (count > 0) {
            throw new BusinessException(ResultCode.BOOK_NAME_REPEAT);
        }
        Category channel = categoryMapper.selectById(channelId);
        Category category = categoryMapper.selectById(categoryId);
        if (channel == null || category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }
        BookInfo bookInfo = new BookInfo();
        BeanUtil.copyProperties(bookPublishReqDTO, bookInfo);
        bookInfo.setBookStatus(BookStatusEnum.SERIALIZING.getCode());
        bookInfo.setTags(JSONUtil.toJsonStr(tags));
        bookInfo.setCollectCount(0);
        bookInfo.setScore(new BigDecimal(0));
        bookInfo.setWordCount(0);
        bookInfo.setAuthorId(author.getId());
        bookInfo.setAuthorName(author.getPenName());
        bookInfo.setCategoryName(category.getName());
        boolean save = this.save(bookInfo);
        if (!save) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        WriterBookPublishRespDTO bookPublishRespDTO = new WriterBookPublishRespDTO();
        BeanUtil.copyProperties(bookInfo, bookPublishRespDTO);
        bookPublishRespDTO.setTags(tags);
        return bookPublishRespDTO;
    }

    @Override
    public void updateBookInfo(Long bookId, WriterBookInfoUpdateReqDTO bookInfoUpdateReqDTO) {
        String coverUrl = bookInfoUpdateReqDTO.getCoverUrl();
        String description = bookInfoUpdateReqDTO.getDescription();
        List<String> tags = bookInfoUpdateReqDTO.getTags();
        validateBookOwnership(bookId);
        boolean update = this.lambdaUpdate()
                .eq(StrUtil.isNotBlank(coverUrl), BookInfo::getCoverUrl, coverUrl)
                .eq(StrUtil.isNotBlank(description), BookInfo::getDescription, description)
                .eq(CollUtil.isNotEmpty(tags), BookInfo::getTags, JSONUtil.toJsonStr(tags))
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        cacheUtil.delete(RedisKeyConstants.BOOK_INFO_PREFIX);

    }

    @Override
    public void overBook(Long bookId) {

        validateBookOwnership(bookId);

        boolean update = this.lambdaUpdate()
                .set(BookInfo::getStatus, BookStatusEnum.FINISHED)
                .eq(BookInfo::getId, bookId)
                .eq(BookInfo::getStatus, BookStatusEnum.SERIALIZING)
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);

        }
        cacheUtil.delete(RedisKeyConstants.BOOK_INFO_PREFIX);

    }

    @Override
    public WriterBookStatsRespDTO queryBookStats() {
        Long authorId = getCurrentAuthorId();

        List<BookInfo> bookInfoList = this.lambdaQuery()
                .eq(BookInfo::getAuthorId, authorId)
                .list();

        List<Long> bookIds = bookInfoList.stream().map(BookInfo::getId).toList();
        if(CollUtil.isEmpty(bookInfoList)){
            return WriterBookStatsRespDTO
                    .builder()
                    .totalBooks(0)
                    .publishedChapters(0)
                    .pendingAuditChapters(0)
                    .totalCollectCount(0L)
                    .totalWordCount(0L)
                    .build();
        }
        long totalWordCount = bookInfoList.stream().mapToLong(BookInfo::getWordCount).sum();
        long totalCollectCount = bookInfoList.stream().mapToInt(BookInfo::getCollectCount).sum();

        Long publishedChapters = bookChapterMapper.selectCount(
                new LambdaQueryWrapper<BookChapter>()
                        .in(CollUtil.isNotEmpty(bookIds), BookChapter::getBookId, bookIds)
                        .eq(BookChapter::getStatus, ChapterStatusEnum.PUBLISHED.getCode())
        );
        Long pendingAuditChapters = bookChapterMapper.selectCount(
                new LambdaQueryWrapper<BookChapter>()
                        .in(CollUtil.isNotEmpty(bookIds), BookChapter::getBookId, bookIds)
                        .eq(BookChapter::getStatus, ChapterStatusEnum.AUDITING.getCode())
        );

        return WriterBookStatsRespDTO
                .builder()
                .totalBooks(bookInfoList.size())
                .publishedChapters(publishedChapters.intValue())
                .pendingAuditChapters(pendingAuditChapters.intValue())
                .totalCollectCount(totalCollectCount)
                .totalWordCount(totalWordCount)
                .build();


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
        BookInfo book = baseMapper.selectById(bookId);
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




