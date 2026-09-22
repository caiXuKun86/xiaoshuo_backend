package com.kun.common.database.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据库实体基类
 * 覆盖全站数据表统一的通用字段：id (雪花主键)、create_time (创建时间)、update_time (更新时间)
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID (采用 MyBatis-Plus 内置雪花算法 ASSIGN_ID)
     */
    @Schema(description = "主键ID (雪花算法)")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建时间 (新增时自动填充)
     */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间 (新增与修改时自动填充)
     */
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
