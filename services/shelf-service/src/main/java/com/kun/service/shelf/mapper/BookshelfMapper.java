package com.kun.service.shelf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kun.service.shelf.domain.Bookshelf;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【bookshelf(用户书架表)】的数据库操作Mapper
* @createDate 2026-09-30 20:32:27
* @Entity com.kun.service.shelf.domain.domain.Bookshelf
*/
public interface BookshelfMapper extends BaseMapper<Bookshelf> {

    int removeByBookIds(@Param("ids") List<Long> ids, @Param("userId") Long userId);

}




