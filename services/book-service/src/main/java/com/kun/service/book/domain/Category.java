package com.kun.service.book.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kun.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 图书分类表
 * @TableName category
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="category")
@Data
public class Category extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称 (如"东方玄幻")
     */
    private String name;

    /**
     * 父级分类 ID (0 为顶级频道，如男频、女频)
     */
    private Long parentId;

    /**
     * 展示排序权重 (数值越小越靠前)
     */
    private Integer sort;

    /**
     * 状态 (0:隐藏 1:显示)
     */
    private Integer status;


}