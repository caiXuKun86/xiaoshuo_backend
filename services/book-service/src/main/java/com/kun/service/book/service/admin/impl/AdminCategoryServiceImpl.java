package com.kun.service.book.service.admin.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.core.enums.CategoryStatusEnum;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.database.page.PageResult;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.domain.Category;
import com.kun.service.book.dto.admin.req.AdminCategoryAddRepDTO;
import com.kun.service.book.dto.admin.req.AdminCategoryPageReqDTO;
import com.kun.service.book.dto.admin.req.AdminCategoryUpdateRepDTO;
import com.kun.service.book.dto.admin.resp.AdminCategoryAddRespDTO;
import com.kun.service.book.dto.admin.resp.AdminCategoryPageRespDTO;
import com.kun.service.book.dto.admin.resp.AdminCategoryQueryRespDTO;
import com.kun.service.book.mapper.BookInfoMapper;
import com.kun.service.book.mapper.CategoryMapper;
import com.kun.service.book.service.admin.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lenovo
 * @description 针对表【category(图书分类表)】的数据库操作Service实现
 * @createDate 2026-09-25 10:39:19
 */
@Service
@RequiredArgsConstructor
public class AdminCategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements AdminCategoryService {

    private final BookInfoMapper bookInfoMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<AdminCategoryQueryRespDTO> queryCategories() {
        // 1. 过滤状态并排序（确保 parentId = 0 排在前面，避免顺序依赖）
        List<Category> categoryList = this.lambdaQuery()
                .orderByAsc(Category::getParentId) // 保证顶级分类先进入 Map
                .orderByAsc(Category::getSort)
                .list();
        Map<Long, AdminCategoryQueryRespDTO> categoryMap = new HashMap<>();
        List<AdminCategoryQueryRespDTO> list = new ArrayList<>();

        categoryList.forEach((category) -> {
            if (category.getParentId() == 0) {
                AdminCategoryQueryRespDTO categoryQueryRespDTO = new AdminCategoryQueryRespDTO(category.getId(), category.getName(), category.getSort(), 0L, category.getStatus(), new ArrayList<>());
                categoryMap.put(category.getId(), categoryQueryRespDTO);
                list.add(categoryQueryRespDTO);
            } else {
                AdminCategoryQueryRespDTO categoryQueryRespDTO = new AdminCategoryQueryRespDTO(category.getId(), category.getName(), category.getSort(), category.getParentId(), category.getStatus(), null);
                AdminCategoryQueryRespDTO parent = categoryMap.get(category.getParentId());
                if (parent != null) {
                    parent.getChildren().add(categoryQueryRespDTO);
                }
            }
        });
        return list;


    }

    @Override
    public PageResult<AdminCategoryPageRespDTO> pageCategories(AdminCategoryPageReqDTO reqDTO) {

        String name = reqDTO.getName();
        Integer channelId = reqDTO.getChannelId();
        Integer status = reqDTO.getStatus();


        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 1. 业务硬约束：只展示已上架的书籍 (status = 1)
        queryWrapper.eq(status != null, Category::getStatus, status)
                .eq(channelId != null, Category::getId, channelId)
                .like(StrUtil.isNotBlank(name), Category::getName, name);

        if (!StringUtils.hasText(reqDTO.getSortField())) {
            queryWrapper.orderByDesc(Category::getCreateTime);
        }


        // 6. 执行分页查询
        Page<Category> page = this.page(reqDTO.toPage(), queryWrapper);

        return PageResult.of(page, category -> BeanUtil.copyProperties(category, AdminCategoryPageRespDTO.class));
    }

    @Override
    public AdminCategoryAddRespDTO addCategory(AdminCategoryAddRepDTO categoryAddRepDTO) {
        Long parentId = categoryAddRepDTO.getParentId();
        String name = categoryAddRepDTO.getName();


        if (StrUtil.isBlank(name)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "分类名称不能为空");
        }
        if (parentId == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "父标签不能为空");
        }
        if (parentId != 0 && !this.lambdaQuery().eq(Category::getId, parentId).exists()) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXISTED, "父标签不存在");
        }
        Category category = BeanUtil.copyProperties(categoryAddRepDTO, Category.class);
        boolean save = this.save(category);
        if (!save) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        clearCache();

        return new AdminCategoryAddRespDTO(category.getId());

    }

    @Override
    public void updateCategory(Long id, AdminCategoryUpdateRepDTO categoryUpdateRepDTO) {
        Long parentId = categoryUpdateRepDTO.getParentId();
        String name = categoryUpdateRepDTO.getName();


        if (StrUtil.isBlank(name)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "分类名称不能为空");
        }
        if (parentId == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "父标签不能为空");
        }
        if (parentId != 0 && !this.lambdaQuery().eq(Category::getId, parentId).exists()) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXISTED, "父标签不存在");
        }
        Category category = BeanUtil.copyProperties(categoryUpdateRepDTO, Category.class);
        category.setId(id);
        boolean update = this.updateById(category);
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        clearCache();


    }

    @Override
    public void patchCategoryStatus(Long id, Integer status) {
        if (CategoryStatusEnum.getByCode(status) == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "状态不存在");
        }
        boolean exists = this.lambdaQuery().eq(Category::getId, id).exists();
        if (!exists) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXISTED);
        }
        boolean update = this.lambdaUpdate().set(status != null, Category::getStatus, status).eq(Category::getId, id).update();
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        clearCache();


    }

    @Override
    public void deleteCategory(Long id) {
        Category category = this.getById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXISTED);
        }
        boolean exists = this.lambdaQuery()
                .eq(Category::getParentId, id)
                .exists();
        if (exists) {
            throw new BusinessException(ResultCode.CATEGORY_SUB_EXISTED);
        }
        Long count = bookInfoMapper.selectCount(
                new LambdaQueryWrapper<BookInfo>()
                        .eq(BookInfo::getCategoryId, id)
                        .last("LIMIT 1")
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.CATEGORY_BOOK_EXISTED);
        }
        clearCache();
    }

    private void clearCache() {
        String key = RedisKeyConstants.CACHE_BOOK_CATEGORY;
        stringRedisTemplate.delete(key);
    }

}




