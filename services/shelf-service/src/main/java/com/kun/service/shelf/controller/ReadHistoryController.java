package com.kun.service.shelf.controller;

import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.shelf.dto.req.ReadHistoryPageReqDTO;
import com.kun.service.shelf.dto.resp.ReadHistoryPageRespDTO;
import com.kun.service.shelf.service.ReadHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shelf/history")
@RequiredArgsConstructor
public class ReadHistoryController {

    private final ReadHistoryService readHistoryService;

    @GetMapping
    public Result<PageResult<ReadHistoryPageRespDTO>> pageReadHistory(ReadHistoryPageReqDTO readHistoryPageReqDTO) {
        PageResult<ReadHistoryPageRespDTO> pageResult = readHistoryService.pageReadHistory(readHistoryPageReqDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping("/clear")
    public Result<Void> clearReadHistory(List<Long> bookIds) {
        readHistoryService.clearReadHistory(bookIds);
        return Result.success();

    }

}
