package com.zheng.aicommunitybackend.common;

/**
 * 系统常量
 */
public class Constants {
    /**
     * 用户角色：普通用户
     */
    public static final String ROLE_USER = "0";

    /**
     * 用户角色：管理员
     */
    public static final String ROLE_ADMIN = "1";
    
    /**
     * ThreadLocal存储用户ID的Key
     */
    public static final String CURRENT_USER_ID = "current_user_id";
    
    /**
     * ThreadLocal存储用户名的Key
     */
    public static final String CURRENT_USERNAME = "current_username";
    
    /**
     * ThreadLocal存储用户角色的Key
     */
    public static final String CURRENT_USER_ROLE = "current_user_role";
    
    /**
     * 状态码：成功
     */
    public static final int SUCCESS_CODE = 200;
    
    /**
     * 状态码：失败
     */
    public static final int ERROR_CODE = 500;
    
    /**
     * 状态码：未授权
     */
    public static final int UNAUTHORIZED_CODE = 401;
    
    /**
     * 状态码：禁止访问
     */
    public static final int FORBIDDEN_CODE = 403;
    
    /**
     * 状态码：资源不存在
     */
    public static final int NOT_FOUND_CODE = 404;
} 