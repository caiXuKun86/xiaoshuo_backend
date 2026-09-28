package com.kun.service.book.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.BookOpStatusEnum;
import com.kun.common.core.enums.ChapterChargeEnum;
import com.kun.common.core.enums.ChapterStatusEnum;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.domain.BookChapter;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.domain.UserChapterUnlock;
import com.kun.service.book.dto.req.BookPageReqDTO;
import com.kun.service.book.dto.resp.BookCatalogQueryRespDTO;
import com.kun.service.book.dto.resp.BookDetailQueryRespDTO;
import com.kun.service.book.dto.resp.BookPageRespDTO;
import com.kun.service.book.mapper.BookChapterMapper;
import com.kun.service.book.mapper.BookInfoMapper;
import com.kun.service.book.mapper.UserChapterUnlockMapper;
import com.kun.service.book.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
        BookInfo bookInfo = this.getById(id);
        if (bookInfo == null || !(Objects.equals(bookInfo.getStatus(), BookOpStatusEnum.ON_SHELF.getCode()))) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        BeanUtil.copyProperties(bookInfo, bookDetailQueryRespDTO);
        bookDetailQueryRespDTO.setTags(List.of(bookInfo.getTags().split(",")));
        //TODO
        bookDetailQueryRespDTO.setRatingUserCount(0);
        bookDetailQueryRespDTO.setUserInteract(null);
        return bookDetailQueryRespDTO;
    }

    @Override
    public BookCatalogQueryRespDTO queryBookCatalogById(Long bookId, String sortOrder) {
        Long userId = UserContextHolder.getUserId();
        BookInfo bookInfo = this.getById(bookId);
        if (bookInfo == null || !(Objects.equals(bookInfo.getStatus(), BookOpStatusEnum.ON_SHELF.getCode()))) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        List<BookChapter> bookChapters = bookChapterMapper.selectList(new LambdaQueryWrapper<BookChapter>()
                .eq(BookChapter::getBookId, bookInfo.getId())
                .eq(BookChapter::getStatus, ChapterStatusEnum.PUBLISHED)
        );
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
}




