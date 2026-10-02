package com.kun.service.shelf.dto.req;

import com.kun.service.shelf.domain.Bookshelf;
import com.kun.service.shelf.domain.ReadHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookShelfMergeReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    private List<Bookshelf> localShelfList;
    private List<ReadHistory> localReadHistoryList;


}

