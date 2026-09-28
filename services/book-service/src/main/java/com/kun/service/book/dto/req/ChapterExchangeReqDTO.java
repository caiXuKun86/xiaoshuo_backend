package com.kun.service.book.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChapterExchangeReqDTO implements Serializable {


    /**
     * 图书Id
     */
    private Long bookId;

    /**
     * 章节 ID
     */
    private Long chapterId;


}


