package com.kun.service.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.dto.admin.resp.AdminBookInfoDashboardRespDTO;
import org.apache.ibatis.annotations.Param;

/**
* @author Lenovo
* @description 针对表【book_info(图书信息主表)】的数据库操作Mapper
* @createDate 2026-09-25 10:39:19
* @Entity com.kun.service.book.domain.BookInfo
*/
public interface BookInfoMapper extends BaseMapper<BookInfo> {

    AdminBookInfoDashboardRespDTO countBookStats( @Param("serializingCode") Integer serializingCode,
                                                  @Param("finishedCode") Integer finishedCode,
                                                  @Param("onShelfCode") Integer onShelfCode,
                                                  @Param("bannedCode") Integer bannedCode);

}




