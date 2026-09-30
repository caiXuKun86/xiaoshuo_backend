package com.kun.service.book.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.api.client.UserFeignClient;
import com.kun.api.dto.user.RegisterWriterDTO;
import com.kun.common.core.context.UserContextHolder;
import com.kun.common.core.enums.AuthorStatusTypeEnum;
import com.kun.common.core.enums.BookOpStatusEnum;
import com.kun.common.core.enums.ResultCode;
import com.kun.common.core.exception.BusinessException;
import com.kun.common.core.result.Result;
import com.kun.service.book.domain.Author;
import com.kun.service.book.domain.BookInfo;
import com.kun.service.book.dto.req.AuthorRegisterReqDTO;
import com.kun.service.book.dto.resp.AuthorDetailQueryRespDTO;
import com.kun.service.book.dto.resp.AuthorRegisterRespDTO;
import com.kun.service.book.mapper.BookInfoMapper;
import com.kun.service.book.service.AuthorService;
import com.kun.service.book.mapper.AuthorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthorServiceImpl extends ServiceImpl<AuthorMapper, Author> implements AuthorService {

    private final BookInfoMapper bookInfoMapper;
    private final UserFeignClient userFeignClient;

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

    @Override
    public AuthorRegisterRespDTO registerAuthor(AuthorRegisterReqDTO authorRegisterReqDTO) {
        Long userId = UserContextHolder.getUserId();
        String penName = authorRegisterReqDTO.getPenName();
        String avatar = authorRegisterReqDTO.getAvatar();
        String intro = authorRegisterReqDTO.getIntro();

        Long count = this.lambdaQuery()
                .eq(Author::getUserId, userId)
                .count();
        if (count > 0) {
            throw new BusinessException(ResultCode.WRITER_ALREADY_REGISTER);
        }
        count = this.lambdaQuery()
                .eq(Author::getPenName, penName)
                .count();
        if (count > 0) {
            throw new BusinessException(ResultCode.PAN_NAME_ALREADY_EXISTED);
        }

        Result<Boolean> booleanResult = userFeignClient.registerWriter(RegisterWriterDTO.builder().isWriter(1).build());
        if (booleanResult == null || booleanResult.getCode() != 200 || !Boolean.TRUE.equals(booleanResult.getData())) {
            throw new BusinessException(ResultCode.UNAVAILABLE_SERVICE, "更新用户信息失败");
        }
        Author author = new Author();
        author.setUserId(userId);
        author.setPenName(penName);
        author.setStatus(AuthorStatusTypeEnum.NORMAL.getCode());
        if(avatar==null){
            author.setAvatar(avatar);
        }
        author.setAvatar(avatar);
        author.setIntro(intro);
        author.setStatus(AuthorStatusTypeEnum.FORBIDDEN.getCode());
        boolean save = this.save(author);
        if (!save) {
            boolean isSuccess = false;
            String failReason = "";
            try {
                // 1. 显式接收远程调用结果
                Result<Boolean> result = userFeignClient.registerWriter(RegisterWriterDTO.builder().isWriter(0).build());

                // 2. 严谨校验：判空、状态码、返回的业务布尔值
                if (result == null) {
                    failReason = "远程返回结果为 null";
                } else if (result.getCode() != 200) {
                    failReason = "下游业务处理失败: " + result.getMessage() + " (code: " + result.getCode() + ")";
                } else if (!Boolean.TRUE.equals(result.getData())) {
                    failReason = "修改操作返回 false";
                } else {
                    // 真正成功
                    isSuccess = true;
                    log.info("更改用户信息成功, userId: {}", userId);
                }
            } catch (Exception ex) {
                // 捕获网络超时、连接拒绝等真正的底层异常
                failReason = "Feign 调用底层抛出异常: " + ex.getMessage();
                log.error("调用用户服务退款异常", ex);
            }

            // 3. 统一处理失败逻辑（无论是网络不通，还是下游业务返回失败）
            if (!isSuccess) {
                log.error("【严重警告】更改用户信息失败，需人工介入！userId: {},  原因: {}",
                        userId, failReason);

            }
        }
        AuthorRegisterRespDTO authorRegisterRespDTO = new AuthorRegisterRespDTO();
        BeanUtil.copyProperties(author, authorRegisterRespDTO);

        return authorRegisterRespDTO;

    }
}




