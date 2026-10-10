package com.kun.service.book.dto.admin.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminChapterPageRespDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键Id
     */
    private Long id;

    /**
     * 图书Id
     */
    private Long bookId;
    /**
     * 序号
     */
    private Integer chapterIndex;


    /**
     * 章节总字数
     */
    private Integer wordCount;

    /**
     * 是否收费
     */
    private Integer isCharge;

    /**
     * 消耗积分
     */
    private Integer requiredPoints;

    /**
     * 章节状态
     */
    private Integer status;
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}

