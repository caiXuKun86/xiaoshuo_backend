package com.kun.service.book.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.kun.common.core.aop.RequireRole;
import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.dto.admin.req.AdminCategoryAddRepDTO;
import com.kun.service.book.dto.admin.req.AdminCategoryPageReqDTO;
import com.kun.service.book.dto.admin.req.AdminCategoryUpdateRepDTO;
import com.kun.service.book.dto.admin.resp.AdminCategoryAddRespDTO;
import com.kun.service.book.dto.admin.resp.AdminCategoryDetailRespDTO;
import com.kun.service.book.dto.admin.resp.AdminCategoryPageRespDTO;
import com.kun.service.book.dto.admin.resp.AdminCategoryQueryRespDTO;
import com.kun.service.book.service.admin.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequireRole
@RestController
@RequestMapping("/admin/book/category")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService categoryService;

    @GetMapping("/tree")
    public Result<List<AdminCategoryQueryRespDTO>> queryCategoryTree() {
        List<AdminCategoryQueryRespDTO> categoriesQueryRespDTO = categoryService.queryCategories();
        return Result.success(categoriesQueryRespDTO);
    }

    @GetMapping("/list")
    public Result<PageResult<AdminCategoryPageRespDTO>> queryCategoryPage(AdminCategoryPageReqDTO categoryPageReqDTO) {
        PageResult<AdminCategoryPageRespDTO> pageResult = categoryService.pageCategories(categoryPageReqDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<AdminCategoryDetailRespDTO> queryCategoryDetail(@PathVariable Long id) {
        AdminCategoryDetailRespDTO categoryDetailRespDTO = BeanUtil.copyProperties(categoryService.getById(id), AdminCategoryDetailRespDTO.class);
        return Result.success(categoryDetailRespDTO);
    }

    @PostMapping
    public Result<AdminCategoryAddRespDTO> addCategory(@RequestBody AdminCategoryAddRepDTO categoryAddRepDTO) {
        AdminCategoryAddRespDTO categoryAddRespDTO = categoryService.addCategory(categoryAddRepDTO);
        return Result.success(categoryAddRespDTO);
    }

    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody AdminCategoryUpdateRepDTO categoryUpdateRepDTO){
        categoryService.updateCategory(id,categoryUpdateRepDTO);
        return Result.success();

    }
    @PatchMapping("/{id}/status")
    public Result<Void> patchCategoryStatus(@PathVariable Long id,Integer status){
        categoryService.patchCategoryStatus(id,status);
        return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return Result.success();

    }

}
