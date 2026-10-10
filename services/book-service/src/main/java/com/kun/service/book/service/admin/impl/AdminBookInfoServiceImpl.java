package com.kun.service.book.service.admin.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.core.enums.BookOpStatusEnum;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.dto.admin.req.AdminBookInfoPageReqDTO;
import com.kun.service.book.dto.admin.resp.AdminBookInfoDetailRespDTO;
import com.kun.service.book.dto.admin.resp.AdminBookInfoPageRespDTO;
import com.kun.service.book.mapper.BookInfoMapper;
import com.kun.service.book.service.admin.AdminBookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author Lenovo
 * @description 针对表【book_info(图书信息主表)】的数据库操作Service实现
 * @createDate 2026-09-25 10:39:19
 */

@RequiredArgsConstructor
@Service
public class AdminBookInfoServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements AdminBookInfoService {


    @Override
    public PageResult<AdminBookInfoPageRespDTO> queryBookInfoPage(AdminBookInfoPageReqDTO reqDTO) {

        Long channelId = reqDTO.getChannelId();
        Long categoryId = reqDTO.getCategoryId();
        String authorName = reqDTO.getAuthorName();
        Integer bookStatus = reqDTO.getBookStatus();
        Integer status = reqDTO.getStatus();
        String bookName = reqDTO.getBookName();
        Integer minWordCount = reqDTO.getMinWordCount();
        Integer maxWordCount = reqDTO.getMaxWordCount();

        LambdaQueryWrapper<BookInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(status != null, BookInfo::getStatus, status)
                .eq(bookStatus != null, BookInfo::getBookStatus, bookStatus)
                .like(StrUtil.isNotBlank(authorName), BookInfo::getAuthorName, authorName)
                .like(StrUtil.isNotBlank(bookName), BookInfo::getBookName, bookName)
                .ge(minWordCount != null, BookInfo::getWordCount, minWordCount)
                .le(maxWordCount != null, BookInfo::getWordCount, maxWordCount);

        if (categoryId != null && categoryId > 0) {
            // 精确匹配二级分类
            queryWrapper.eq(BookInfo::getCategoryId, categoryId);
        } else if (channelId != null && channelId > 0) {
            // 只选了一级频道 (例如男频/女频)，使用子查询匹配该频道下的所有子分类
            queryWrapper.inSql(BookInfo::getCategoryId, "SELECT id FROM category WHERE parent_id = " + channelId);
        }


        // 5. 排序规则：若未传自定义排序，默认按更新时间倒序
        if (!StringUtils.hasText(reqDTO.getSortField())) {
            queryWrapper.orderByDesc(BookInfo::getLatestChapterTime);
        }

        // 6. 执行分页查询
        Page<BookInfo> page = this.page(reqDTO.toPage(), queryWrapper);

        return PageResult.of(page, bookInfo -> {
            AdminBookInfoPageRespDTO dto = new AdminBookInfoPageRespDTO();
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
    public AdminBookInfoDetailRespDTO queryBookInfoDetail(Long bookId) {
        AdminBookInfoDetailRespDTO bookInfoDetailRespDTO = new AdminBookInfoDetailRespDTO();
        BookInfo bookInfo = this.getById(bookId);
        if (bookInfo == null || !bookInfo.getStatus().equals(BookOpStatusEnum.ON_SHELF.getCode())) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        BeanUtil.copyProperties(bookInfo, bookInfoDetailRespDTO);
        bookInfoDetailRespDTO.setTags(List.of(bookInfo.getTags().split(",")));
        return bookInfoDetailRespDTO;

    }

    @Override
    public void patchBookInfoStatus(Long bookId, Integer status) {
        BookInfo bookInfo = this.getById(bookId);
        if (bookInfo == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        if (status == null || BookOpStatusEnum.getByCode(status) == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "状态不存在");
        }
        boolean update = this.lambdaUpdate()
                .eq(BookInfo::getId, bookId)
                .set(BookInfo::getStatus, status)
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);

        }
    }

    @Override
    public void deleteBookInfo(Long bookId) {
        BookInfo bookInfo = this.getById(bookId);
        if (bookInfo == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        if (bookInfo.getLatestChapterId() != null) {
            throw new BusinessException(ResultCode.BOOK_CANNOT_DELETE);
        }
        boolean delete = this.removeById(bookId);
        if (!delete) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }

    }
}




