package com.kun.service.book.service.admin.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.core.enums.ChapterChargeEnum;
import com.kun.common.core.enums.ChapterStatusEnum;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.database.page.PageResult;
import com.kun.common.oss.template.OssTemplate;
import com.kun.common.redis.util.CacheUtil;
import com.kun.service.book.domain.BookChapter;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.dto.admin.req.AdminChapterBatchSetChargeReqDTO;
import com.kun.service.book.dto.admin.req.AdminChapterPageReqDTO;
import com.kun.service.book.dto.admin.resp.AdminChapterBatchSetChargeRespDTO;
import com.kun.service.book.dto.admin.resp.AdminChapterDetailRespDTO;
import com.kun.service.book.dto.admin.resp.AdminChapterPageRespDTO;
import com.kun.service.book.mapper.BookChapterMapper;
import com.kun.service.book.mapper.BookInfoMapper;
import com.kun.service.book.service.admin.AdminBookChapterService;
import com.kun.service.book.service.admin.stragety.ChapterStatusHandler;
import com.kun.service.book.service.admin.stragety.ChapterStatusHandlerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * @author Lenovo
 * @description 针对表【book_chapter(章节目录表)】的数据库操作Service实现
 * @createDate 2026-09-25 10:39:19
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminBookChapterServiceImpl extends ServiceImpl<BookChapterMapper, BookChapter> implements AdminBookChapterService {

    private final BookInfoMapper bookInfoMapper;
    private final OssTemplate ossTemplate;
    private final CacheUtil cacheUtil;
    private final ChapterStatusHandlerFactory chapterStatusHandlerFactory;
    @Override
    public PageResult<AdminChapterPageRespDTO> queryChapterPage(AdminChapterPageReqDTO reqDTO) {
        Integer status = reqDTO.getStatus();
        Integer isCharge = reqDTO.getIsCharge();
        Long bookId = reqDTO.getBookId();
        String chapterName = reqDTO.getChapterName();
        Long count = bookInfoMapper.selectCount(new LambdaQueryWrapper<BookInfo>().eq(BookInfo::getId, bookId));
        if (count < 1) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }

        LambdaQueryWrapper<BookChapter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(status != null, BookChapter::getStatus, status)
                .eq(isCharge != null, BookChapter::getIsCharge, isCharge)
                .eq(bookId != null, BookChapter::getBookId, bookId)
                .likeRight(StrUtil.isNotBlank(chapterName), BookChapter::getChapterName, chapterName);

        // 5. 排序规则：若未传自定义排序，默认按发布时间倒序
        if (!StringUtils.hasText(reqDTO.getSortField())) {
            queryWrapper.orderByDesc(BookChapter::getPublishTime);
        }

        // 6. 执行分页查询
        Page<BookChapter> page = this.page(reqDTO.toPage(), queryWrapper);

        return PageResult.of(page, bookChapter -> BeanUtil.copyProperties(bookChapter, AdminChapterPageRespDTO.class));
    }

    @Override
    public AdminChapterDetailRespDTO queryChapterDetail(Long chapterId) {

        BookChapter bookChapter = this.getById(chapterId);
        if (bookChapter == null) {
            throw new BusinessException(ResultCode.CHAPTER_NOT_FOUND);
        }
        AdminChapterDetailRespDTO chapterDetailRespDTO = new AdminChapterDetailRespDTO();
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
    @Transactional(rollbackFor = Exception.class)
    public void patchChapterStatus(Long chapterId, Integer status) {
        ChapterStatusEnum statusEnum = ChapterStatusEnum.getByCode(status);
        if(statusEnum ==null){
            throw new BusinessException(ResultCode.PARAM_INVALID,"状态不存在");
        }

        BookChapter bookChapter = this.getById(chapterId);
        if (bookChapter == null) {
            throw new BusinessException(ResultCode.CHAPTER_NOT_FOUND);
        }
        ChapterStatusHandler chapterStatusHandler = chapterStatusHandlerFactory.getHandler(status);
        chapterStatusHandler.handle(bookChapter,null);

    }


    @Override
    public AdminChapterBatchSetChargeRespDTO setChapterChargeBatch(AdminChapterBatchSetChargeReqDTO chapterBatchSetChargeReqDTO) {

        Long bookId = chapterBatchSetChargeReqDTO.getBookId();
        Integer startChapterIndex = chapterBatchSetChargeReqDTO.getStartChapterIndex();
        Integer endChapterIndex = chapterBatchSetChargeReqDTO.getEndChapterIndex();
        Integer requiredPoints = chapterBatchSetChargeReqDTO.getRequiredPoints();

        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (bookInfo == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        if (ObjUtil.hasNull(bookInfo, startChapterIndex)) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }

        int count = baseMapper.update(this.lambdaUpdate()
                .set(BookChapter::getIsCharge, ChapterChargeEnum.CHARGE.getCode())
                .set(BookChapter::getRequiredPoints, requiredPoints)
                .ge(BookChapter::getChapterIndex, startChapterIndex)
                .le(endChapterIndex != null, BookChapter::getChapterIndex, endChapterIndex));

        if (count < 1) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        return new AdminChapterBatchSetChargeRespDTO(count);

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
        boolean delete = this.removeById(chapterId);
        if (!delete) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "删除失败");
        }
    }


}




