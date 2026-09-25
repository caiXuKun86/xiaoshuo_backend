package com.kun.service.book.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户章节解锁记录表
 * @TableName user_chapter_unlock
 */
@TableName(value ="user_chapter_unlock")
@Data
public class UserChapterUnlock implements Serializable {
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

    /**
     * 兑换时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}