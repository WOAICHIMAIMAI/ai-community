package com.zheng.aicommunitybackend.annotation;

import java.lang.annotation.*;

/**
 * 限流注解容器，支持多个限流注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimits {
    
    /**
     * 限流注解数组
     */
    RateLimit[] value();
}
