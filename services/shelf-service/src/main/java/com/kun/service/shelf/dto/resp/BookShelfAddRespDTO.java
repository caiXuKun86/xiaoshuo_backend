package com.kun.service.shelf.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookShelfAddRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 小说图书 ID
     */
    private Long bookId;

    /**
     * 总数量
     */
    private Integer totalShelfCount;


}

