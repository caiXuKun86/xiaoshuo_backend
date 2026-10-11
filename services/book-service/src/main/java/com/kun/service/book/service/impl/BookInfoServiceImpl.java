package com.kun.service.book.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.api.client.ShelfFeignClient;
import com.kun.api.dto.shelf.ShelfDTO;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.BookOpStatusEnum;
import com.kun.common.core.enums.BookRankTypeEnum;
import com.kun.common.core.enums.CategoryStatusEnum;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.common.redis.util.CacheUtil;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.domain.Category;
import com.kun.service.book.dto.req.BookInfoPageReqDTO;
import com.kun.service.book.dto.req.BookRankPageReqDTO;
import com.kun.service.book.dto.req.BookSearchPageReqDTO;
import com.kun.service.book.dto.resp.BookInfoDetailRespDTO;
import com.kun.service.book.dto.resp.BookInfoPageRespDTO;
import com.kun.service.book.dto.resp.BookRankHomeSummaryRespDTO;
import com.kun.service.book.dto.resp.BookRankPageRespDTO;
import com.kun.service.book.mapper.BookInfoMapper;
import com.kun.service.book.mapper.CategoryMapper;
import com.kun.service.book.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Lenovo
 * @description 针对表【book_info(图书信息主表)】的数据库操作Service实现
 * @createDate 2026-09-25 10:39:19
 */

@RequiredArgsConstructor
@Service
public class BookInfoServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements BookInfoService {
    private final StringRedisTemplate stringRedisTemplate;


    private final ShelfFeignClient shelfFeignClient;
    private final CacheUtil cacheUtil;
    private final CategoryMapper categoryMapper;


    @Override
    public PageResult<BookInfoPageRespDTO> queryBookInfoPage(BookInfoPageReqDTO reqDTO) {
        boolean enableCache = reqDTO.getPageNum() <= 5;
        String cacheKey = reqDTO.generateCacheKey();
        String hashKey =String.format(RedisKeyConstants.CACHE_BOOK_PAGE,cacheKey);

        if (enableCache) {

            return cacheUtil.queryWithMutex(
                    hashKey,
                    new TypeReference<PageResult<BookInfoPageRespDTO>>() {},
                    () -> queryBookInfoPage2Db(reqDTO),
                    5L,
                    TimeUnit.MINUTES
            );
        }
        return queryBookInfoPage2Db(reqDTO);
    }

    private PageResult<BookInfoPageRespDTO> queryBookInfoPage2Db(BookInfoPageReqDTO reqDTO) {
        Integer channelId = reqDTO.getChannelId();
        Long categoryId = reqDTO.getCategoryId();
        Integer bookStatus = reqDTO.getBookStatus();
        LambdaQueryWrapper<BookInfo> queryWrapper = new LambdaQueryWrapper<>();
        // 1. 业务硬约束：只展示已上架的书籍 (status = 1)
        queryWrapper.eq(BookInfo::getStatus, 1);

        // 2. 分类/频道筛选
        if (categoryId != null && categoryId > 0) {
            // 精确匹配二级分类
            queryWrapper.eq(BookInfo::getCategoryId, categoryId);
        } else if (channelId != null && channelId > 0) {
            // 只选了一级频道 (例如男频/女频)，使用子查询匹配该频道下的所有子分类
            List<Long> childrenCategoryIds = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                            .select(Category::getId)
                            .eq(Category::getParentId, channelId)
                    .eq(Category::getStatus, CategoryStatusEnum.SHOW)
            ).stream().map(Category::getId).toList();
            // 只选了一级频道 (例如男频/女频)，使用子查询匹配该频道下的所有子分类
            queryWrapper.in(CollUtil.isNotEmpty(childrenCategoryIds),BookInfo::getCategoryId, childrenCategoryIds);        }

        // 3. 连载状态筛选
        if (bookStatus != null) {
            queryWrapper.eq(BookInfo::getBookStatus, bookStatus);
        }
        // 5. 排序规则：若未传自定义排序，默认按更新时间倒序
        if (!StringUtils.hasText(reqDTO.getSortField())) {
            queryWrapper.orderByDesc(BookInfo::getLatestChapterTime);
        }

        // 6. 执行分页查询
        Page<BookInfo> page = this.page(reqDTO.toPage(), queryWrapper);

        return PageResult.of(page, bookInfo -> {
            BookInfoPageRespDTO dto = new BookInfoPageRespDTO();
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
    public BookInfoDetailRespDTO queryBookInfoDetail(Long id) {
        BookInfoDetailRespDTO bookDetailQueryRespDTO = new BookInfoDetailRespDTO();
        String redisKey = String.format(RedisKeyConstants.BOOK_INFO_PREFIX, id);
        BookInfo bookInfo = cacheUtil.queryWithMutex(redisKey, BookInfo.class, () -> this.getById(id), 2L, TimeUnit.HOURS);
        if (bookInfo == null || !bookInfo.getStatus().equals(BookOpStatusEnum.ON_SHELF.getCode())) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        BeanUtil.copyProperties(bookInfo, bookDetailQueryRespDTO);
        bookDetailQueryRespDTO.setTags(List.of(bookInfo.getTags().split(",")));
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            bookDetailQueryRespDTO.setUserInteract(new BookInfoDetailRespDTO.UserInteract(false, null, null));
            return bookDetailQueryRespDTO;
        }
        Result<ShelfDTO> result = shelfFeignClient.getShelfDTO(id,userId);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            bookDetailQueryRespDTO.setUserInteract(new BookInfoDetailRespDTO.UserInteract(false, null, null));
            return bookDetailQueryRespDTO;
        }
        ShelfDTO shelfDTO = result.getData();
        BookInfoDetailRespDTO.UserInteract userInteract = new BookInfoDetailRespDTO.UserInteract();
        userInteract.setIsInBookshelf(shelfDTO.getIsInBookshelf());
        userInteract.setLastReadChapterId(shelfDTO.getLastReadChapterId());
        userInteract.setLastReadChapterName(shelfDTO.getLastReadChapterName());
        bookDetailQueryRespDTO.setUserInteract(userInteract);
        return bookDetailQueryRespDTO;
    }






    @Override
    public PageResult<BookInfoPageRespDTO> searchBookPage(BookSearchPageReqDTO reqDTO) {
        String keyword = reqDTO.getKeyword();
        if (keyword.isBlank()) {
            throw new BusinessException(ResultCode.SEARCH_KEYWORD_BLANK);
        }
        keyword = keyword.trim();
        LambdaQueryWrapper<BookInfo> queryWrapper = new LambdaQueryWrapper<>();
        // 1. 业务硬约束：只展示已上架的书籍 (status = 1)
        queryWrapper.eq(BookInfo::getStatus, 1);
        String finalKeyword = keyword;
        queryWrapper.and(wq -> wq.like(BookInfo::getBookName, finalKeyword)
                .or().like(BookInfo::getAuthorName, finalKeyword)
                .or().like(BookInfo::getTags, finalKeyword));
        // 5. 排序规则：若未传自定义排序，默认按更新时间倒序
        if (!StringUtils.hasText(reqDTO.getSortField())) {
            queryWrapper.orderByDesc(BookInfo::getLatestChapterTime);
        }

        // 6. 执行分页查询
        Page<BookInfo> page = this.page(reqDTO.toPage(), queryWrapper);

        return PageResult.of(page, bookInfo -> {
            BookInfoPageRespDTO dto = new BookInfoPageRespDTO();
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
    public PageResult<BookRankPageRespDTO> rankBookPage(BookRankPageReqDTO bookSearchPageReqDTO) {

        Integer rankType = bookSearchPageReqDTO.getRankType();
        Integer pageNum = bookSearchPageReqDTO.getPageNum();
        Integer pageSize = bookSearchPageReqDTO.getPageSize();
        if (BookRankTypeEnum.getByCode(rankType) == null) {
            throw new BusinessException(ResultCode.RANK_TYPE_NOT_FOUND);
        }
        String key;
        if (BookRankTypeEnum.NEW_BOOK.getCode().equals(rankType)) {
            key = RedisKeyConstants.RANK_NEW_BOOKS;
        } else {
            key = RedisKeyConstants.RANK_COMPLETED;
        }
        // 0-19 20-39
        long start = (long) (pageNum - 1) * pageSize;
        long stop = start + pageSize - 1;
        Long total = stringRedisTemplate.opsForZSet().zCard(key);
        total = Math.min(total, 100L);
        Set<String> bookIds = stringRedisTemplate.opsForZSet().reverseRange(key, start, stop);
        List<BookInfo> bookInfoList = this.listByIds(bookIds);

        AtomicInteger currentRank = new AtomicInteger((pageNum - 1) * pageSize + 1);
        List<BookRankPageRespDTO> dtoList = bookInfoList.stream()
                .map(bookInfo -> {
                    BookRankPageRespDTO dto = new BookRankPageRespDTO();
                    BeanUtils.copyProperties(bookInfo, dto);
                    dto.setRank(currentRank.getAndIncrement());
                    return dto;
                })
                .toList();
        return PageResult.of(dtoList, pageNum, pageSize, total);
    }

    @Override
    public BookRankHomeSummaryRespDTO queryRankHomeSummary() {
        String rankCompletedKey = RedisKeyConstants.RANK_COMPLETED;

        Set<String> bookIds = stringRedisTemplate.opsForZSet().reverseRange(rankCompletedKey, 0, 4);
        List<BookInfo> bookInfoList = this.listByIds(bookIds);

        AtomicInteger currentRank = new AtomicInteger(1);
        AtomicInteger finalCurrentRank1 = currentRank;
        List<BookRankPageRespDTO> CompletedBookList = bookInfoList.stream()
                .map(bookInfo -> {
                    BookRankPageRespDTO dto = new BookRankPageRespDTO();
                    BeanUtils.copyProperties(bookInfo, dto);
                    dto.setRank(finalCurrentRank1.getAndIncrement());
                    return dto;
                })
                .toList();

        String newBookKey = RedisKeyConstants.RANK_NEW_BOOKS;

        bookIds = stringRedisTemplate.opsForZSet().reverseRange(newBookKey, 0, 4);
        bookInfoList = this.listByIds(bookIds);
        currentRank = new AtomicInteger(1);
        AtomicInteger finalCurrentRank = currentRank;
        List<BookRankPageRespDTO> newBookList = bookInfoList.stream()
                .map(bookInfo -> {
                    BookRankPageRespDTO dto = new BookRankPageRespDTO();
                    BeanUtils.copyProperties(bookInfo, dto);
                    dto.setRank(finalCurrentRank.getAndIncrement());
                    return dto;
                })
                .toList();
        return new BookRankHomeSummaryRespDTO(newBookList, CompletedBookList);
    }
}




