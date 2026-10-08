package com.kun.service.shelf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.api.client.BookFeignClient;
import com.kun.api.dto.book.BookDTO;
import com.kun.api.dto.shelf.ShelfDTO;
import com.kun.common.core.constant.NovelConstants;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.BookOpStatusEnum;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.service.shelf.domain.Bookshelf;
import com.kun.service.shelf.domain.ReadHistory;
import com.kun.service.shelf.dto.req.BookShelfAddReqDTO;
import com.kun.service.shelf.dto.req.BookShelfMergeReqDTO;
import com.kun.service.shelf.dto.req.BookShelfPageReqDTO;
import com.kun.service.shelf.dto.req.BookShelfSyncReqDTO;
import com.kun.service.shelf.dto.resp.BookShelfAddRespDTO;
import com.kun.service.shelf.dto.resp.BookShelfQueryRespDTO;
import com.kun.service.shelf.dto.resp.BookShelfSyncRespDTO;
import com.kun.service.shelf.dto.resp.ReadingProgressQueryRespDTO;
import com.kun.service.shelf.mapper.BookshelfMapper;
import com.kun.service.shelf.mapper.ReadHistoryMapper;
import com.kun.service.shelf.mq.message.ReadingProgressSyncEvent;
import com.kun.service.shelf.service.BookshelfService;
import com.kun.service.shelf.service.ReadHistoryService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Lenovo
 * @description 针对表【bookshelf(用户书架表)】的数据库操作Service实现
 * @createDate 2026-09-30 20:32:27
 */
@RequiredArgsConstructor
@Service
public class BookshelfServiceImpl extends ServiceImpl<BookshelfMapper, Bookshelf> implements BookshelfService {

    private final BookFeignClient bookFeignClient;
    private final BookshelfMapper bookshelfMapper;
    private final DefaultMQProducer defaultMQProducer;
    private final StringRedisTemplate stringRedisTemplate;
    private final ReadHistoryMapper readHistoryMapper;
    private final ReadHistoryService readHistoryService;

    @Override
    public PageResult<BookShelfQueryRespDTO> pageBookShelfList(BookShelfPageReqDTO bookShelfPageReqDTO) {

        Long userId = UserContextHolder.getUserId();

        LambdaQueryWrapper<Bookshelf> queryWrapper = new LambdaQueryWrapper<Bookshelf>()
                .eq(Bookshelf::getUserId, userId);

        // 若未指定自定义排序字段，默认最后阅读时间倒序排列
        if (!StringUtils.hasText(bookShelfPageReqDTO.getSortField())) {
            queryWrapper.orderByDesc(Bookshelf::getLastReadTime);
        }

        Page<Bookshelf> bookshelfPage = this.page(bookShelfPageReqDTO.toPage(), queryWrapper);
        List<Bookshelf> records = bookshelfPage.getRecords();
        List<Long> bookIds = records.stream().map(Bookshelf::getBookId).toList();
        Result<List<BookDTO>> result = bookFeignClient.getBookListById(bookIds);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE, "图书查找失败");
        }
        List<BookDTO> bookDTOList = result.getData();
        Map<Long, BookDTO> bookDTOMap = bookDTOList.stream().collect(Collectors.toMap(BookDTO::getId, bookDTO -> bookDTO));


        return PageResult.of(bookshelfPage, bookshelf -> {
            BookShelfQueryRespDTO dto = new BookShelfQueryRespDTO();
            BeanUtils.copyProperties(bookshelf, dto);
            BookDTO bookDTO = bookDTOMap.get(bookshelf.getBookId());
            if (bookDTO != null) {
                dto.setAuthorName(bookDTO.getAuthorName());
                dto.setLatestChapterId(bookDTO.getLatestChapterId());
                dto.setLatestChapterName(bookDTO.getLatestChapterName());
                dto.setLatestChapterTime(bookDTO.getLatestChapterTime());
            }

            return dto;
        });

    }

    @Override
    public BookShelfAddRespDTO addBook2Shelf(BookShelfAddReqDTO bookShelfAddReqDTO) {
        Long userId = UserContextHolder.getUserId();
        Long bookId = bookShelfAddReqDTO.getBookId();
        Long lastReadChapterId = bookShelfAddReqDTO.getLastReadChapterId();
        Integer lastReadChapterIndex = bookShelfAddReqDTO.getLastReadChapterIndex();
        String lastReadChapterName = bookShelfAddReqDTO.getLastReadChapterName();
        Integer lastReadParagraph = bookShelfAddReqDTO.getLastReadParagraph();
        BigDecimal readPercent = bookShelfAddReqDTO.getReadPercent();
        if (ObjUtil.hasNull(lastReadChapterId, lastReadChapterIndex, lastReadChapterName, lastReadParagraph, readPercent)) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        if (readPercent.doubleValue() > 100.00) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        Result<BookDTO> result = bookFeignClient.getBookById(bookId);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE, "图书查找失败");
        }
        BookDTO bookDTO = result.getData();
        if (!bookDTO.getStatus().equals(BookOpStatusEnum.ON_SHELF.getCode())) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }

        Long count = this.lambdaQuery()
                .eq(Bookshelf::getBookId, bookId)
                .eq(Bookshelf::getUserId, userId)
                .count();
        if (count > 0) {
            throw new BusinessException(ResultCode.SHELF_ALREADY_EXISTS);
        }
        //MAX_SHELF_CAPACITY
        count = this.lambdaQuery()
                .eq(Bookshelf::getUserId, userId)
                .count();
        if (count >= NovelConstants.MAX_SHELF_CAPACITY) {
            throw new BusinessException(ResultCode.SHELF_CAPACITY_LIMIT);
        }
        Bookshelf bookshelf = BeanUtil.copyProperties(bookShelfAddReqDTO, Bookshelf.class);
        bookshelf.setUserId(userId);
        boolean save = this.save(bookshelf);
        if (!save) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        return new BookShelfAddRespDTO(bookId, count.intValue() + 1);
    }

    @Override
    public void removeBooks2Shelf(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        Long userId = UserContextHolder.getUserId();
        int count = bookshelfMapper.removeByBookIds(ids, userId);
        if (count < 1) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }

    }

    @Override
    public BookShelfSyncRespDTO syncProgress(BookShelfSyncReqDTO reqDTO) {
        Long userId = UserContextHolder.getUserId();
        // 1. 构建阅读进度对象
        ReadingProgressSyncEvent event = ReadingProgressSyncEvent.builder()
                .userId(userId)
                .bookId(reqDTO.getBookId())
                .chapterId(reqDTO.getChapterId())
                .chapterIndex(reqDTO.getChapterIndex())
                .chapterName(reqDTO.getChapterName())
                .paragraphIndex(reqDTO.getParagraphIndex())
                .readPercent(reqDTO.getReadPercent())
                .eventTime(System.currentTimeMillis())
                .build();
        // 2. 依然第一时间写入 Redis Hash (保证换设备/跨端查询能瞬间读到)
        String hashKey = String.format(RedisKeyConstants.SHELF_PROGRESS_PREFIX, userId);
        stringRedisTemplate.opsForHash().put(hashKey, String.valueOf(reqDTO.getBookId()), JSONUtil.toJsonStr(event));
        stringRedisTemplate.expire(hashKey, 14L + RandomUtil.randomLong(1, 5), TimeUnit.DAYS);
        // 3. 异步发送到 MQ (单向发送/可靠发送，仅需 1~2ms)
        // 采用 (userId + ":" + bookId) 作为 hashKey/shardingKey，保证同一个用户的同一本书落到同一个分区，保持局部有序
        Message message = new Message("shelf-topic", "tag-progress-sync", JSONUtil.toJsonStr(event).getBytes());
        try {
            defaultMQProducer.sendOneway(message);
        } catch (Exception e) {
            log.error("Mq消息发送失败{}");
        }
        return new BookShelfSyncRespDTO(LocalDateTime.now());
    }

    @Override
    public ReadingProgressQueryRespDTO getProgressByBookId(Long bookId) {
        Long userId = UserContextHolder.getUserId();

        String hashKey = String.format(RedisKeyConstants.SHELF_PROGRESS_PREFIX, userId);

        String s = stringRedisTemplate.opsForHash().get(hashKey, bookId.toString()).toString();
        if (StrUtil.isNotBlank(s)) {
            ReadingProgressSyncEvent event = JSONUtil.toBean(s, ReadingProgressSyncEvent.class);
            return BeanUtil.copyProperties(event, ReadingProgressQueryRespDTO.class);
        }
        ReadHistory readHistory = readHistoryMapper.selectOne(new LambdaQueryWrapper<ReadHistory>()
                .eq(ReadHistory::getBookId, bookId)
                .eq(ReadHistory::getUserId, userId)
        );
        if (readHistory != null) {
            return BeanUtil.copyProperties(readHistory, ReadingProgressQueryRespDTO.class);

        }
        return null;


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mergeShelf(BookShelfMergeReqDTO bookShelfMergeReqDTO) {
        Long userId = UserContextHolder.getUserId();

        // 1. 合并书架列表 (Bookshelf)
        List<Bookshelf> localShelfList = bookShelfMergeReqDTO.getLocalShelfList();


        if (CollUtil.isNotEmpty(localShelfList)) {
            // 本地记录按 bookId 去重并保留阅读时间最新的一条（写折叠）
            Map<Long, Bookshelf> latestLocalShelfMap = new HashMap<>();
            for (Bookshelf local : localShelfList) {
                if (local == null || local.getBookId() == null) {
                    continue;
                }
                latestLocalShelfMap.merge(local.getBookId(), local, (oldVal, newVal) -> {
                    if (oldVal.getLastReadTime() == null) return newVal;
                    if (newVal.getLastReadTime() == null) return oldVal;
                    return newVal.getLastReadTime().isAfter(oldVal.getLastReadTime()) ? newVal : oldVal;
                });
            }

            List<Long> bookIdList = new ArrayList<>(latestLocalShelfMap.keySet());


            if (CollUtil.isNotEmpty(bookIdList)) {
                Result<List<BookDTO>> result = bookFeignClient.getBookListById(bookIdList);
                if (result == null || result.getCode() != 200 || result.getData() == null) {
                    throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE,"图书查找失败");
                }
                List<BookDTO> bookDTOList = result.getData();
                Set<Long> existBookIds = bookDTOList.stream()
                        .filter(bookDTO -> bookDTO.getStatus().equals(BookOpStatusEnum.ON_SHELF.getCode()))
                        .map(BookDTO::getId)
                        .collect(Collectors.toSet());
                bookIdList = bookIdList.stream().filter(existBookIds::contains).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(bookIdList)) {
                    List<Bookshelf> bookshelfList = this.lambdaQuery()
                            .eq(Bookshelf::getUserId, userId)
                            .in(Bookshelf::getBookId, bookIdList)
                            .list();
                    Map<Long, Bookshelf> bookShelfMap = bookshelfList.stream()
                            .collect(Collectors.toMap(Bookshelf::getBookId, b -> b, (b1, b2) -> b1));

                    List<Bookshelf> updateShelfList = new ArrayList<>();
                    List<Bookshelf> insertShelfList = new ArrayList<>();

                    for (Bookshelf local : latestLocalShelfMap.values()) {
                        Long bookId = local.getBookId();
                        // 过滤未上架/无效图书
                        if (!existBookIds.contains(bookId)) {
                            continue;
                        }
                        local.setUserId(userId);

                        if (bookShelfMap.containsKey(bookId)) {
                            Bookshelf dbShelf = bookShelfMap.get(bookId);
                            // 本地进度更新时才覆盖云端记录
                            if (local.getLastReadTime() != null &&
                                    (dbShelf.getLastReadTime() == null || local.getLastReadTime().isAfter(dbShelf.getLastReadTime()))) {
                                local.setId(dbShelf.getId()); // 关键：绑定云端主键 ID 才能执行 updateBatchById
                                updateShelfList.add(local);
                            }
                        } else {
                            local.setId(null); // 清空本地可能存在的临时 ID，由数据库生成主键
                            insertShelfList.add(local);
                        }
                    }

                    if (CollUtil.isNotEmpty(insertShelfList)) {
                        this.saveBatch(insertShelfList);
                    }
                    if (CollUtil.isNotEmpty(updateShelfList)) {
                        this.updateBatchById(updateShelfList);
                    }
                }

            }
        }

        // 2. 合并阅读足迹列表 (ReadHistory)
        List<ReadHistory> localReadHistoryList = bookShelfMergeReqDTO.getLocalReadHistoryList();
        if (CollUtil.isNotEmpty(localReadHistoryList)) {
            // 本地记录按 bookId 去重并保留阅读时间最新的一条（写折叠）
            Map<Long, ReadHistory> latestLocalHistoryMap = new HashMap<>();
            for (ReadHistory local : localReadHistoryList) {
                if (local == null || local.getBookId() == null) {
                    continue;
                }
                latestLocalHistoryMap.merge(local.getBookId(), local, (oldVal, newVal) -> {
                    if (oldVal.getLastReadTime() == null) return newVal;
                    if (newVal.getLastReadTime() == null) return oldVal;
                    return newVal.getLastReadTime().isAfter(oldVal.getLastReadTime()) ? newVal : oldVal;
                });
            }

            List<Long> historyBookIds = new ArrayList<>(latestLocalHistoryMap.keySet());
            if (CollUtil.isNotEmpty(historyBookIds)) {
                Result<List<BookDTO>> result = bookFeignClient.getBookListById(historyBookIds);
                if (result == null || result.getCode() != 200 || result.getData() == null) {
                    throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE,"图书查找失败");
                }
                List<BookDTO> bookDTOList = result.getData();
                Set<Long> existBookIds = bookDTOList.stream()
                        .filter(bookDTO -> bookDTO.getStatus().equals(BookOpStatusEnum.ON_SHELF.getCode()))
                        .map(BookDTO::getId)
                        .collect(Collectors.toSet());
                historyBookIds = historyBookIds.stream().filter(existBookIds::contains).collect(Collectors.toList());

                if (CollUtil.isNotEmpty(historyBookIds)) {
                    List<ReadHistory> dbHistoryList = readHistoryService.lambdaQuery()
                            .eq(ReadHistory::getUserId, userId)
                            .in(ReadHistory::getBookId, historyBookIds)
                            .list();
                    Map<Long, ReadHistory> dbHistoryMap = dbHistoryList.stream()
                            .collect(Collectors.toMap(ReadHistory::getBookId, h -> h, (h1, h2) -> h1));

                    List<ReadHistory> updateHistoryList = new ArrayList<>();
                    List<ReadHistory> insertHistoryList = new ArrayList<>();

                    for (ReadHistory local : latestLocalHistoryMap.values()) {
                        Long bookId = local.getBookId();
                        // 过滤未上架/无效图书
                        if (!existBookIds.contains(bookId)) {
                            continue;
                        }
                        local.setUserId(userId);

                        if (dbHistoryMap.containsKey(bookId)) {
                            ReadHistory dbHistory = dbHistoryMap.get(bookId);
                            // 本地进度更新时才覆盖云端记录
                            if (local.getLastReadTime() != null &&
                                    (dbHistory.getLastReadTime() == null || local.getLastReadTime().isAfter(dbHistory.getLastReadTime()))) {
                                local.setId(dbHistory.getId()); // 关键：绑定云端主键 ID 才能执行 updateBatchById
                                updateHistoryList.add(local);
                            }
                        } else {
                            local.setId(null); // 清空本地临时 ID，新增入库
                            insertHistoryList.add(local);
                        }
                    }

                    if (CollUtil.isNotEmpty(insertHistoryList)) {
                        readHistoryService.saveBatch(insertHistoryList);
                    }
                    if (CollUtil.isNotEmpty(updateHistoryList)) {
                        readHistoryService.updateBatchById(updateHistoryList);
                    }
                }

            }
        }
    }

    @Override
    public ShelfDTO getShelfByBookId(Long bookId,Long userId) {
        Bookshelf bookshelf = this.lambdaQuery()
                .eq(Bookshelf::getBookId, bookId)
                .eq(Bookshelf::getUserId, userId)
                .one();
        if (bookshelf != null) {
            return ShelfDTO.builder()
                    .isInBookshelf(true)
                    .lastReadChapterId(bookshelf.getLastReadChapterId())
                    .lastReadChapterName(bookshelf.getLastReadChapterName())
                    .build();
        }
        ReadHistory readHistory = readHistoryMapper.selectOne(new LambdaQueryWrapper<ReadHistory>()
                .eq(ReadHistory::getBookId, bookId)
                .eq(ReadHistory::getUserId, userId));
        if (readHistory == null) {
            return ShelfDTO.builder().isInBookshelf(false).build();
        } else {
            return ShelfDTO.builder().isInBookshelf(false)
                    .lastReadChapterName(readHistory.getLastReadChapterName())
                    .lastReadChapterId(readHistory.getLastReadChapterId())
                    .build();
        }

    }
}




