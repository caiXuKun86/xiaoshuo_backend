package com.kun.service.book.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kun.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 作家/笔名表
 * @TableName author
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="author")
@Data
public class Author extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 作家主键 ID (雪花算法)
     */
    @TableId
    private Long id;

    /**
     * 关联的用户 ID (读者转作者时绑定，唯一)
     */
    private Long userId;

    /**
     * 笔名 (如"天蚕土豆")
     */
    private String penName;

    /**
     * 作家头像 OSS 地址
     */
    private String avatar;

    /**
     * 作家简介/档案
     */
    private String intro;

    /**
     * 认证状态 (0:封禁 1:正常)
     */
    private Integer status;




}