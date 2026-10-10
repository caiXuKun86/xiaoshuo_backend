package com.kun.service.book.controller;

import com.kun.common.core.result.Result;
import com.kun.common.database.page.PageResult;
import com.kun.common.oss.template.OssTemplate;
import com.kun.service.book.dto.req.BookInfoPageReqDTO;
import com.kun.service.book.dto.req.BookPublishReqDTO;
import com.kun.service.book.dto.resp.BookCatalogQueryRespDTO;
import com.kun.service.book.dto.resp.BookInfoDetailRespDTO;
import com.kun.service.book.dto.resp.BookInfoPageRespDTO;
import com.kun.service.book.dto.resp.BookPublishRespDTO;
import com.kun.service.book.service.BookInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookInfoController {

    private final BookInfoService bookInfoService;
    private final OssTemplate ossTemplate;

    @GetMapping("/filter")
    public Result<PageResult<BookInfoPageRespDTO>> queryBookInfoPage(BookInfoPageReqDTO bookInfoPageReqDTO) {
        PageResult<BookInfoPageRespDTO> pageResult = bookInfoService.queryBookInfoPage(bookInfoPageReqDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/detail/{id}")
    public Result<BookInfoDetailRespDTO> queryBookInfoDetail(@PathVariable("id") Long id) {
        BookInfoDetailRespDTO bookDetailQueryRespDTO = bookInfoService.queryBookInfoDetail(id);
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
    @PostMapping("/over/{bookId}")
    public Result<Void> overBook(@PathVariable("bookId") Long bookId){
        bookInfoService.overBook(bookId);
        return Result.success();

    }

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @PostMapping("/cover/upload")
    public Result<String> uploadCover(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }
        // 2. 校验文件大小 (不超过 2MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.fail("封面图片大小不能超过 2MB");
        }
        // 3. 校验文件后缀名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.fail("上传文件缺少后缀名");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(suffix)) {
            return Result.fail("仅支持 JPG、PNG、WEBP 格式的图片");
        }
        // 4. (推荐) 校验 MIME 类型，防止恶意修改文件后缀上传
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            return Result.fail("非法图片格式");
        }
        // 5. 规划 OSS 存储路径并上传
        String objectName = "cover/" + UUID.randomUUID().toString().replace("-", "") + suffix;
        String fileUrl = ossTemplate.uploadFile(objectName, file.getInputStream());
        return Result.success(fileUrl);
    }


}
