package com.kun.service.comment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.service.comment.domain.CommentInfo;
import com.kun.service.comment.service.CommentInfoService;
import com.kun.service.comment.mapper.CommentInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【comment_info(评论互动主表)】的数据库操作Service实现
* @createDate 2026-10-02 19:42:59
*/
@Service
public class CommentInfoServiceImpl extends ServiceImpl<CommentInfoMapper, CommentInfo>
    implements CommentInfoService{

}




