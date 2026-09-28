package com.kun.service.book.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorDetailQueryRespDTO implements Serializable {

    /**
     * 作家主键 ID (雪花算法)
     */
    private Long authorId;

    /**
     * 笔名 (如"天蚕土豆")
     */
    private String penName;

    /**
     * 作家头像 OSS 地址
     */
    private String avatar;

    /**
     * 作家简介/档案
     */
    private String intro;

    /**
     * 作品总数
     */
    private Integer totalBookCount;


    /**
     * 总字数
     */
    private Integer totalWordCount;

    /**
     * 书籍信息
     */
    private List<BookInfoDTO> books;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BookInfoDTO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        /**
         * 图书主键 ID (雪花算法)
         */
        private Long bookId;

        /**
         * 小说书名
         */
        private String bookName;

        /**
         * 分类名称 (冗余展示)
         */
        private String categoryName;

        /**
         * 封面图 OSS 地址
         */
        private String coverUrl;

        /**
         * 全书总字数
         */
        private Integer wordCount;

        /**
         * 连载状态 (0:连载中 1:完结)
         */
        private Integer bookStatus;


    }
}

