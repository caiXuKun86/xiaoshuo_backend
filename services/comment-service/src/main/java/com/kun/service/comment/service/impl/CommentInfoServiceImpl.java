package com.kun.service.comment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.api.client.BookFeignClient;
import com.kun.api.client.UserFeignClient;
import com.kun.api.dto.book.BookDTO;
import com.kun.api.dto.book.ChapterDTO;
import com.kun.api.dto.user.UserDTO;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.*;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.service.comment.domain.CommentInfo;
import com.kun.service.comment.domain.CommentLike;
import com.kun.service.comment.dto.req.*;
import com.kun.service.comment.dto.resp.*;
import com.kun.service.comment.mapper.CommentInfoMapper;
import com.kun.service.comment.mapper.CommentLikeMapper;
import com.kun.service.comment.mq.event.CommentLikeUpdateEvent;
import com.kun.service.comment.service.CommentInfoService;
import com.kun.service.comment.service.CommentParagraphStatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Lenovo
 * @description 针对表【comment_info(评论互动主表)】的数据库操作Service实现
 * @createDate 2026-10-02 19:42:59
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CommentInfoServiceImpl extends ServiceImpl<CommentInfoMapper, CommentInfo> implements CommentInfoService {

    private final UserFeignClient userFeignClient;
    private final BookFeignClient bookFeignClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final CommentParagraphStatService commentParagraphStatService;
    private final DefaultMQProducer defaultMQProducer;
    private final CommentLikeMapper commentLikeMapper;

    @Override
    public PageResult<CommentPageRespDTO> pageComment(CommentPageReqDTO reqDTO) {

        Long userId = UserContextHolder.getUserId();

        Long bookId = reqDTO.getBookId();
        Long chapterId = reqDTO.getChapterId();
        checkBookAndChapter(bookId, chapterId);

        LambdaQueryWrapper<CommentInfo> queryWrapper = new LambdaQueryWrapper<>();
        Integer commentType = reqDTO.getCommentType();
        if (commentType == null || CommentTypeEnum.BOOK.getCode().equals(commentType) && CommentTypeEnum.CHAPTER.getCode().equals(commentType)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "评论类型无效");
        }
        queryWrapper.eq(CommentInfo::getBookId, reqDTO.getBookId())
                .eq(CommentInfo::getChapterId, reqDTO.getChapterId() != null ? reqDTO.getChapterId() : 0L)
                .eq(CommentInfo::getCommentType, commentType)
                .eq(CommentInfo::getRootId, 0L)   // 核心：只查顶级根评论！
                .eq(CommentInfo::getStatus, 1);   // 正常状态

        // 5. 排序规则：若未传自定义排序，默认按更新时间倒序
        if (!StringUtils.hasText(reqDTO.getSortField())) {
            queryWrapper.orderByDesc(CommentInfo::getCreateTime);
        }

        // 6. 执行分页查询
        Page<CommentInfo> page = this.page(reqDTO.toPage(), queryWrapper);
        List<CommentInfo> records = page.getRecords();
        List<Long> rootIds = records.stream().map(CommentInfo::getId).collect(Collectors.toList());
        if (CollUtil.isEmpty(rootIds)) {
            return PageResult.empty();
        }

        List<CommentLike> commentLikes = commentLikeMapper.selectList(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getUserId, userId)
                .in(CommentLike::getCommentId, rootIds)
                .eq(CommentLike::getStatus, 1));
        Map<Long, Integer> commentLikeMap = commentLikes.stream().collect(Collectors.toMap(CommentLike::getCommentId, CommentLike::getStatus));


        return PageResult.of(page, commentInfo -> {
            CommentPageRespDTO dto = BeanUtil.copyProperties(commentInfo, CommentPageRespDTO.class);
            Integer likeStatus = commentLikeMap.get(commentInfo.getId());
            if (likeStatus == null) {
                dto.setIsLiked(false);
            } else {
                dto.setIsLiked(likeStatus.equals(1));
            }
            return dto;
        });

    }


    @Override
    public CommentPublishRespDTO publishComment(CommentPublishReqDTO commentPublishReqDTO) {
        Long userId = UserContextHolder.getUserId();
        Long bookId = commentPublishReqDTO.getBookId();
        Long chapterId = commentPublishReqDTO.getChapterId();
        Integer paragraphIndex = commentPublishReqDTO.getParagraphIndex();
        Integer commentType = commentPublishReqDTO.getCommentType();
        checkBookAndChapter(bookId, chapterId);

        if (CommentTypeEnum.getByCode(commentType) == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "评论类型无效");
        }
        if (commentType.equals(CommentTypeEnum.BOOK.getCode()) && (commentType != 0 || paragraphIndex != 0)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "评论类型无效");
        }
        if (commentType.equals(CommentTypeEnum.CHAPTER.getCode()) && paragraphIndex != 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "评论类型无效");
        }
        if (commentType.equals(CommentTypeEnum.PARAGRAPH.getCode()) && paragraphIndex == 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "评论类型无效");
        }
        Boolean b = stringRedisTemplate.opsForValue().setIfAbsent(String.format(RedisKeyConstants.COMMENT_COOLDOWN_PREFIX, userId), "1", 10, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(b)) {
            throw new BusinessException(ResultCode.COMMENT_FREQUENCY_LIMIT);
        }

        Result<UserDTO> result = userFeignClient.getUserById(userId);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE, "用户信息查找失败");
        }
        UserDTO userDTO = result.getData();
        CommentInfo commentInfo = new CommentInfo();
        BeanUtil.copyProperties(commentPublishReqDTO, commentInfo);
        commentInfo.setUserId(userId);
        commentInfo.setUserAvatar(userDTO.getAvatar());
        commentInfo.setUserNickname(userDTO.getNickName());
        boolean save = this.save(commentInfo);
        if (!save) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }

        // 若为段评吐槽 (commentType = 3)，原子自增段评统计并同步更新 Redis Hash
        if (Integer.valueOf(3).equals(commentInfo.getCommentType())
                && commentInfo.getParagraphIndex() != null
                && commentInfo.getParagraphIndex() > 0) {
            commentParagraphStatService.incrParagraphCommentCount(
                    commentInfo.getBookId(),
                    commentInfo.getChapterId(),
                    commentInfo.getParagraphIndex()
            );
        }

        return new CommentPublishRespDTO(commentInfo.getId(), commentInfo.getContent(), commentInfo.getCreateTime());
    }

    @Override
    public PageResult<CommentRepliesPageRespDTO> pageCommentReplies(Long rootId, CommentRepliesPageReqDTO reqDTO) {
        Long userId = UserContextHolder.getUserId();
        LambdaQueryWrapper<CommentInfo> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper
                .eq(CommentInfo::getRootId, rootId)
                .eq(CommentInfo::getCommentType, CommentTypeEnum.REPLY.getCode())
                .eq(CommentInfo::getStatus, 1);   // 正常状态


        // 5. 排序规则：若未传自定义排序，默认按更新时间倒序
        if (!StringUtils.hasText(reqDTO.getSortField())) {
            queryWrapper.orderByDesc(CommentInfo::getCreateTime);
        }

        // 6. 执行分页查询
        Page<CommentInfo> page = this.page(reqDTO.toPage(), queryWrapper);
        List<CommentInfo> records = page.getRecords();

        List<Long> repliesIds = records.stream().map(CommentInfo::getId).toList();

        List<CommentLike> commentLikes = commentLikeMapper.selectList(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getUserId, userId)
                .in(CommentLike::getCommentId, repliesIds)
                .eq(CommentLike::getStatus, 1));
        Map<Long, Integer> commentLikeMap = commentLikes.stream().collect(Collectors.toMap(CommentLike::getCommentId, CommentLike::getStatus));

        return PageResult.of(page, commentInfo -> {
            CommentRepliesPageRespDTO dto = new CommentRepliesPageRespDTO();
            BeanUtil.copyProperties(commentInfo, dto);
            Integer likeStatus = commentLikeMap.get(commentInfo.getId());
            if (likeStatus == null) {
                dto.setIsLiked(false);
            } else {
                dto.setIsLiked(likeStatus.equals(1));

            }
            return dto;
        });

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentReplyRespDTO replyComment(CommentReplyReqDTO commentReplyReqDTO) {
        Long userId = UserContextHolder.getUserId();
        Long rootId = commentReplyReqDTO.getRootId();
        Long parentId = commentReplyReqDTO.getParentId();
        String content = commentReplyReqDTO.getContent();
        if (ObjUtil.hasNull(rootId, parentId)) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        if (StrUtil.isBlank(content)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "评论内容不能为空");
        }
        CommentInfo replyTarget = this.lambdaQuery()
                .eq(CommentInfo::getId, parentId)
                .eq(CommentInfo::getStatus, CommentStatusEnum.NORMAL.getCode())
                .one();
        if (replyTarget == null) {
            throw new BusinessException(ResultCode.PARENT_COMMENT_NOT_FOUND);
        }
        CommentInfo rootComment;
        if (replyTarget.getRootId() == 0 && Objects.equals(rootId, parentId)) {
            rootComment = replyTarget;
        } else {
            rootComment = this.lambdaQuery()
                    .eq(CommentInfo::getId, rootId)
                    .eq(CommentInfo::getStatus, CommentStatusEnum.NORMAL.getCode())
                    .one();
        }


        if (rootComment == null || rootComment.getRootId() != 0) {
            throw new BusinessException(ResultCode.PARENT_COMMENT_NOT_FOUND);
        }


        Result<UserDTO> result = userFeignClient.getUserById(userId);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE,"用户信息查找失败");
        }
        UserDTO userDTO = result.getData();

        Boolean b = stringRedisTemplate.opsForValue().setIfAbsent(String.format(RedisKeyConstants.COMMENT_COOLDOWN_PREFIX, userId), "1", 10, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(b)) {
            throw new BusinessException(ResultCode.COMMENT_FREQUENCY_LIMIT);
        }

        CommentInfo commentInfo = new CommentInfo();
        BeanUtil.copyProperties(commentReplyReqDTO, commentInfo);
        commentInfo.setUserId(userId);
        commentInfo.setUserAvatar(userDTO.getAvatar());
        commentInfo.setUserNickname(userDTO.getNickName());
        commentInfo.setBookId(rootComment.getBookId());
        commentInfo.setChapterId(rootComment.getChapterId());
        commentInfo.setCommentType(CommentTypeEnum.REPLY.getCode());
        commentInfo.setParagraphIndex(rootComment.getParagraphIndex());
        commentInfo.setReplyToNickname(replyTarget.getUserNickname());
        commentInfo.setLikeCount(0);

        //保存回复
        boolean save = this.save(commentInfo);
        if (!save) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        //rootId 回复数量+1
        boolean update = this.lambdaUpdate()
                .setSql("reply_count = reply_count +1")
                .eq(CommentInfo::getId, rootId)
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }

        // 若回复的是段落评论吐槽，同样递增该段落气泡统计
        if (Integer.valueOf(3).equals(rootComment.getCommentType())
                && rootComment.getParagraphIndex() != null
                && rootComment.getParagraphIndex() > 0) {
            commentParagraphStatService.incrParagraphCommentCount(
                    rootComment.getBookId(),
                    rootComment.getChapterId(),
                    rootComment.getParagraphIndex()
            );
        }

        return new CommentReplyRespDTO(commentInfo.getId(), commentInfo.getContent(), commentInfo.getCreateTime());

    }

    @Override
    public PageResult<ParagraphCommentPageRespDTO> pageParagraphComment(ParagraphCommentPageReqDTO reqDTO) {
        Long userId = UserContextHolder.getUserId();

        Long chapterId = reqDTO.getChapterId();
        Integer paragraphIndex = reqDTO.getParagraphIndex();


        LambdaQueryWrapper<CommentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(CommentInfo::getChapterId, chapterId)
                .eq(CommentInfo::getParagraphIndex, paragraphIndex)
                .eq(CommentInfo::getRootId, 0)
                .eq(CommentInfo::getStatus, 1);


        // 5. 排序规则：若未传自定义排序，默认按点赞热评倒序
        if (!StringUtils.hasText(reqDTO.getSortField())) {
            queryWrapper.orderByDesc(CommentInfo::getLikeCount);
        }

        // 6. 执行分页查询
        Page<CommentInfo> page = this.page(reqDTO.toPage(), queryWrapper);
        List<CommentInfo> records = page.getRecords();

        List<Long> commentIds = records.stream().map(CommentInfo::getId).toList();

        List<CommentLike> commentLikes = commentLikeMapper.selectList(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getUserId, userId)
                .in(CommentLike::getCommentId, commentIds)
                .eq(CommentLike::getStatus, 1));
        Map<Long, Integer> commentLikeMap = commentLikes.stream().collect(Collectors.toMap(CommentLike::getCommentId, CommentLike::getStatus));

        return PageResult.of(page, commentInfo -> {

            ParagraphCommentPageRespDTO dto = new ParagraphCommentPageRespDTO();
            BeanUtil.copyProperties(commentInfo, dto);
            Integer likeStatus = commentLikeMap.get(commentInfo.getId());
            if (likeStatus == null) {
                dto.setIsLiked(false);
            } else {
                dto.setIsLiked(likeStatus.equals(1));
            }
            return dto;
        });
    }

    @Override
    public CommentLikeRespDTO likeComment(Long commentId, CommentLikeReqDTO commentLikeReqDTO) {
        Long userId = UserContextHolder.getUserId();
        Integer action = commentLikeReqDTO.getAction();
        if (action == null || action != 0 && action != 1) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "参数非法");
        }
        String setKey = String.format(RedisKeyConstants.COMMENT_LIKE_SET_PREFIX, commentId);
        String hashKey = String.format(RedisKeyConstants.COMMENT_LIKES_COUNT_PREFIX);
        Boolean isLiked = stringRedisTemplate.opsForSet().isMember(setKey, userId);
        Object likedCount = stringRedisTemplate.opsForHash().get(hashKey, commentId);

        if (action == 1 && isLiked || action == 0 && !isLiked) {
            return CommentLikeRespDTO.builder()
                    .commentId(commentId)
                    .isLiked(isLiked)
                    .currentLikeCount(Integer.valueOf(likedCount.toString()))
                    .build();

        }
        CommentLikeRespDTO commentLikeRespDTO;
        if (action == 1) {
            stringRedisTemplate.opsForSet().add(setKey, userId.toString());
            stringRedisTemplate.opsForHash().increment(hashKey, commentId.toString(), 1);
            commentLikeRespDTO = CommentLikeRespDTO.builder()
                    .commentId(commentId)
                    .isLiked(true)
                    .currentLikeCount(Integer.valueOf(likedCount.toString()) + 1)
                    .build();

        } else {
            stringRedisTemplate.opsForSet().remove(setKey, userId.toString());
            stringRedisTemplate.opsForHash().increment(hashKey, commentId.toString(), -1);
            commentLikeRespDTO = CommentLikeRespDTO.builder()
                    .commentId(commentId)
                    .isLiked(false)
                    .currentLikeCount(Integer.valueOf(likedCount.toString()) - 1)
                    .build();
        }

        CommentLikeUpdateEvent event = new CommentLikeUpdateEvent(null,userId, commentId, action, System.currentTimeMillis());
        Message message=new Message("comment-topic","tag-commentLike-update", JSONUtil.toJsonStr(event).getBytes());
        try {
            defaultMQProducer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {

                }

                @Override
                public void onException(Throwable e) {
                    log.error("mq消息发送失败{}",e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("mq消息发送失败{}",e.getMessage());
        }
        return commentLikeRespDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        CommentInfo commentInfo = this.getById(commentId);
        if (commentInfo == null || commentInfo.getStatus().equals(CommentStatusEnum.NORMAL.getCode())) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        Long userId = UserContextHolder.getUserId();
        if (!commentInfo.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOAUTH_OP_COMMENT);
        }
        boolean update = this.lambdaUpdate()
                .eq(CommentInfo::getId, commentId)
                .set(CommentInfo::getStatus, CommentStatusEnum.USER_DELETED.getCode())
                .update();
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        Long rootId = commentInfo.getRootId();
        if (rootId == 0) {
            boolean update1 = this.lambdaUpdate()
                    .eq(CommentInfo::getRootId, rootId)
                    .set(CommentInfo::getStatus, CommentStatusEnum.USER_DELETED.getCode())
                    .update();
            if (!update1) {
                throw new BusinessException(ResultCode.OPERATION_FAILED);
            }
        }

    }

    private void checkBookAndChapter(Long bookId, Long chapterId) {
        if (ObjUtil.hasNull(bookId, chapterId)) {
            throw new BusinessException(ResultCode.PARAM_INVALID);
        }
        Result<BookDTO> bookResult = bookFeignClient.getBookById(bookId);
        if (bookResult == null || bookResult.getCode() != 200 || bookResult.getData() == null) {
            throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE,"图书查找失败");
        }
        BookDTO bookDTO = bookResult.getData();
        if (!bookDTO.getStatus().equals(BookOpStatusEnum.ON_SHELF.getCode())) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        if (chapterId != 0) {
            Result<ChapterDTO> chapterResult = bookFeignClient.getChapterById(chapterId);
            if (chapterResult == null || chapterResult.getCode() != 200 || chapterResult.getData() == null) {
                throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE,"章节查找失败");
            }
            ChapterDTO chapterDTO = chapterResult.getData();
            if (!chapterDTO.getStatus().equals(ChapterStatusEnum.PUBLISHED.getCode())) {
                throw new BusinessException(ResultCode.CHAPTER_NOT_FOUND);
            }
        }
    }
}




