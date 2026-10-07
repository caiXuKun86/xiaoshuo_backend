package com.kun.service.book.controller.inner;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.kun.api.dto.book.BookDTO;
import com.kun.api.dto.book.ChapterDTO;
import com.kun.common.core.result.Result;
import com.kun.service.book.domain.BookChapter;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.service.BookChapterService;
import com.kun.service.book.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController("innerBookController")
@RequiredArgsConstructor
public class BookController {

    private final BookInfoService bookInfoService;
    private final BookChapterService bookChapterService;

    @GetMapping("/inner/book/{bookId}")
    Result<BookDTO> getBookById(@PathVariable("bookId") Long bookId) {
        BookInfo bookInfo = bookInfoService.getById(bookId);
        BookDTO bookDTO = BeanUtil.copyProperties(bookInfo, BookDTO.class);
        return Result.success(bookDTO);
    }

    @GetMapping("/inner/book/list")
    Result<List<BookDTO>> getBookListById(@RequestParam("ids") Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Result.success(Collections.emptyList()); // 或者直接做后续无需查书的逻辑
        }
        List<BookInfo> bookInfoList = bookInfoService.listByIds(ids);
        List<BookDTO> bookDTOList = BeanUtil.copyToList(bookInfoList, BookDTO.class);
        return Result.success(bookDTOList);
    }
    /**
     * 根据章节 ID 查询章节信息 (用于单章兑换、计费核验等)
     */
    @GetMapping("/inner/book/chapter/{chapterId}")
    Result<ChapterDTO> getChapterById(@PathVariable("chapterId") Long chapterId){
        BookChapter bookChapter = bookChapterService.getById(chapterId);
        ChapterDTO chapterDTO = BeanUtil.copyProperties(bookChapter, ChapterDTO.class);
        return Result.success(chapterDTO);
    }


}
