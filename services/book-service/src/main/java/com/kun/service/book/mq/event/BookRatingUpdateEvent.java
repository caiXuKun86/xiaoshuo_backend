package com.kun.service.book.mq.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookRatingUpdateEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 小说图书 ID
     */
    private Long bookId;

    /**
     * 分数
     */
    private Integer updateScore;
    /**
     * 时间
     */
    private LocalDateTime time;


}

