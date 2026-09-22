package com.kun.common.redis.constant;

/**
 * 全局 Redis 键名前缀规范常量
 * 严格对应系统架构设计与各微服务高频缓存场景
 */
public final class RedisKeyConstants {

    private RedisKeyConstants() {
    }

    /**
     * 基础分隔符
     */
    public static final String SPLIT = ":";

    // ==================== 1. API 网关 (Gateway) ====================
    /**
     * 网关 IP 令牌桶限流 Key: gateway:rate_limit:ip:{ip}
     */
    public static final String GATEWAY_IP_RATE_LIMIT_PREFIX = "gateway:rate_limit:ip:";

    /**
     * IP 黑名单 Key: gateway:blacklist:ip
     */
    public static final String GATEWAY_IP_BLACKLIST = "gateway:blacklist:ip";

    // ==================== 2. 用户与资产服务 (User Service) ====================
    /**
     * 用户访问令牌 Key: user:token:{userId}
     */
    public static final String USER_TOKEN_PREFIX = "user:token:";

    /**
     * 用户双 Token 刷新凭据 Key: user:refresh_token:{userId}
     */
    public static final String USER_REFRESH_TOKEN_PREFIX = "user:refresh_token:";

    /**
     * 每日连续签到打卡 BitMap Key: user:signin:{userId}:{yyyyMM}
     */
    public static final String USER_SIGNIN_BITMAP_PREFIX = "user:signin:";

    /**
     * 用户资产信息缓存 Key: user:asset:{userId}
     */
    public static final String USER_ASSET_CACHE_PREFIX = "user:asset:";

    // ==================== 3. 图书与内容服务 (Book Service) ====================
    /**
     * 图书基础元数据缓存 Key: book:info:{bookId}
     */
    public static final String BOOK_INFO_PREFIX = "book:info:";

    /**
     * 章节最新 20 章热门正文缓存 Key: book:chapter:content:{chapterId}
     */
    public static final String BOOK_CHAPTER_CONTENT_PREFIX = "book:chapter:content:";

    /**
     * 图书章节目录树缓存 Key: book:catalog:{bookId}
     */
    public static final String BOOK_CATALOG_PREFIX = "book:catalog:";

    // ==================== 4. 进度与书架服务 (Shelf Service) ====================
    /**
     * 跨端高频阅读进度实时缓存 (防抖打点暂存) Key: shelf:progress:{userId}:{bookId}
     */
    public static final String SHELF_PROGRESS_PREFIX = "shelf:progress:";

    /**
     * 用户书架列表缓存 Key: shelf:list:{userId}
     */
    public static final String SHELF_LIST_PREFIX = "shelf:list:";

    // ==================== 5. 互动评论服务 (Comment Service) ====================
    /**
     * 评论点赞去重与计数 Set: comment:likes:{commentId}
     */
    public static final String COMMENT_LIKE_SET_PREFIX = "comment:likes:";

    /**
     * 段评气泡聚合统计 Hash: comment:paragraph_stat:{chapterId} (field: paragraph_index, value: count)
     */
    public static final String COMMENT_PARA_STAT_PREFIX = "comment:paragraph_stat:";

    /**
     * 用户发评冷却限制 Key: comment:cooldown:{userId}
     */
    public static final String COMMENT_COOLDOWN_PREFIX = "comment:cooldown:";

    // ==================== 6. 交易支付服务 (Pay Service) ====================
    /**
     * 充值下单防重分布式锁 Key: pay:lock:order:{idempotentKey}
     */
    public static final String PAY_ORDER_LOCK_PREFIX = "pay:lock:order:";

    /**
     * 章节兑换并发防重分布式锁 Key: pay:lock:exchange:{userId}:{chapterId}
     */
    public static final String CHAPTER_EXCHANGE_LOCK_PREFIX = "pay:lock:exchange:";

    // ==================== 7. 搜索与榜单服务 (Search Service) ====================
    /**
     * 全网热搜词加权排序 ZSet: search:hot_words
     */
    public static final String SEARCH_HOT_WORDS_ZSET = "search:hot_words";

    /**
     * 小说多维排行通用前缀 ZSet: search:rank:{rankType} (如 monthly, ticket, new)
     */
    public static final String SEARCH_RANK_ZSET_PREFIX = "search:rank:";
}
