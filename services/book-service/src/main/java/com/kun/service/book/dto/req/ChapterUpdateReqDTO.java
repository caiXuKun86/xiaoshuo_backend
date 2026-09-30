package com.kun.service.book.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChapterUpdateReqDTO implements Serializable {

    /**
     * 所属图书 ID
     */
    private Long bookId;


    /**
     * 章节标题 (如"第1章 陨落的天才")
     */
    private String chapterName;
    /**
     * 内容
     */
    private String content;


}

