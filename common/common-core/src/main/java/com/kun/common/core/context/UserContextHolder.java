package com.kun.common.core.context;

/**
 * 用户上下文工具类 (基于 ThreadLocal)
 */
public final class UserContextHolder {

    private static final ThreadLocal<LoginUser> USER_CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {
    }

    /**
     * 设置当前登录用户信息
     */
    public static void set(LoginUser user) {
        USER_CONTEXT.set(user);
    }

    /**
     * 获取当前登录用户信息
     */
    public static LoginUser get() {
        return USER_CONTEXT.get();
    }

    /**
     * 获取当前登录用户的 ID
     */
    public static Long getUserId() {
        LoginUser user = USER_CONTEXT.get();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 清空当前线程上下文 (防止内存泄漏与线程池污染)
     */
    public static void clear() {
        USER_CONTEXT.remove();
    }
}
