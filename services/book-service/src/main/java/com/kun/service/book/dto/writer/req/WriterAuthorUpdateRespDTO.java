package com.kun.service.book.dto.writer.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WriterAuthorUpdateRespDTO implements Serializable {

    /**
     * 作家头像 OSS 地址
     */
    private String avatar;

    /**
     * 作家简介/档案
     */
    private String intro;

}

