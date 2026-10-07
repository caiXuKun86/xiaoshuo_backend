package com.kun.service.book.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorRegisterReqDTO implements Serializable {


    /**
     * 笔名 (如"天蚕土豆")
     */
    private String penName;



    /**
     * 作家简介/档案
     */
    private String intro;


}

