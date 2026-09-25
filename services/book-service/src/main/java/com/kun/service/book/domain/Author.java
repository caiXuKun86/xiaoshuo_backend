package com.kun.service.book.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 作家/笔名表
 * @TableName author
 */
@TableName(value ="author")
@Data
public class Author implements Serializable {
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

    /**
     * 签约/入驻时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}