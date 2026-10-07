package com.kun.service.book.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.api.client.ShelfFeignClient;
import com.kun.api.dto.shelf.ShelfDTO;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.*;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.common.redis.util.CacheUtil;
import com.kun.service.book.domain.*;
import com.kun.service.book.dto.req.BookPageReqDTO;
import com.kun.service.book.dto.req.BookPublishReqDTO;
import com.kun.service.book.dto.resp.BookCatalogQueryRespDTO;
import com.kun.service.book.dto.resp.BookDetailQueryRespDTO;
import com.kun.service.book.dto.resp.BookPageRespDTO;
import com.kun.service.book.dto.resp.BookPublishRespDTO;
import com.kun.service.book.mapper.*;
import com.kun.service.book.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Lenovo
 * @description 针对表【book_info(图书信息主表)】的数据库操作Service实现
 * @createDate 2026-09-25 10:39:19
 */

@RequiredArgsConstructor
@Service
public class BookInfoServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements BookInfoService {

    private final BookChapterMapper bookChapterMapper;

    private final UserChapterUnlockMapper userChapterUnlockMapper;

    private final ShelfFeignClient shelfFeignClient;
    private final CacheUtil cacheUtil;
    private final AuthorMapper authorMapper;
    private final CategoryMapper categoryMapper;


    @Override
    public PageResult<BookPageRespDTO> pageBook(BookPageReqDTO reqDTO) {
        Integer channelId = reqDTO.getChannelId();
        Long categoryId = reqDTO.getCategoryId();
        Integer bookStatus = reqDTO.getBookStatus();
        Integer wordRange = reqDTO.getWordRange();

        LambdaQueryWrapper<BookInfo> queryWrapper = new LambdaQueryWrapper<>();

        // 1. 业务硬约束：只展示已上架的书籍 (status = 1)
        queryWrapper.eq(BookInfo::getStatus, 1);

        // 2. 分类/频道筛选
        if (categoryId != null && categoryId > 0) {
            // 精确匹配二级分类
            queryWrapper.eq(BookInfo::getCategoryId, categoryId);
        } else if (channelId != null && channelId > 0) {
            // 只选了一级频道 (例如男频/女频)，使用子查询匹配该频道下的所有子分类
            queryWrapper.inSql(BookInfo::getCategoryId, "SELECT id FROM category WHERE parent_id = " + channelId);
        }

        // 3. 连载状态筛选 (-1 为全部)
        if (bookStatus != null && bookStatus != -1) {
            queryWrapper.eq(BookInfo::getBookStatus, bookStatus);
        }

        // 4. 字数区间筛选
        if (wordRange != null && wordRange > 0) {
            switch (wordRange) {
                case 1 -> queryWrapper.le(BookInfo::getWordCount, 300_000);
                case 2 -> queryWrapper.between(BookInfo::getWordCount, 300_000, 1_000_000);
                case 3 -> queryWrapper.between(BookInfo::getWordCount, 1_000_001, 2_000_000);
                case 4 -> queryWrapper.gt(BookInfo::getWordCount, 2_000_000);
                default -> {
                }
            }
        }

        // 5. 排序规则：若未传自定义排序，默认按更新时间倒序
        if (!StringUtils.hasText(reqDTO.getSortField())) {
            queryWrapper.orderByDesc(BookInfo::getLatestChapterTime);
        }

        // 6. 执行分页查询
        Page<BookInfo> page = this.page(reqDTO.toPage(), queryWrapper);

        return PageResult.of(page, bookInfo -> {
            BookPageRespDTO dto = new BookPageRespDTO();
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
    public BookDetailQueryRespDTO queryBookDetailById(Long id) {
        BookDetailQueryRespDTO bookDetailQueryRespDTO = new BookDetailQueryRespDTO();
        String redisKey = String.format(RedisKeyConstants.BOOK_INFO_PREFIX, id);
        BookInfo bookInfo = cacheUtil.queryWithMutex(redisKey, BookInfo.class, () -> this.getById(id), 2L, TimeUnit.HOURS);
        if (bookInfo == null || !bookInfo.getStatus().equals(BookOpStatusEnum.ON_SHELF.getCode())) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        BeanUtil.copyProperties(bookInfo, bookDetailQueryRespDTO);
        bookDetailQueryRespDTO.setTags(List.of(bookInfo.getTags().split(",")));
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            bookDetailQueryRespDTO.setUserInteract(new BookDetailQueryRespDTO.UserInteract(false, null, null));
            return bookDetailQueryRespDTO;
        }
        Result<ShelfDTO> result = shelfFeignClient.getShelfByBookId(id);
        ShelfDTO shelfDTO = result.getData();
        BookDetailQueryRespDTO.UserInteract userInteract = new BookDetailQueryRespDTO.UserInteract();
        userInteract.setIsInBookshelf(shelfDTO.getIsInBookshelf());
        userInteract.setLastReadChapterId(shelfDTO.getLastReadChapterId());
        userInteract.setLastReadChapterName(shelfDTO.getLastReadChapterName());
        bookDetailQueryRespDTO.setUserInteract(userInteract);
        return bookDetailQueryRespDTO;
    }

    @Override
    public BookCatalogQueryRespDTO queryBookCatalogById(Long bookId, String sortOrder) {
        Long userId = UserContextHolder.getUserId();
        BookInfo bookInfo = this.getById(bookId);
        if (bookInfo == null || !bookInfo.getStatus().equals(BookOpStatusEnum.ON_SHELF.getCode())) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        if (!sortOrder.equals("ASC") && !sortOrder.equals("DESC")) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        List<BookChapter> bookChapters = cacheUtil.queryListWithMutex(
                String.format(RedisKeyConstants.CACHE_BOOK_CATALOG, bookId),
                BookChapter.class,
                () -> bookChapterMapper.selectList(new LambdaQueryWrapper<BookChapter>()
                        .eq(BookChapter::getBookId, bookInfo.getId())
                        .eq(BookChapter::getStatus, ChapterStatusEnum.PUBLISHED.getCode())
                ),
                2L,
                TimeUnit.HOURS
        );
        if (sortOrder.equals("DESC")) {
            CollUtil.reverse(bookChapters);
        }
        List<UserChapterUnlock> userChapterUnlocks = userChapterUnlockMapper.selectList(new LambdaQueryWrapper<UserChapterUnlock>()
                .eq(UserChapterUnlock::getUserId, userId)
                .eq(UserChapterUnlock::getBookId, bookId)
        );
        Set<Long> unlockedChapters = userChapterUnlocks.stream().map(UserChapterUnlock::getChapterId).collect(Collectors.toSet());


        BookCatalogQueryRespDTO bookCatalogQueryRespDTO = new BookCatalogQueryRespDTO();
        bookCatalogQueryRespDTO.setBookId(bookId);
        bookCatalogQueryRespDTO.setTotalChapters(bookChapters.size());

        bookCatalogQueryRespDTO.setChapters(bookChapters.stream().map(bookChapter -> {
            BookCatalogQueryRespDTO.ChapterInfoDTO chapterInfoDTO = new BookCatalogQueryRespDTO.ChapterInfoDTO();
            BeanUtil.copyProperties(bookChapter, chapterInfoDTO);
            if (Objects.equals(bookChapter.getIsCharge(), ChapterChargeEnum.FREE.getCode())) {
                chapterInfoDTO.setIsUnlocked(true);
            } else {
                chapterInfoDTO.setIsUnlocked(unlockedChapters.contains(bookChapter.getId()));
            }
            return chapterInfoDTO;
        }).collect(Collectors.toList()));
        return bookCatalogQueryRespDTO;

    }

    //TODO
    @Override
    public BookPublishRespDTO publishBook(BookPublishReqDTO bookPublishReqDTO) {
        String bookName = bookPublishReqDTO.getBookName();
        Integer channelId = bookPublishReqDTO.getChannelId();
        Integer categoryId = bookPublishReqDTO.getCategoryId();
        List<String> tags = bookPublishReqDTO.getTags();
        Integer status = bookPublishReqDTO.getStatus();
        if(ObjUtil.hasEmpty(channelId,categoryId,status)){
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        if(StrUtil.isBlank(bookName)){
            throw new BusinessException(ResultCode.PARAM_INVALID,"书名不能为空");

        }
        if (BookOpStatusEnum.getEnumByCode(status) == null || BookOpStatusEnum.BANNED.getCode().equals(status)) {

        }
        Long userId = UserContextHolder.getUserId();
        Author author = authorMapper.selectOne(
                new LambdaQueryWrapper<Author>()
                        .eq(Author::getUserId, userId)
        );
        if (author == null) {
            throw new BusinessException(ResultCode.NOT_AN_AUTHOR);
        }
        if (!Objects.equals(author.getStatus(), AuthorStatusTypeEnum.NORMAL.getCode())) {
            throw new BusinessException(ResultCode.AUTHOR_BANNED);
        }
        Long count = this.lambdaQuery()
                .eq(BookInfo::getAuthorId, author.getId())
                .eq(BookInfo::getBookName, bookName)
                .count();
        if (count > 0) {
            throw new BusinessException(ResultCode.ALREADY_HAVE_BOOK);
        }
        Category channel = categoryMapper.selectById(channelId);
        Category category = categoryMapper.selectById(categoryId);
        if (channel == null || category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXISTED);
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
        BookPublishRespDTO bookPublishRespDTO = new BookPublishRespDTO();
        BeanUtil.copyProperties(bookInfo, bookPublishRespDTO);
        bookPublishRespDTO.setTags(tags);
        return bookPublishRespDTO;
    }
}




