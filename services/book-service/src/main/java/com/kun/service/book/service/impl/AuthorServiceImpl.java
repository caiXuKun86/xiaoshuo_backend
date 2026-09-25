package com.kun.service.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.service.book.domain.Author;
import com.kun.service.book.service.AuthorService;
import com.kun.service.book.mapper.AuthorMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【author(作家/笔名表)】的数据库操作Service实现
* @createDate 2026-09-25 10:39:19
*/
@Service
public class AuthorServiceImpl extends ServiceImpl<AuthorMapper, Author>
    implements AuthorService{

}




