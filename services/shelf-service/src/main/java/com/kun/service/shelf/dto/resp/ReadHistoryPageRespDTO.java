package com.kun.service.shelf.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadHistoryPageRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 主键 ID (自增或雪花算法)
     */
    private Long id;

    /**
     * 小说图书 ID
     */
    private Long bookId;

    /**
     * 书名
     */
    private String bookName;

    /**
     * 封面
     */
    private String coverUrl;
    /**
     * 作者笔名
     */
    private String authorName;

    /**
     * 读到的最后章节 ID
     */
    private Long lastReadChapterId;

    /**
     * 读到的最后章节序号
     */
    private Integer lastReadChapterIndex;

    /**
     * 读到的章节标题 (冗余快照)
     */
    private String lastReadChapterName;


    /**
     * 最后一次阅读时间
     */
    private LocalDateTime lastReadTime;


}

