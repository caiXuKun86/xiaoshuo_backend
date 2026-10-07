package com.kun.service.comment.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentReplyReqDTO implements Serializable {


    /**
     * 顶级根评论 ID (0:本身即是根评论；>0:楼中楼子评论，二级树核心字段)
     */
    private Long rootId;

    /**
     * 被回复的目标评论 ID (直接回复根评论即等于 root_id)
     */
    private Long parentId;

    /**
     * 内容
     */
    private String content;


}

