package com.kun.service.book.service.writer;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.service.book.domain.Author;
import com.kun.service.book.dto.writer.req.WriterAuthorUpdateRespDTO;
import com.kun.service.book.dto.writer.resp.WriterAuthorProfileRespDTO;

/**
 * @author Lenovo
 * @description 针对表【author(作家/笔名表)】的数据库操作Service
 * @createDate 2026-09-25 10:39:19
 */
public interface WriterAuthorService extends IService<Author> {


    WriterAuthorProfileRespDTO queryWriterProfile();

    void updateWriterProfile(WriterAuthorUpdateRespDTO authorUpdateRespDTO);
}
