package com.kun.common.database.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.kun.common.core.constant.NovelConstants;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis-Plus 核心拦截器配置
 * 1. 分页插件 (结合小说业务限制单页最大 100 条)
 * 2. 生产安全防护插件 (防止全表 UPDATE / DELETE 误操作)
 */
@AutoConfiguration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置单页最大限制 (防止超大 pageSize 拖垮数据库与内存)
        paginationInterceptor.setMaxLimit((long) NovelConstants.MAX_PAGE_SIZE);
        // 超出总页数时默认不循环
        paginationInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInterceptor);

        // 2. 防全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }
}
