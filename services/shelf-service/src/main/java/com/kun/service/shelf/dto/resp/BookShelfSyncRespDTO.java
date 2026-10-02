package com.kun.service.shelf.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookShelfSyncRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    private LocalDateTime syncTime;


}

