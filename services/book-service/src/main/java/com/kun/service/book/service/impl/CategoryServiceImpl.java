package com.kun.service.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.redis.constant.RedisKeyConstants;
import com.kun.common.redis.util.CacheUtil;
import com.kun.service.book.domain.Category;
import com.kun.service.book.dto.resp.CategoryQueryRespDTO;
import com.kun.service.book.mapper.CategoryMapper;
import com.kun.service.book.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Lenovo
 * @description 针对表【category(图书分类表)】的数据库操作Service实现
 * @createDate 2026-09-25 10:39:19
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final CacheUtil cacheUtil;

    @Override
    public List<CategoryQueryRespDTO> queryCategories() {
        return cacheUtil.queryListWithMutex(
                RedisKeyConstants.CACHE_BOOK_CATEGORY,
                CategoryQueryRespDTO.class,
                () -> {
                    // 1. 过滤状态并排序（确保 parentId = 0 排在前面，避免顺序依赖）
                    List<Category> categoryList = this.lambdaQuery()
                            .eq(Category::getStatus, 1)
                            .orderByAsc(Category::getParentId) // 保证顶级分类先进入 Map
                            .orderByAsc(Category::getSort)
                            .list();
                    Map<Long, CategoryQueryRespDTO> categoryMap = new HashMap<>();
                    List<CategoryQueryRespDTO> list = new ArrayList<>();

                    categoryList.forEach((category) -> {
                        if (category.getParentId() == 0) {
                            CategoryQueryRespDTO categoryQueryRespDTO = new CategoryQueryRespDTO(category.getId(), category.getName(), category.getSort(), new ArrayList<>());
                            categoryMap.put(category.getId(), categoryQueryRespDTO);
                            list.add(categoryQueryRespDTO);
                        } else {
                            CategoryQueryRespDTO categoryQueryRespDTO = new CategoryQueryRespDTO(category.getId(), category.getName(), category.getSort(), null);
                            CategoryQueryRespDTO parent = categoryMap.get(category.getParentId());
                            if (parent != null) {
                                parent.getChildren().add(categoryQueryRespDTO);
                            }
                        }
                    });
                    return list;
                },
                2L,
                TimeUnit.HOURS
        );




    }
}




