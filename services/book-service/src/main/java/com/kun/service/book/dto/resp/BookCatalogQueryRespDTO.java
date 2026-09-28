package com.kun.service.book.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookCatalogQueryRespDTO implements Serializable {

    /**
     * book ID
     */
    private Long bookId;

    /**
     * 总章数
     */
    private Integer totalChapters;



    /**
     * 书籍信息
     */
    private List<ChapterInfoDTO> chapters;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChapterInfoDTO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        /**
         * 章节主键 ID (雪花算法)
         */
        private Long id;


        /**
         * 章节顺序号 (第几章: 1, 2, 3...)
         */
        private Integer chapterIndex;

        /**
         * 章节标题 (如"第1章 陨落的天才")
         */
        private String chapterName;

        /**
         * 本章字数 (如 3200)
         */
        private Integer wordCount;

        /**
         * 是否付费章节 (0:免费 1:收费)
         */
        private Integer isCharge;

        /**
         * 兑换本章所需积分
         */
        private Integer requiredPoints;
        /**
         * 是否解锁
         */
        private Boolean isUnlocked;

        /**
         * 发布生效时间 (支持定时发布)
         */
        private LocalDateTime publishTime;



    }
}

