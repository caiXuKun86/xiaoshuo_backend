package com.kun.service.book.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDetailQueryRespDTO implements Serializable {

    /**
     * 分类主键 ID
     */
    private Long id;

    /**
     * 书籍名称
     */
    private String bookName;

    /**
     * 作者Id
     */
    private Integer authorId;
    /**
     * 作者笔名
     */
    private String authorName;

    /**
     * 分类Id
     */
    private Long categoryId;
    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 作品封面
     */
    private String coverUrl;

    /**
     * 作品描述
     */
    private String description;

    /**
     * 作品标签
     */
    private List<String> tags;

    /**
     * 作品字数
     */
    private Integer wordCount;

    /**
     * 连载状态 (0:连载中 1:完结)
     */
    private Integer bookStatus;


    /**
     * 综合评分 (如 9.6 分)
     */
    private BigDecimal score;

    /**
     * 打分用户数量
     */
    private Integer ratingUserCount;

    /**
     * 收藏数量
     */
    private Integer collectCount;

    /**
     * 第一章Id
     */
    private Integer firstChapterId;
    /**
     * 最新章节Id
     */
    private Integer latestChapterId;
    /**
     * 最新章节名
     */
    private String latestChapterName;

    /**
     * 最新章节发布时间
     */
    private Date latestChapterTime;

    /**
     * 用户交互信息
     */
    private UserInteract userInteract;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInteract implements Serializable {
        /**
         * 是否已加入书架
         */
        private Boolean isInBookshelf;
        /**
         * 用户个人评分 (1-10 分，未评分为 null)
         */
        private Integer userScore;
        /**
         * 读到的最后章节 ID (雪花算法使用 Long)
         */
        private Long lastReadChapterId;
        /**
         * 读到的最后章节名称
         */
        private String lastReadChapterName;
    }
}

