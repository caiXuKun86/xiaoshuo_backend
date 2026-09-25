package com.kun.service.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.service.book.domain.BookChapter;
import com.kun.service.book.service.BookChapterService;
import com.kun.service.book.mapper.BookChapterMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【book_chapter(章节目录表)】的数据库操作Service实现
* @createDate 2026-09-25 10:39:19
*/
@Service
public class BookChapterServiceImpl extends ServiceImpl<BookChapterMapper, BookChapter>
    implements BookChapterService{

}




