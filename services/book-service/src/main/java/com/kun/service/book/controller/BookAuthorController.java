package com.kun.service.book.controller;

import com.kun.common.core.result.Result;
import com.kun.service.book.dto.resp.AuthorDetailQueryRespDTO;
import com.kun.service.book.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/book/author")
@RequiredArgsConstructor
public class BookAuthorController {

    private final AuthorService authorService;


    @GetMapping("/{authorId}")
    public Result<AuthorDetailQueryRespDTO> queryBookDetail(@PathVariable Long authorId) {
        AuthorDetailQueryRespDTO authorDetailQueryRespDTO = authorService.queryBookDetailById(authorId);

        return Result.success(authorDetailQueryRespDTO);
    }
}
