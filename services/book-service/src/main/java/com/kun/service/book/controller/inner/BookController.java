package com.kun.service.book.controller.inner;

import cn.hutool.core.bean.BeanUtil;
import com.kun.api.dto.book.BookDTO;
import com.kun.common.core.result.Result;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController("innerBookController")
@RequestMapping("/inner/book")
@RequiredArgsConstructor
public class BookController {

    private final BookInfoService bookInfoService;

    @GetMapping("/inner/book/{bookId}")
    Result<BookDTO> getBookById(@PathVariable("bookId") Long bookId) {
        BookInfo bookInfo = bookInfoService.getById(bookId);
        BookDTO bookDTO = BeanUtil.copyProperties(bookInfo, BookDTO.class);
        return Result.success(bookDTO);
    }

    @GetMapping("/inner/book/list")
    Result<List<BookDTO>> getBookListById(Collection<Long> ids) {
        List<BookInfo> bookInfoList = bookInfoService.listByIds(ids);
        List<BookDTO> bookDTOList = BeanUtil.copyToList(bookInfoList, BookDTO.class);
        return Result.success(bookDTOList);
    }


}
