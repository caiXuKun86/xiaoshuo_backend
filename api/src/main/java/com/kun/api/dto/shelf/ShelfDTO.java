package com.kun.api.dto.shelf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShelfDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 读到的最后章节 ID (与图书最新章节比对红点)
     */
    private Long lastReadChapterId;


    /**
     * 最后阅读章节名称 (冗余快照，避免频繁联表)
     */
    private String lastReadChapterName;


    /**
     * 是否在书架中
     */
    private Boolean isInBookshelf;

}
