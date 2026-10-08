package com.kun.service.shelf.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.api.client.BookFeignClient;
import com.kun.api.dto.book.BookDTO;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.shelf.domain.ReadHistory;
import com.kun.service.shelf.dto.req.ReadHistoryPageReqDTO;
import com.kun.service.shelf.dto.resp.ReadHistoryPageRespDTO;
import com.kun.service.shelf.service.ReadHistoryService;
import com.kun.service.shelf.mapper.ReadHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lenovo
 * @description 针对表【read_history(用户阅读历史足迹表)】的数据库操作Service实现
 * @createDate 2026-09-30 20:32:27
 */
@Service
@RequiredArgsConstructor
public class ReadHistoryServiceImpl extends ServiceImpl<ReadHistoryMapper, ReadHistory>
        implements ReadHistoryService {

    private final BookFeignClient bookFeignClient;
    private final ReadHistoryMapper readHistoryMapper;

    @Override
    public PageResult<ReadHistoryPageRespDTO> pageReadHistory(ReadHistoryPageReqDTO reqDTO) {
        Long userId = UserContextHolder.getUserId();
        LambdaQueryWrapper<ReadHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReadHistory::getUserId, userId);
        // 5. 排序规则：若未传自定义排序，默认按更新时间倒序
        if (!StringUtils.hasText(reqDTO.getSortField())) {
            queryWrapper.orderByDesc(ReadHistory::getLastReadTime);
        }

        // 6. 执行分页查询
        Page<ReadHistory> page = this.page(reqDTO.toPage(), queryWrapper);
        List<ReadHistory> records = page.getRecords();
        Set<Long> bookIds = records.stream().map(ReadHistory::getBookId).collect(Collectors.toSet());
        if (CollUtil.isEmpty(bookIds)) {
            return PageResult.empty();
        }
        Result<List<BookDTO>> result = bookFeignClient.getBookListById(bookIds);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE, "图书查找失败");
        }
        List<BookDTO> bookDTOList = result.getData();
        Map<Long, BookDTO> bookMap = bookDTOList.stream().collect(Collectors.toMap(BookDTO::getId, b -> b));

        return PageResult.of(page, r -> {
            ReadHistoryPageRespDTO readHistoryPageRespDTO = new ReadHistoryPageRespDTO();
            BookDTO bookDTO = bookMap.get(r.getBookId());
            readHistoryPageRespDTO.setId(r.getId());
            readHistoryPageRespDTO.setBookId(r.getBookId());
            readHistoryPageRespDTO.setBookName(bookDTO.getBookName());
            readHistoryPageRespDTO.setCoverUrl(bookDTO.getCoverUrl());
            readHistoryPageRespDTO.setAuthorName(bookDTO.getAuthorName());
            readHistoryPageRespDTO.setLastReadChapterId(r.getLastReadChapterId());
            readHistoryPageRespDTO.setLastReadChapterIndex(r.getLastReadChapterIndex());
            readHistoryPageRespDTO.setLastReadChapterName(r.getLastReadChapterName());
            readHistoryPageRespDTO.setLastReadTime(r.getLastReadTime());
            return readHistoryPageRespDTO;
        });


    }

    @Override
    public void clearReadHistory(List<Long> bookIds) {
        Long userId = UserContextHolder.getUserId();

        if (bookIds == null || CollUtil.isEmpty(bookIds)) {
            boolean remove = this.remove(new LambdaQueryWrapper<ReadHistory>()
                    .eq(ReadHistory::getUserId, userId)
            );
            if (!remove) {
                throw new BusinessException(ResultCode.OPERATION_FAILED);
            }
        }
        int count = readHistoryMapper.deleteByBookIds(bookIds, userId);
        if (count < 1) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
    }
}




