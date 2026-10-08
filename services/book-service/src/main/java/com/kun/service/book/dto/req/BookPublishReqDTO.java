package com.kun.service.book.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookPublishReqDTO implements Serializable {


    /**
     * 小说书名
     */
    private String bookName;

    /**
     * 分类 ID
     */
    private Integer channelId;
    /**
     * 分类 ID
     */
    private Integer categoryId;


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


    /**
     * 运营状态 (1:上架 2:下架封禁)
     */
    private Integer status;


}

