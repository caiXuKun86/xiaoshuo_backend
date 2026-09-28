package com.kun.service.book.controller;

import com.kun.common.core.result.Result;
import com.kun.service.book.dto.resp.CategoryQueryRespDTO;
import com.kun.service.book.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/book/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryQueryRespDTO>> queryCategories() {
        List<CategoryQueryRespDTO> categoriesQueryRespDTO = categoryService.queryCategories();
        return Result.success(categoriesQueryRespDTO);
    }

}
