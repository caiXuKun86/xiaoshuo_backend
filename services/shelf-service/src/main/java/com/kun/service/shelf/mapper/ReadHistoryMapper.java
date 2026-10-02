package com.kun.service.shelf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kun.service.shelf.domain.ReadHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【read_history(用户阅读历史足迹表)】的数据库操作Mapper
* @createDate 2026-09-30 20:32:27
* @Entity com.kun.service.shelf.domain.ReadHistory
*/
public interface ReadHistoryMapper extends BaseMapper<ReadHistory> {

    int deleteByBookIds(@Param("bookIds") List<Long> bookIds, @Param("userId") Long userId);
}




