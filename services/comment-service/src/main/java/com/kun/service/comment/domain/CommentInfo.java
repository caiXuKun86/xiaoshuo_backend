package com.kun.service.comment.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kun.common.database.entity.BaseEntity;
import lombok.Data;

import java.io.Serial;

/**
 * 评论互动主表
 * @TableName comment_info
 */
@TableName(value ="comment_info")
@Data
public class CommentInfo extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 评论主键 ID (雪花算法)
     */
    @TableId
    private Long id;

    /**
     * 所属图书 ID
     */
    private Long bookId;

    /**
     * 所属章节 ID (针对全书的书评填 0)
     */
    private Long chapterId;

    /**
     * 评论类型 (1:整本书评 2:章节评论 3:段落吐槽/本章说)
     */
    private Integer commentType;

    /**
     * 段落序号 (从 1 开始；书评/章评填 0)
     */
    private Integer paragraphIndex;


    /**
     * 发评人用户 ID
     */
    private Long userId;

    /**
     * 发评人昵称 (冗余快照，避免高频联表查询 user)
     */
    private String userNickname;

    /**
     * 发评人头像 URL (冗余快照，列表秒级渲染)
     */
    private String userAvatar;

    /**
     * 顶级根评论 ID (0:本身即是根评论；>0:楼中楼子评论，二级树核心字段)
     */
    private Long rootId;

    /**
     * 被回复的目标评论 ID (直接回复根评论即等于 root_id)
     */
    private Long parentId;

    /**
     * 被回复人的用户 ID (0 表示直接发表)
     */
    private Long replyToUserId;

    /**
     * 被回复人的昵称 (冗余快照，前台渲染"回复 @某某：")
     */
    private String replyToNickname;

    /**
     * 评论文本内容 (已过敏感词过滤)
     */
    private String content;

    /**
     * 点赞总数 (Redis 异步定时批量回写)
     */
    private Integer likeCount;

    /**
     * 楼中楼回复总数 (仅 root_id = 0 的根评论维护)
     */
    private Integer replyCount;


    /**
     * 状态 (0:待审 1:正常 2:作者/读者自删 3:违规下架)
     */
    private Integer status;


}