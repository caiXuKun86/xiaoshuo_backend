package com.kun.common.core.constant;

/**
 * 小说系统通用业务常量
 */
public interface NovelConstants {


    /**
     * 默认分页起始页码
     */
    int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认分页每页大小
     */
    int DEFAULT_PAGE_SIZE = 20;

    /**
     * 单页最大允许记录数 (防拖垮数据库)
     */
    int MAX_PAGE_SIZE = 100;

    /**
     * 个人书架最大容量限制 (500本)
     */
    int MAX_SHELF_CAPACITY = 500;

    /**
     * 标准日期时间格式
     */
    String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 标准日期格式
     */
    String DATE_FORMAT = "yyyy-MM-dd";
}
