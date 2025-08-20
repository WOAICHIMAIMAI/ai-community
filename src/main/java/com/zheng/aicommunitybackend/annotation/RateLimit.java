package com.zheng.aicommunitybackend.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(RateLimits.class)
public @interface RateLimit {
    
    /**
     * 限流key的前缀
     */
    String key() default "";
    
    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.USER;
    
    /**
     * 时间窗口大小（秒）
     */
    int windowSize() default 60;
    
    /**
     * 窗口内最大请求数
     */
    int maxRequests() default 10;
    
    /**
     * 限流算法
     */
    Algorithm algorithm() default Algorithm.SLIDING_WINDOW;
    
    /**
     * 被限流时的提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
    
    /**
     * 限流类型枚举
     */
    enum LimitType {
        USER,    // 用户级限流
        IP,      // IP级限流
        GLOBAL   // 全局限流
    }
    
    /**
     * 限流算法枚举
     */
    enum Algorithm {
        SLIDING_WINDOW,  // 滑动窗口
        TOKEN_BUCKET,    // 令牌桶
        FIXED_WINDOW     // 固定窗口
    }
}
