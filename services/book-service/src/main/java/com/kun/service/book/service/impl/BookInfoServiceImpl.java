package com.kun.service.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.service.BookInfoService;
import com.kun.service.book.mapper.BookInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【book_info(图书信息主表)】的数据库操作Service实现
* @createDate 2026-09-25 10:39:19
*/
@Service
public class BookInfoServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo>
    implements BookInfoService{

}




