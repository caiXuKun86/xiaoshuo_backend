package com.kun.service.book.service.writer.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.service.book.domain.Author;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.dto.writer.req.WriterAuthorUpdateRespDTO;
import com.kun.service.book.dto.writer.resp.WriterAuthorProfileRespDTO;
import com.kun.service.book.mapper.AuthorMapper;
import com.kun.service.book.mapper.BookInfoMapper;
import com.kun.service.book.service.writer.WriterAuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lenovo
 * @description 针对表【author(作家/笔名表)】的数据库操作Service实现
 * @createDate 2026-09-25 10:39:19
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class WriterAuthorServiceImpl extends ServiceImpl<AuthorMapper, Author> implements WriterAuthorService {

    private final BookInfoMapper bookInfoMapper;


    @Override
    public WriterAuthorProfileRespDTO queryWriterProfile() {
        Author author = getCurrentAuthor();
        List<BookInfo> bookInfoList = bookInfoMapper.selectList(new LambdaQueryWrapper<BookInfo>()
                .eq(BookInfo::getAuthorId, author.getId())
        );
        WriterAuthorProfileRespDTO dto = new WriterAuthorProfileRespDTO();
        dto.setAuthorId(author.getId());
        dto.setUserId(author.getUserId());
        dto.setPenName(author.getPenName());
        dto.setAvatar(author.getAvatar());
        dto.setIntro(author.getIntro());
        dto.setStatus(author.getStatus());
        dto.setBookCount(bookInfoList.size());
        dto.setTotalWordCount(bookInfoList.stream().mapToLong(bookInfo -> bookInfo.getWordCount().longValue()).sum());
        dto.setCreateTime(author.getCreateTime());
        return dto;


    }

    @Override
    public void updateWriterProfile(WriterAuthorUpdateRespDTO authorUpdateRespDTO) {
        Author author = getCurrentAuthor();
        author.setAvatar(authorUpdateRespDTO.getAvatar());
        author.setIntro(authorUpdateRespDTO.getIntro());
        boolean update = this.updateById(author);
        if (!update) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
    }

    /**
     * 获取当前登录用户的作家
     */
    private Author getCurrentAuthor() {
        Long userId = UserContextHolder.get().getUserId();
        Author author = baseMapper.selectOne(
                new LambdaQueryWrapper<Author>().eq(Author::getUserId, userId)
        );
        if (author == null || author.getStatus() == 0) {
            throw new BusinessException(ResultCode.AUTHOR_BANNED);
        }
        return author;
    }

    /**
     * 获取当前登录用户的作家 ID
     */
    private Long getCurrentAuthorId() {
        return getCurrentAuthor().getId();
    }

    /**
     * 校验当前作品是否属于当前作家
     */
    private BookInfo validateBookOwnership(Long bookId) {
        Long authorId = getCurrentAuthorId();
        BookInfo book = bookInfoMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        // 彻底杜绝水平越权：即使黑客篡改了 bookId 也无法操作他人作品
        if (!book.getAuthorId().equals(authorId)) {
            throw new BusinessException(ResultCode.AUTHOR_NOT_PERMITTED); // 403: 无权操作他人作品
        }
        return book;
    }
}





