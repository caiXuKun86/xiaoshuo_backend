package com.kun.service.comment.dto.admin.resp;

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
public class AdminCommentInfoDetailPageRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    private Long id;
    /**
     * 作品Id
     */
    private Long bookId;
    /**
     * 章节Id
     */
    private Long chapterId;
    /**
     * 评论类型
     */
    private Integer commentType;
    /**
     * 段落
     */
    private Integer paragraphIndex;
    /**
     * 发评人用户 ID
     */
    private Long userId;

    /**
     * 发评人昵称
     */
    private String userNickname;


    /**
     * 评论文本内容
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
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 回复
     */
    private List<Reply> replies;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Reply {
        private Long id;
        /**
         * 根评论Id
         */
        private Long rootId;
        /**
         * 父评论Id
         */
        private Long parentId;


        /**
         * 发评人用户 ID
         */
        private Long userId;

        /**
         * 发评人昵称
         */
        private String userNickname;


        /**
         * 评论文本内容
         */
        private String content;

        /**
         * 回复用户Id
         */
        private Long replyToUserId;
        /**
         * 回复用户昵称
         */
        private Long replyToUserName;
        /**
         * 状态
         */
        private Integer status;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }


}

