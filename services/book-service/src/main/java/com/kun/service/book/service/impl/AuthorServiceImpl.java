package com.kun.service.book.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.core.enums.BookOpStatusEnum;
import com.kun.service.book.domain.Author;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.dto.resp.AuthorDetailQueryRespDTO;
import com.kun.service.book.mapper.BookInfoMapper;
import com.kun.service.book.service.AuthorService;
import com.kun.service.book.mapper.AuthorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lenovo
 * @description 针对表【author(作家/笔名表)】的数据库操作Service实现
 * @createDate 2026-09-25 10:39:19
 */
@RequiredArgsConstructor
@Service
public class AuthorServiceImpl extends ServiceImpl<AuthorMapper, Author> implements AuthorService {

    private final BookInfoMapper bookInfoMapper;

    @Override
    public AuthorDetailQueryRespDTO queryBookDetailById(Long id) {

        Author author = this.getById(id);

        List<BookInfo> bookInfoList = bookInfoMapper.selectList(
                new LambdaQueryWrapper<BookInfo>()
                        .eq(BookInfo::getAuthorId, author.getId())
                        .eq(BookInfo::getStatus, BookOpStatusEnum.ON_SHELF.getCode())
        );
        AuthorDetailQueryRespDTO authorDetailQueryRespDTO = new AuthorDetailQueryRespDTO();
        BeanUtil.copyProperties(author, authorDetailQueryRespDTO);
        authorDetailQueryRespDTO.setAuthorId(author.getId());

        authorDetailQueryRespDTO.setTotalBookCount(bookInfoList.size());
        authorDetailQueryRespDTO.setTotalWordCount(bookInfoList.stream()
                .mapToInt(book -> book.getWordCount() != null ? book.getWordCount() : 0)
                .sum());
        authorDetailQueryRespDTO.setBooks(bookInfoList.stream().map(bookInfo -> {
            AuthorDetailQueryRespDTO.BookInfoDTO bookInfoDTO = new AuthorDetailQueryRespDTO.BookInfoDTO();
            BeanUtil.copyProperties(bookInfo, bookInfoDTO);
            return bookInfoDTO;
        }).collect(Collectors.toList()));
        return authorDetailQueryRespDTO;

    }
}




