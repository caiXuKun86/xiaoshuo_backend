package com.kun.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局业务统一状态码体系（严格对照接口契约规范）
 */
@Getter
@AllArgsConstructor
public enum ResultCode implements IResultCode {

    // ==================== 200 全局成功 ====================
    SUCCESS(200, "操作成功"),
    //服务不可用
    OPERATION_FAILED(500,"操作失败"),


    // ==================== 10000 ~ 10099 基础与网关层 ====================
    UNAVAILABLE_SERVICE(10000,"服务不可用"),
    PARAM_INVALID(10001, "请求参数格式错误或必填项为空"),
    REQUEST_RATE_LIMIT(10002, "操作过于频繁，请稍后再试"),
    IP_BLOCKED(10003, "访问异常，当前 IP 已被临时限制"),
    IDEMPOTENT_REPEATED(10004, "正在处理中，请勿重复提交"),
    SERVICE_DEGRADED(10005, "系统繁忙，核心服务正在维护中"),

    // ==================== 10100 ~ 10199 安全与认证鉴权 ====================
    UNAUTHORIZED(10101, "用户尚未登录，请先登录"),
    ACCESS_TOKEN_EXPIRED(10102, "登录令牌已过期"),
    REFRESH_TOKEN_EXPIRED(10103, "登录凭证已失效，请重新登录"),
    TOKEN_INVALID(10104, "登录凭据解析失败，非法访问"),
    FORBIDDEN_ACCESS(10105, "当前账号权限不足"),
    USER_BANNED(10106, "账号存在违规行为已被封禁"),


    // ==================== 20000 ~ 29999 用户与资产服务 (User) ====================
    USER_ALREADY_EXISTS(20001, "该用户名已被注册"),
    USER_NOT_FOUND(20002, "用户不存在"),
    PASSWORD_ERROR(20003, "用户名或密码错误"),
    ALREADY_CHECKED_IN(20004, "今日已完成签到，请明日再来"),
    INVITATION_CODE_INVALID(20005, "邀请码不存在或已达上限"),
    INSUFFICIENT_POINTS(20006, "可用积分余额不足"),

    // ==================== 30000 ~ 39999 图书与内容服务 (Book) ====================
    BOOK_NOT_FOUND(30001, "作品不存在或已下架"),
    CHAPTER_NOT_FOUND(30002, "章节不存在或已被作者删除"),
    CHAPTER_NEED_PURCHASE(30003, "本章节为付费章节，需要解锁后阅读"),
    OSS_CONTENT_FETCH_FAIL(30004, "章节正文加载失败，请重试"),
    PAY_INSUFFICIENT_POINTS(30005, "账户可用积分不足，无法完成兑换"),
    CHAPTER_ALREADY_UNLOCKED(30006, "该章节已解锁，无需重复购买"),
    PAN_NAME_ALREADY_EXISTED(30007, "笔名已存在"),
    WRITER_ALREADY_REGISTER(30008, "您已经是认证作家"),
    NOT_AN_AUTHOR(30009, "非作家"),
    AUTHOR_BANNED(30010, "您已被封禁"),
    ALREADY_HAVE_BOOK(30011, "书名重复"),
    CATEGORY_NOT_EXISTED(30012, "分类不存在"),
    AUTHOR_NOT_PERMITTED(30013, "无权修改或操作他人作品与章节"),
    UPLOAD_CONTENT_FAILED(30014, "内容上传失败,请重试"),
    ALREADY_PUBLISH_INDEX(30015, "您已发送过本章节"),
    BOOK_IS_OVER(30016, "已完结,不支持发布新章节"),

    // ==================== 40000 ~ 49999 进度与书架服务 (Shelf) ====================
    SHELF_ALREADY_EXISTS(40001, "该书已存在于您的书架中"),
    SHELF_CAPACITY_LIMIT(40002, "书架容量已达上限 (最多 500 本)"),
    PROGRESS_SYNC_CONFLICT(40003, "阅读进度版本冲突"),

    // ==================== 50000 ~ 59999 互动评论服务 (Comment) ====================
    RATING_SCORE_ILLEGAL(50001, "评分分值非法（必须在 1 到 5 之间）"),
    ALREADY_RATING(50002, "已评分"),
    COMMENT_SENSITIVE_REJECT(50003, "评论内容包含敏感违规词，无法发布"),
    COMMENT_FREQUENCY_LIMIT(50004, "发言过于频繁，请休息一下再发"),
    COMMENT_NOT_FOUND(50005, "评论不存在或已被原作者删除"),
    PARENT_COMMENT_NOT_FOUND(50006, "目标评论不存在或已被删除"),
    NOAUTH_OP_COMMENT(50007, "无权操作该评论"),
    ALREADY_RATED(50008, "您已经为本书评过分了"),

    // ==================== 60000 ~ 69999 交易支付服务 (Pay) ====================

    PAY_SKU_NOT_FOUND(60001, "充值套餐不存在或已下架"),
    ORDER_CREATE_FAILED(60002, "订单创建失败"),
    ORDER_NOT_FOUND(60003, "订单不存在"),
    ORDER_AMOUNT_NOT_EQUAL(60003, "支付金额与订单金额不一致"),
    PAY_ORDER_EXPIRED(60004, "订单支付已超时关闭，请重新下单"),
    PAY_SIGN_VERIFY_FAIL(60005, "支付回调签名校验失败"),
    PAY_CHANNEL_TIMEOUT(60006, "第三方支付渠道响应超时"),

    // ==================== 70000 ~ 79999 搜索与榜单服务 (Search) ====================
    SEARCH_KEYWORD_BLANK(70001, "搜索关键词不能为空"),
    RANK_TYPE_NOT_FOUND(70002, "榜单类型不存在"),

    // ==================== 99999 系统兜底异常 ====================
    SYSTEM_ERROR(99999, "系统未知异常，请稍后重试");

    private final Integer code;
    private final String message;
}
