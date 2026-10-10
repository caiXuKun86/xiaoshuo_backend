package com.kun.service.book.dto.admin.req;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 字段为 null 时不参与 JSON 序列化
public class AdminCategoryAddRepDTO implements Serializable {


    /**
     * 父级分类ID (0 为顶级频道；若大于0必须为合法的一级分类)
     */
    private Long parentId;
    /**
     * 分类名称 (如"东方玄幻")
     */
    private String name;

    /**
     * 展示排序权重 (数值越小越靠前)
     */
    private Integer sort;

    /**
     * 分类状态
     */
    private Integer status;


}
