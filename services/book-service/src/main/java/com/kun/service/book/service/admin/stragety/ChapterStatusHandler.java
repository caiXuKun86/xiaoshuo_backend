package com.kun.service.book.service.admin.stragety;

import com.kun.common.core.enums.ChapterStatusEnum;
import com.kun.service.book.domain.BookChapter;

public interface ChapterStatusHandler {

    /**
     * 该策略负责处理的目标状态
     */
    ChapterStatusEnum getTargetStatus();

    /**
     * 执行具体的状态变更与衍生业务
     * @param chapter 当前章节对象
     * @param context 扩展上下文（如操作人ID、驳回原因等）
     */
    void handle(BookChapter chapter, ChapterStatusContext context);
}