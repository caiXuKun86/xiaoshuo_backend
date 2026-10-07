package com.kun.service.comment.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookRatingDetailRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 小说图书 ID
     */
    private Long bookId;


    /**
     * 最新评分
     */
    private BigDecimal score;
    /**
     * 评分数量
     */
    private Integer ratingCount;

    private BigDecimal fiveStarPercent;
    private BigDecimal fourStarPercent;
    private BigDecimal threeStarPercent;
    private BigDecimal twoStarPercent;
    private BigDecimal oneStarPercent;

}

