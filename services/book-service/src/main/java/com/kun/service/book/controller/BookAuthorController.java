package com.kun.service.book.controller;

import com.kun.common.core.result.Result;
import com.kun.service.book.dto.req.AuthorRegisterReqDTO;
import com.kun.service.book.dto.resp.AuthorDetailQueryRespDTO;
import com.kun.service.book.dto.resp.AuthorRegisterRespDTO;
import com.kun.service.book.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book/author")
@RequiredArgsConstructor
public class BookAuthorController {

    private final AuthorService authorService;


    @GetMapping("/{authorId}")
    public Result<AuthorDetailQueryRespDTO> queryBookDetail(@PathVariable("authorId") Long authorId) {
        AuthorDetailQueryRespDTO authorDetailQueryRespDTO = authorService.queryBookDetailById(authorId);

        return Result.success(authorDetailQueryRespDTO);
    }

    @PostMapping("/register")
    public Result<AuthorRegisterRespDTO> registerWriter(@RequestBody AuthorRegisterReqDTO authorRegisterReqDTO) {
        AuthorRegisterRespDTO authorRegisterRespDTO = authorService.registerAuthor(authorRegisterReqDTO);
        return Result.success(authorRegisterRespDTO);

    }
}
