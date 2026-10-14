package com.kun.service.book.controller.writer;

import com.kun.common.core.result.Result;
import com.kun.service.book.dto.writer.req.WriterAuthorUpdateRespDTO;
import com.kun.service.book.dto.writer.resp.WriterAuthorProfileRespDTO;
import com.kun.service.book.service.writer.WriterAuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book/writer")
@RequiredArgsConstructor
public class WriterAuthorController {

    private final WriterAuthorService authorService;


    @GetMapping("/profile")
    public Result<WriterAuthorProfileRespDTO> queryWriterProfile() {
        WriterAuthorProfileRespDTO authorProfileRespDTO = authorService.queryWriterProfile();

        return Result.success(authorProfileRespDTO);
    }

    @PutMapping("/profile")
    public Result<Void> updateWriterProfile(@RequestBody WriterAuthorUpdateRespDTO authorUpdateRespDTO) {
        authorService.updateWriterProfile(authorUpdateRespDTO);
        return Result.success();

    }

}
