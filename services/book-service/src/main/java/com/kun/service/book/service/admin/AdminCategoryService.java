package com.kun.service.book.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.common.database.page.PageResult;
import com.kun.service.book.domain.Category;
import com.kun.service.book.dto.admin.req.AdminCategoryAddRepDTO;
import com.kun.service.book.dto.admin.req.AdminCategoryPageReqDTO;
import com.kun.service.book.dto.admin.req.AdminCategoryUpdateRepDTO;
import com.kun.service.book.dto.admin.resp.AdminCategoryAddRespDTO;
import com.kun.service.book.dto.admin.resp.AdminCategoryPageRespDTO;
import com.kun.service.book.dto.admin.resp.AdminCategoryQueryRespDTO;

import java.util.List;

/**
 * @author Lenovo
 * @description 针对表【category(图书分类表)】的数据库操作Service
 * @createDate 2026-09-25 10:39:19
 */
public interface AdminCategoryService extends IService<Category> {

    List<AdminCategoryQueryRespDTO> queryCategories();

    PageResult<AdminCategoryPageRespDTO> pageCategories(AdminCategoryPageReqDTO categoryPageReqDTO);

    AdminCategoryAddRespDTO addCategory(AdminCategoryAddRepDTO categoryAddRepDTO);

    void updateCategory(Long id,AdminCategoryUpdateRepDTO categoryUpdateRepDTO);

    void patchCategoryStatus(Long id, Integer status);

    void deleteCategory(Long id);
}
