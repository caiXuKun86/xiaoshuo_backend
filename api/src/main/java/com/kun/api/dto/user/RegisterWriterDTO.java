package com.kun.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户跨服务传输基础信息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "跨服务用户基础信息传输对象")
public class RegisterWriterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer isWriter;
}
