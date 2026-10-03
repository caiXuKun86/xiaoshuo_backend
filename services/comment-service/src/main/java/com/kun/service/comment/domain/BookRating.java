package com.kun.service.comment.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kun.common.database.entity.BaseEntity;
import lombok.Data;

import java.io.Serial;

/**
 * 书籍评分表
 * @TableName book_rating
 */
@TableName(value ="book_rating")
@Data
public class BookRating extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 评分主键 ID (雪花算法)
     */
    @TableId
    private Long id;

    /**
     * 评分用户 ID
     */
    private Long userId;

    /**
     * 小说图书 ID
     */
    private Long bookId;

    /**
     * 评分数值 (1-10，对应半星 0.5~5.0 分)
     */
    private Integer score;


}