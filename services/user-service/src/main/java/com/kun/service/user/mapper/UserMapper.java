package com.kun.service.user.mapper;

import com.kun.service.user.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Lenovo
* @description 针对表【user(用户基础信息表)】的数据库操作Mapper
* @createDate 2026-09-23 09:13:15
* @Entity com.kun.service.user.domain.User
*/
public interface UserMapper extends BaseMapper<User> {

    User selectByUserIdForUpdate(Long userId);
}




