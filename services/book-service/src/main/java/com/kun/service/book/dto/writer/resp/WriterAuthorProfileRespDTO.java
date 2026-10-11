package com.kun.service.book.dto.writer.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WriterAuthorProfileRespDTO implements Serializable {

    /**
     * 作家主键 ID (雪花算法)
     */
    private Long authorId;
    /**
     * 用户ID
     */
    private Long userId;

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
     * 状态
     */
    private Integer status;

    /**
     * 作品数量
     */
    private Integer bookCount;
    /**
     * 总字数
     */
    private Long totalWordCount;
    private LocalDateTime createTime;


}

