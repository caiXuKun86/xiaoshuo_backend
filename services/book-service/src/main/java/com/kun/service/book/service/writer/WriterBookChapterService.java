package com.kun.service.book.service.writer;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.service.book.domain.BookChapter;
import com.kun.service.book.dto.writer.req.WriterChapterDraftAddReqDTO;
import com.kun.service.book.dto.writer.req.WriterChapterUpdateReqDTO;
import com.kun.service.book.dto.writer.resp.*;

import java.util.List;

/**
 * @author Lenovo
 * @description 针对表【book_chapter(章节目录表)】的数据库操作Service
 * @createDate 2026-09-25 10:39:19
 */
public interface WriterBookChapterService extends IService<BookChapter> {


    List<WriterChapterListRespDTO> queryBookChapterList(Long bookId, String sortOrder);

    WriterChapterDetailRespDTO queryBookChapter(Long chapterId);

    WriterChapterDraftAddRespDTO addChapterDraft(WriterChapterDraftAddReqDTO chapterDraftAddReqDTO);

    WriterChapterPublishRespDTO publishChapter(WriterChapterDraftAddReqDTO chapterPublishReqDTO);

    WriterChapterUpdateRespDTO updateChapter(WriterChapterUpdateReqDTO chapterUpdateReqDTO, Long chapterId);

    void deleteChapter(Long chapterId);
}
