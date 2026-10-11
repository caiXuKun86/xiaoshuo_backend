package com.kun.service.book.controller.writer;

import com.kun.common.core.result.Result;
import com.kun.common.oss.template.OssTemplate;
import com.kun.service.book.dto.writer.req.WriterChapterDraftAddReqDTO;
import com.kun.service.book.dto.writer.req.WriterChapterUpdateReqDTO;
import com.kun.service.book.dto.writer.resp.*;
import com.kun.service.book.service.writer.WriterBookChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/book/writer/chapter")
@RequiredArgsConstructor
public class WriterBookChapterController {

    private final WriterBookChapterService bookChapterService;

    @GetMapping("/{bookId}/list")
    public Result<List<WriterChapterListRespDTO>> queryBookChapterList(@PathVariable("bookId") Long bookId, @RequestParam(name = "sortOrder", required = false, defaultValue = "ASC") String sortOrder) {
        List<WriterChapterListRespDTO> bookCatalogQueryRespDTO = bookChapterService.queryBookChapterList(bookId, sortOrder);
        return Result.success(bookCatalogQueryRespDTO);
    }


    @GetMapping("/{chapterId}")
    public Result<WriterChapterDetailRespDTO> queryBookChapter(@PathVariable("chapterId") Long chapterId) {
        WriterChapterDetailRespDTO chapterDetailRespDTO = bookChapterService.queryBookChapter(chapterId);
        return Result.success(chapterDetailRespDTO);
    }

    @PostMapping("/draft")
    public Result<WriterChapterDraftAddRespDTO> addChapterDraft(@RequestBody WriterChapterDraftAddReqDTO chapterDraftAddReqDTO) {
        WriterChapterDraftAddRespDTO chapterDraftAddRespDTO = bookChapterService.addChapterDraft(chapterDraftAddReqDTO);
        return Result.success(chapterDraftAddRespDTO);
    }


    @PostMapping("/publish")
    public Result<WriterChapterPublishRespDTO> publishChapter(@RequestBody WriterChapterDraftAddReqDTO chapterPublishReqDTO) {
        WriterChapterPublishRespDTO chapterPublishRespDTO = bookChapterService.publishChapter(chapterPublishReqDTO);
        return Result.success(chapterPublishRespDTO);
    }

    @PutMapping("/{chapterId}")
    public Result<WriterChapterUpdateRespDTO> updateChapter(@RequestBody WriterChapterUpdateReqDTO chapterUpdateReqDTO, @PathVariable("chapterId") Long chapterId) {
        WriterChapterUpdateRespDTO chapterUpdateRespDTO = bookChapterService.updateChapter(chapterUpdateReqDTO, chapterId);
        return Result.success(chapterUpdateRespDTO);
    }

    @DeleteMapping("/{chapterId}")
    public Result<Void> deleteChapter(@PathVariable Long chapterId) {
        bookChapterService.deleteChapter(chapterId);
        return Result.success();

    }
    private final OssTemplate ossTemplate;
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
