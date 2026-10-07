package com.kun.service.comment.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentLikeReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * action：1: 点赞；0: 取消点赞。
     */
    private Integer action;


}

