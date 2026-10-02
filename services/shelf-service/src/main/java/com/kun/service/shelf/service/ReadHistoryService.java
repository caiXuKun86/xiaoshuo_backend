package com.kun.service.shelf.service;

import com.kun.common.database.page.PageResult;
import com.kun.service.shelf.domain.ReadHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.service.shelf.dto.req.ReadHistoryPageReqDTO;
import com.kun.service.shelf.dto.resp.ReadHistoryPageRespDTO;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【read_history(用户阅读历史足迹表)】的数据库操作Service
* @createDate 2026-09-30 20:32:27
*/
public interface ReadHistoryService extends IService<ReadHistory> {

    PageResult<ReadHistoryPageRespDTO> pageReadHistory(ReadHistoryPageReqDTO readHistoryPageReqDTO);

    void clearReadHistory(List<Long> bookIds);
}
