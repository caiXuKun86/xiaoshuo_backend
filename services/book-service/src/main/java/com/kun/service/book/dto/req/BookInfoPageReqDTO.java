package com.kun.service.book.dto.req;

import cn.hutool.crypto.digest.DigestUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookInfoPageReqDTO extends com.kun.common.database.page.PageRequest {

    /**
     * 频道筛选
     */
    private Integer channelId = 0;

    /**
     * 二级分类 ID
     */
    private Long categoryId;

    /**
     * 连载状态
     */
    private Integer bookStatus;

    public String generateCacheKey() {
        // 1. 规范化拼接：保证 null 字段有统一的占位标识，防止参数顺序或空值导致 Hash 漂移
        String rawParams = String.format(
                "c:%d_cat:%s_bs:%s_p:%d_s:%d_sf:%s_asc:%s",
                this.channelId != null ? this.channelId : 0,
                this.categoryId != null ? this.categoryId : 0,
                this.bookStatus != null ? this.bookStatus : -1,
                this.getPageNum(),
                this.getPageSize(),
                this.getSortField() != null ? this.getSortField() : "default",
                this.getIsAsc() != null ? this.getIsAsc() : false
        );
        // 2. MD5 压缩成固定 32 位十六进制字符串
        // 最终 Key 形如: book:filter:page:e10adc3949ba59abbe56e057f20f883e
        return "book:filter:page:" + DigestUtil.md5Hex(rawParams);
    }

}
