package com.zheng.aicommunitybackend.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 限流注解测试
 */
@SpringBootTest
public class RateLimitTest {

    /**
     * 测试方法，使用多个限流注解
     */
    @RateLimit(
        key = "test_user",
        limitType = RateLimit.LimitType.USER,
        windowSize = 1,
        maxRequests = 1,
        message = "用户限流"
    )
    @RateLimit(
        key = "test_global",
        limitType = RateLimit.LimitType.GLOBAL,
        windowSize = 1,
        maxRequests = 100,
        message = "全局限流"
    )
    public void testMethod() {
        // 测试方法
    }

    @Test
    public void testRepeatableAnnotation() throws NoSuchMethodException {
        // 获取测试方法
        Method method = this.getClass().getMethod("testMethod");
        
        // 获取所有RateLimit注解
        RateLimit[] rateLimits = method.getAnnotationsByType(RateLimit.class);
        
        // 验证注解数量
        assertEquals(2, rateLimits.length);
        
        // 验证第一个注解
        RateLimit userLimit = rateLimits[0];
        assertEquals("test_user", userLimit.key());
        assertEquals(RateLimit.LimitType.USER, userLimit.limitType());
        assertEquals(1, userLimit.windowSize());
        assertEquals(1, userLimit.maxRequests());
        assertEquals("用户限流", userLimit.message());
        
        // 验证第二个注解
        RateLimit globalLimit = rateLimits[1];
        assertEquals("test_global", globalLimit.key());
        assertEquals(RateLimit.LimitType.GLOBAL, globalLimit.limitType());
        assertEquals(1, globalLimit.windowSize());
        assertEquals(100, globalLimit.maxRequests());
        assertEquals("全局限流", globalLimit.message());
    }

    @Test
    public void testSingleAnnotation() throws NoSuchMethodException {
        // 获取只有单个注解的方法
        Method method = this.getClass().getMethod("singleAnnotationMethod");
        
        // 获取所有RateLimit注解
        RateLimit[] rateLimits = method.getAnnotationsByType(RateLimit.class);
        
        // 验证注解数量
        assertEquals(1, rateLimits.length);
        
        // 验证注解内容
        RateLimit rateLimit = rateLimits[0];
        assertEquals("single_test", rateLimit.key());
        assertEquals(RateLimit.LimitType.IP, rateLimit.limitType());
    }

    /**
     * 单个限流注解的测试方法
     */
    @RateLimit(
        key = "single_test",
        limitType = RateLimit.LimitType.IP,
        windowSize = 60,
        maxRequests = 10
    )
    public void singleAnnotationMethod() {
        // 测试方法
    }
}
