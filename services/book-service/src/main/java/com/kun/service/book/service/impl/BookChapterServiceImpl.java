package com.kun.service.book.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.api.client.UserFeignClient;
import com.kun.api.dto.user.UserDTO;
import com.kun.api.dto.user.UserPointsUpdateDTO;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.AssetChangeTypeEnum;
import com.kun.common.core.enums.ChapterChargeEnum;
import com.kun.common.core.enums.ChapterStatusEnum;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.core.result.Result;
import com.kun.common.oss.template.OssTemplate;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.common.redis.util.CacheUtil;
import com.kun.service.book.domain.Author;
import com.kun.service.book.domain.BookChapter;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.domain.UserChapterUnlock;
import com.kun.service.book.dto.req.ChapterExchangeReqDTO;
import com.kun.service.book.dto.req.ChapterPublishReqDTO;
import com.kun.service.book.dto.req.ChapterUpdateReqDTO;
import com.kun.service.book.dto.resp.BookChapterQueryRespDTO;
import com.kun.service.book.dto.resp.ChapterExchangeRespDTO;
import com.kun.service.book.dto.resp.ChapterPublishRespDTO;
import com.kun.service.book.dto.resp.ChapterUpdateRespDTO;
import com.kun.service.book.mapper.AuthorMapper;
import com.kun.service.book.mapper.BookChapterMapper;
import com.kun.service.book.mapper.BookInfoMapper;
import com.kun.service.book.mapper.UserChapterUnlockMapper;
import com.kun.service.book.service.BookChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Lenovo
 * @description 针对表【book_chapter(章节目录表)】的数据库操作Service实现
 * @createDate 2026-09-25 10:39:19
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class BookChapterServiceImpl extends ServiceImpl<BookChapterMapper, BookChapter> implements BookChapterService {

    private final OssTemplate ossTemplate;
    private final BookInfoMapper bookInfoMapper;
    private final UserChapterUnlockMapper userChapterUnlockMapper;
    private final RedissonClient redissonClient;
    private final UserFeignClient userFeignClient;
    private final TransactionTemplate transactionTemplate;
    private final AuthorMapper authorMapper;
    private final CacheUtil cacheUtil;

    @Override
    public BookChapterQueryRespDTO queryBookChapter(Long bookId, Long chapterId) {
        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (bookInfo == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        BookChapter bookChapter = this.getById(chapterId);
        if (bookChapter == null) {
            throw new BusinessException(ResultCode.CHAPTER_NOT_FOUND);
        }
        BookChapterQueryRespDTO bookChapterQueryRespDTO = new BookChapterQueryRespDTO();
        BeanUtil.copyProperties(bookChapter, bookChapterQueryRespDTO);
        bookChapterQueryRespDTO.setRequiredPoints(null);
        BookChapter preChapter = this.lambdaQuery()
                .eq(BookChapter::getBookId, bookChapter.getBookId())
                .eq(BookChapter::getChapterIndex, bookChapter.getChapterIndex() - 1)
                .select(BookChapter::getId)
                .one();

        BookChapter nextChapter = this.lambdaQuery()
                .eq(BookChapter::getBookId, bookChapter.getBookId())
                .eq(BookChapter::getChapterIndex, bookChapter.getChapterIndex() + 1)
                .select(BookChapter::getId)
                .one();

        if (preChapter != null) {
            bookChapterQueryRespDTO.setPrevChapterId(preChapter.getId());
        }
        if (nextChapter != null) {
            bookChapterQueryRespDTO.setPrevChapterId(nextChapter.getId());
        }

        if (Objects.equals(bookChapter.getIsCharge(), ChapterChargeEnum.CHARGE.getCode())) {
            Long userId = UserContextHolder.getUserId();
            if (userId == null) {
                bookChapterQueryRespDTO.setIsLocked(true);
                bookChapterQueryRespDTO.setNeedLogin(true);
                return bookChapterQueryRespDTO;
            }

            if (!isUnlockChapter(userId, chapterId)) {
                bookChapterQueryRespDTO.setIsLocked(true);
                bookChapterQueryRespDTO.setNeedLogin(false);
                bookChapterQueryRespDTO.setRequiredPoints(bookChapter.getRequiredPoints());
                return bookChapterQueryRespDTO;
            } else {
                bookChapterQueryRespDTO.setIsLocked(true);
            }
        }
        String content = null;
        try {
            content = ossTemplate.readChapterContent(bookChapter.getOssPath());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.OSS_CONTENT_FETCH_FAIL);
        }
        if (StrUtil.isBlank(content)) {
            throw new BusinessException(ResultCode.OSS_CONTENT_FETCH_FAIL);
        }
        bookChapterQueryRespDTO.setContent(content);
        return bookChapterQueryRespDTO;


    }

    @Override
    public ChapterExchangeRespDTO exchangeChapter(ChapterExchangeReqDTO chapterExchangeReqDTO) {
        Long bookId = chapterExchangeReqDTO.getBookId();
        Long chapterId = chapterExchangeReqDTO.getChapterId();
        Long userId = UserContextHolder.getUserId();

        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (bookInfo == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        BookChapter bookChapter = this.getById(chapterId);
        if (bookChapter == null) {
            throw new BusinessException(ResultCode.CHAPTER_NOT_FOUND);
        }
        RLock lock = redissonClient.getLock(String.format(RedisKeyConstants.LOCK_EXCHANGE_CHAPTER, userId, chapterId));
        ChapterExchangeRespDTO chapterExchangeRespDTO = new ChapterExchangeRespDTO();
        try {
            boolean b = lock.tryLock();
            if (!b) {
                throw new BusinessException(ResultCode.REQUEST_RATE_LIMIT);
            }
            if (isUnlockChapter(userId, chapterId)) {
                throw new BusinessException(ResultCode.CHAPTER_ALREADY_UNLOCKED);
            }
            Result<UserDTO> result = userFeignClient.getUserById(userId);
            if (result.getCode() != 200) {
                throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE, "用户服务调用失败");
            }
            UserDTO userDTO = result.getData();
            if (userDTO.getPointBalance() < bookChapter.getRequiredPoints()) {
                throw new BusinessException(ResultCode.PAY_INSUFFICIENT_POINTS);
            }
            doExchangeChapter(userId, userDTO, bookChapter, bookInfo);
            chapterExchangeRespDTO.setBookId(bookId);
            chapterExchangeRespDTO.setChapterId(chapterId);
            chapterExchangeRespDTO.setChapterName(bookChapter.getChapterName());
            chapterExchangeRespDTO.setChapterIndex(bookChapter.getChapterIndex());
            chapterExchangeRespDTO.setCostPoints(bookChapter.getRequiredPoints());
            chapterExchangeRespDTO.setRemainingPoints(userDTO.getPointBalance() - bookChapter.getRequiredPoints());
            chapterExchangeRespDTO.setUnlockedTime(LocalDateTime.now());
            return chapterExchangeRespDTO;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChapterPublishRespDTO publishChapter(ChapterPublishReqDTO chapterPublishReqDTO) {
        Long bookId = chapterPublishReqDTO.getBookId();
        Integer chapterIndex = chapterPublishReqDTO.getChapterIndex();
        String chapterName = chapterPublishReqDTO.getChapterName();
        String content = chapterPublishReqDTO.getContent();
        Integer isCharge = chapterPublishReqDTO.getIsCharge();
        Integer requiredPoints = chapterPublishReqDTO.getRequiredPoints();

        if (ObjUtil.hasNull(chapterIndex, chapterName, isCharge)) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        if (StrUtil.isBlank(content)) {
            throw new BusinessException(ResultCode.CONTENT_IS_BLANK);
        }
        //收费必须传入requiredPoints
        if (isCharge == 1 && requiredPoints == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "付费章节必须指定消费积分");
        }

        Long userId = UserContextHolder.getUserId();
        Author author = authorMapper.selectOne(new LambdaQueryWrapper<Author>().eq(Author::getUserId, userId));
        if (author == null) {
            throw new BusinessException(ResultCode.NOT_AN_AUTHOR);
        }
        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (bookInfo == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        if (!Objects.equals(bookInfo.getAuthorId(), author.getId())) {
            throw new BusinessException(ResultCode.AUTHOR_NOT_PERMITTED);
        }
        String ossPath = ossTemplate.uploadChapterContent(String.format(OssTemplate.UPLOAD_CHAPTER, bookId, chapterIndex), content);

        BookChapter bookChapter = new BookChapter();
        BeanUtil.copyProperties(chapterPublishReqDTO, bookChapter);
        bookChapter.setOssPath(ossPath);
        int wordCount = computeContentLength(content);
        bookChapter.setWordCount(wordCount);
        bookChapter.setParagraphCount(countParagraphs(content));
        bookChapter.setStatus(ChapterStatusEnum.PUBLISHED.getCode());
        boolean save = this.save(bookChapter);
        if (!save) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        int update = bookInfoMapper.update(
                new LambdaUpdateWrapper<BookInfo>()
                        .eq(BookInfo::getId, bookId)
                        .setSql("word_count=word_count+" + wordCount)
                        .set(BookInfo::getLatestChapterId, bookChapter.getId())
                        .set(BookInfo::getLatestChapterName, bookChapter.getChapterName())
                        .set(BookInfo::getLatestChapterTime, LocalDateTime.now())
        );
        if (update < 1) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        cacheUtil.delete(String.format(RedisKeyConstants.CACHE_BOOK_CATALOG, bookId));

        return BeanUtil.copyProperties(bookChapter, ChapterPublishRespDTO.class);


    }

    @Transactional
    @Override
    public ChapterUpdateRespDTO updateChapter(ChapterUpdateReqDTO chapterUpdateReqDTO, Long chapterId) {
        Long bookId = chapterUpdateReqDTO.getBookId();
        String chapterName = chapterUpdateReqDTO.getChapterName();
        String content = chapterUpdateReqDTO.getContent();

        Long userId = UserContextHolder.getUserId();
        Author author = authorMapper.selectOne(new LambdaQueryWrapper<Author>().eq(Author::getUserId, userId));
        if (author == null) {
            throw new BusinessException(ResultCode.NOT_AN_AUTHOR);
        }
        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (bookInfo == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        if (!Objects.equals(bookInfo.getAuthorId(), author.getId())) {
            throw new BusinessException(ResultCode.AUTHOR_NOT_PERMITTED);
        }
        BookChapter bookChapter = this.getById(chapterId);

        if (bookChapter == null) {
            throw new BusinessException(ResultCode.CHAPTER_NOT_FOUND);
        }

        int newCount = computeContentLength(content);
        int newParagraphCount = countParagraphs(content);

        boolean update = this.lambdaUpdate()
                .eq(BookChapter::getId, chapterId)
                .set(StrUtil.isNotBlank(chapterName), BookChapter::getChapterName, chapterName)
                .set(StrUtil.isNotBlank(content), BookChapter::getWordCount, newCount)
                .set(StrUtil.isNotBlank(content), BookChapter::getParagraphCount, newParagraphCount)
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }

        if (StrUtil.isNotBlank(content)) {
            int delta = newCount - bookChapter.getWordCount();
            int update1 = bookInfoMapper.update(new LambdaUpdateWrapper<BookInfo>()
                    .eq(BookInfo::getId, bookId)
                    .setSql("word_count = word_count + " + delta)
            );
            if (update1 < 1) {
                throw new BusinessException(ResultCode.OPERATION_FAILED);

            }
            ossTemplate.uploadChapterContent(String.format(OssTemplate.UPLOAD_CHAPTER, bookId, bookChapter.getChapterIndex()), content);
        }
        if (StrUtil.isNotBlank(chapterName) && Objects.equals(bookInfo.getLatestChapterId(), chapterId)) {
            int update1 = bookInfoMapper.update(
                    new LambdaUpdateWrapper<BookInfo>()
                            .eq(BookInfo::getId, bookId)
                            .set(BookInfo::getLatestChapterName, chapterName)
            );
            if (update1 < 1) {
                throw new BusinessException(ResultCode.OPERATION_FAILED);

            }

        }
        cacheUtil.delete(String.format(RedisKeyConstants.CACHE_BOOK_CATALOG, bookId));
        return BeanUtil.copyProperties(bookChapter, ChapterUpdateRespDTO.class);

    }

    private void doExchangeChapter(long userId, UserDTO userDTO, BookChapter bookChapter, BookInfo bookInfo) {
        // 1. 组装远程扣积分参数
        UserPointsUpdateDTO updateDTO = new UserPointsUpdateDTO();
        updateDTO.setUserId(userId);
        updateDTO.setBalanceChange(-bookChapter.getRequiredPoints()); // 注意：扣减积分通常是负数
        updateDTO.setBalanceBefore(userDTO.getPointBalance());
        updateDTO.setBalanceAfter(userDTO.getPointBalance() - bookChapter.getRequiredPoints());
        updateDTO.setBizId(bookChapter.getId());
        // 假设你的枚举字段是 changeType 而不是重复覆盖 balanceChange
        updateDTO.setChangeType(AssetChangeTypeEnum.CHAPTER_EXCHANGE.getCode());
        updateDTO.setTitle(String.format("兑换<< %s >>第 %d 章: %s",
                bookInfo.getBookName(), bookChapter.getChapterIndex(), bookChapter.getChapterName()));

        // 2. 步骤一：远程调用扣减积分（不在本地事务内）
        Result<Boolean> booleanResult = userFeignClient.updatePoints(updateDTO);
        if (booleanResult == null || booleanResult.getCode() != 200 || !Boolean.TRUE.equals(booleanResult.getData())) {
            throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE, "用户积分扣减失败");
        }

        // 3. 步骤二：本地事务执行（只包裹本地写库，快速释放连接）
        try {
            transactionTemplate.executeWithoutResult(status -> {
                UserChapterUnlock userChapterUnlock = new UserChapterUnlock();
                userChapterUnlock.setUserId(userId); // 别忘了存 userId
                userChapterUnlock.setChapterId(bookChapter.getId());
                userChapterUnlock.setBookId(bookInfo.getId());
                userChapterUnlock.setCostPoints(bookChapter.getRequiredPoints());

                // 注意：这里必须插入 userChapterUnlock 对象，而不是 new UserChapterUnlock()
                int insert = userChapterUnlockMapper.insert(userChapterUnlock);
                if (insert < 1) {
                    // 抛出异常会触发 transactionTemplate 自动回滚
                    throw new BusinessException(ResultCode.SYSTEM_ERROR, "新增解锁记录失败");
                }
            });
        } catch (Exception e) {
            log.error("本地插入解锁记录失败，开始执行积分回滚补偿，userId: {}, chapterId: {}", userId, bookChapter.getId(), e);
            // 4. 关键补偿：本地落库失败，必须补偿把积分退还给用户！
            compensateRefundPoints(userId, bookChapter, bookInfo);

            // 向上抛出异常让外层处理
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "解锁章节失败，已自动退还积分");
        }
    }

    /**
     * 失败回滚补偿逻辑
     */
    private void compensateRefundPoints(long userId, BookChapter bookChapter, BookInfo bookInfo) {
        UserPointsUpdateDTO refundDTO = new UserPointsUpdateDTO();
        refundDTO.setUserId(userId);
        refundDTO.setBalanceChange(bookChapter.getRequiredPoints()); // 加回积分
        refundDTO.setBizId(bookChapter.getId());
        refundDTO.setChangeType(AssetChangeTypeEnum.REFUND.getCode());
        refundDTO.setTitle(String.format("退还<< %s >>第 %d 章兑换积分", bookInfo.getBookName(), bookChapter.getChapterIndex()));

        boolean isSuccess = false;
        String failReason = "";

        try {
            // 1. 显式接收远程调用结果
            Result<Boolean> result = userFeignClient.updatePoints(refundDTO);

            // 2. 严谨校验：判空、状态码、返回的业务布尔值
            if (result == null) {
                failReason = "远程返回结果为 null";
            } else if (result.getCode() != 200) {
                failReason = "下游业务处理失败: " + result.getMessage() + " (code: " + result.getCode() + ")";
            } else if (!Boolean.TRUE.equals(result.getData())) {
                failReason = "积分增减操作返回 false";
            } else {
                // 真正成功
                isSuccess = true;
                log.info("积分退还补偿成功, userId: {}, chapterId: {}", userId, bookChapter.getId());
            }
        } catch (Exception ex) {
            // 捕获网络超时、连接拒绝等真正的底层异常
            failReason = "Feign 调用底层抛出异常: " + ex.getMessage();
            log.error("调用用户服务退款异常", ex);
        }

        // 3. 统一处理失败逻辑（无论是网络不通，还是下游业务返回失败）
        if (!isSuccess) {
            log.error("【严重警告】积分退还补偿失败，需人工介入！userId: {}, chapterId: {}, 原因: {}",
                    userId, bookChapter.getId(), failReason);

        }
    }

    private boolean isUnlockChapter(long userId, long chapterId) {
        Long count = userChapterUnlockMapper.selectCount(new LambdaQueryWrapper<UserChapterUnlock>()
                .eq(UserChapterUnlock::getUserId, userId)
                .eq(UserChapterUnlock::getChapterId, chapterId)
        );
        return count > 0;
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
}




