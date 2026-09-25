package com.kun.service.book.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 图书分类表
 * @TableName category
 */
@TableName(value ="category")
@Data
public class Category implements Serializable {
    /**
     * 分类主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 分类名称 (如"东方玄幻")
     */
    private String name;

    /**
     * 父级分类 ID (0 为顶级频道，如男频、女频)
     */
    private Integer parentId;

    /**
     * 展示排序权重 (数值越小越靠前)
     */
    private Integer sort;

    /**
     * 状态 (0:隐藏 1:显示)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}