package com.kun.service.book.dto.admin.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 字段为 null 时不参与 JSON 序列化
public class AdminCategoryAddRespDTO implements Serializable {


    private Long id;


}
