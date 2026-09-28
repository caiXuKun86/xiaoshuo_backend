package com.kun.service.book.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kun.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 用户章节解锁记录表
 * @TableName user_chapter_unlock
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="user_chapter_unlock")
@Data
public class UserChapterUnlock extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键 ID (雪花算法)
     */
    @TableId
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 图书 ID (冗余方便统计)
     */
    private Long bookId;

    /**
     * 章节 ID
     */
    private Long chapterId;

    /**
     * 兑换消耗的积分数
     */
    private Integer costPoints;


}