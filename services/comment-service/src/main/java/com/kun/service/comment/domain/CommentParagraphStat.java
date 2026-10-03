package com.kun.service.comment.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kun.common.database.entity.BaseEntity;
import lombok.Data;

import java.io.Serial;

/**
 * 段落评论聚合统计表
 * @TableName comment_paragraph_stat
 */
@TableName(value ="comment_paragraph_stat")
@Data
public class CommentParagraphStat extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 统计主键 ID (雪花算法)
     */
    @TableId
    private Long id;

    /**
     * 所属图书 ID (冗余，方便统计与级联清理)
     */
    private Long bookId;

    /**
     * 所属章节 ID
     */
    private Long chapterId;

    /**
     * 段落序号 (从 1 开始)
     */
    private Integer paragraphIndex;

    /**
     * 段落特征指纹快照 (校验段落一致性)
     */
    private String paragraphHash;

    /**
     * 该段落累计评论总数 (气泡展示数)
     */
    private Integer commentCount;


}