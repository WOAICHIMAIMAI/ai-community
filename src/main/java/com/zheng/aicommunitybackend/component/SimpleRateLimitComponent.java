package com.zheng.aicommunitybackend.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 简化版限流组件 - 不使用Lua脚本
 */
@Slf4j
@Component
public class SimpleRateLimitComponent {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 简单的固定窗口限流
     * @param key 限流键
     * @param windowSize 时间窗口大小（秒）
     * @param maxRequests 窗口内最大请求数
     * @return 是否允许通过
     */
    public boolean simpleRateLimit(String key, int windowSize, int maxRequests) {
        try {
            // 使用当前时间窗口作为key的一部分
            long currentWindow = System.currentTimeMillis() / (windowSize * 1000);
            String windowKey = key + ":" + currentWindow;
            
            Long currentCount = redisTemplate.opsForValue().increment(windowKey);
            
            if (currentCount == 1) {
                // 第一次访问，设置过期时间
                redisTemplate.expire(windowKey, windowSize, TimeUnit.SECONDS);
            }
            
            boolean allowed = currentCount <= maxRequests;
            
            if (!allowed) {
                log.warn("限流触发，key: {}, 当前计数: {}, 限制: {}", windowKey, currentCount, maxRequests);
            }
            
            return allowed;
            
        } catch (Exception e) {
            log.error("限流组件异常，key: {}", key, e);
            // 异常时允许通过，避免影响业务
            return true;
        }
    }

    /**
     * 用户级限流
     */
    public boolean userRateLimit(Long userId, String action, int windowSize, int maxRequests) {
        String key = String.format("simple_rate_limit:user:%d:%s", userId, action);
        return simpleRateLimit(key, windowSize, maxRequests);
    }

    /**
     * 全局限流
     */
    public boolean globalRateLimit(String action, int windowSize, int maxRequests) {
        String key = String.format("simple_rate_limit:global:%s", action);
        return simpleRateLimit(key, windowSize, maxRequests);
    }

    /**
     * IP级限流
     */
    public boolean ipRateLimit(String ip, String action, int windowSize, int maxRequests) {
        String key = String.format("simple_rate_limit:ip:%s:%s", ip, action);
        return simpleRateLimit(key, windowSize, maxRequests);
    }
}
