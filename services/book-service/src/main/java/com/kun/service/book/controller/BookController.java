package com.kun.service.book.controller;

import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.common.oss.template.OssTemplate;
import com.kun.service.book.dto.req.BookPageReqDTO;
import com.kun.service.book.dto.req.BookPublishReqDTO;
import com.kun.service.book.dto.resp.BookCatalogQueryRespDTO;
import com.kun.service.book.dto.resp.BookDetailQueryRespDTO;
import com.kun.service.book.dto.resp.BookPageRespDTO;
import com.kun.service.book.dto.resp.BookPublishRespDTO;
import com.kun.service.book.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookController {

    private final BookInfoService bookInfoService;
    private final OssTemplate ossTemplate;

    @GetMapping("/filter")
    public Result<PageResult<BookPageRespDTO>> pageBook(BookPageReqDTO bookPageReqDTO) {
        PageResult<BookPageRespDTO> pageResult = bookInfoService.pageBook(bookPageReqDTO);
        return Result.success(pageResult);

    }

    //TODO 远程调用获取isInBookshelf userScore lastReadChapterId lastReadChapterName
    //TODO 缓存
    @GetMapping("/detail/{id}")
    public Result<BookDetailQueryRespDTO> queryBookDetail(@PathVariable("id") Long id) {
        BookDetailQueryRespDTO bookDetailQueryRespDTO = bookInfoService.queryBookDetailById(id);
        return Result.success(bookDetailQueryRespDTO);
    }

    @GetMapping("/{bookId}/catalog")
    public Result<BookCatalogQueryRespDTO> queryBookCatalog(@PathVariable("bookId") Long bookId, @RequestParam(name = "sortOrder", required = false, defaultValue = "ASC") String sortOrder) {
        BookCatalogQueryRespDTO bookCatalogQueryRespDTO = bookInfoService.queryBookCatalogById(bookId, sortOrder);
        return Result.success(bookCatalogQueryRespDTO);
    }

    @PostMapping("/publish")
    public Result<BookPublishRespDTO> publishBook(@RequestBody BookPublishReqDTO bookPublishReqDTO) {
        BookPublishRespDTO bookPublishRespDTO = bookInfoService.publishBook(bookPublishReqDTO);
        return Result.success(bookPublishRespDTO);

    }
    @PostMapping("/cover")
    public Result<String> uploadCover(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }

        // 1. 校验图片类型 (jpg, png, webp 等)
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        // 2. 规划 OSS 存储路径 (例如: cover/2026/09/uuid.jpg)
        String objectName = "cover/" + UUID.randomUUID().toString().replace("-", "") + suffix;
        // 3. 上传并返回外链可访问的 URL
        String fileUrl = ossTemplate.uploadFile(objectName, file.getInputStream());
        return Result.success(fileUrl);
    }


}
