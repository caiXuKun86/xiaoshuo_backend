package com.kun.service.book.dto.writer.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WriterBookInfoUpdateReqDTO implements Serializable {


    /**
     * 封面图 OSS 地址
     */
    private String coverUrl;

    /**
     * 作品简介
     */
    private String description;

    /**
     * 标签 (逗号隔开，如"穿越,系统")
     */
    private List<String> tags;


}

