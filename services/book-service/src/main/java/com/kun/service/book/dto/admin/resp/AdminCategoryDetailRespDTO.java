package com.kun.service.book.dto.admin.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 字段为 null 时不参与 JSON 序列化
public class AdminCategoryDetailRespDTO implements Serializable {

    /**
     * 分类主键 ID
     */
    private Long id;

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


    /**
     * 创建时间 (新增时自动填充)
     */
    private LocalDateTime createTime;
    /**
     * 更新时间 (新增与修改时自动填充)
     */
    private LocalDateTime updateTime;

}
