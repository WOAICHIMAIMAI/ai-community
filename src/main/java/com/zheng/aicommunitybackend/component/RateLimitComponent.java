package com.zheng.aicommunitybackend.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 限流组件
 */
@Slf4j
@Component
public class RateLimitComponent {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 滑动窗口限流
     * @param key 限流键
     * @param windowSize 时间窗口大小（秒）
     * @param maxRequests 窗口内最大请求数
     * @return 是否允许通过
     */
    public boolean slidingWindowRateLimit(String key, int windowSize, int maxRequests) {
        String luaScript =
                """
                        local key = KEYS[1]
                        local window = tonumber(ARGV[1])
                        local limit = tonumber(ARGV[2])
                        local current = tonumber(ARGV[3])
                        
                        -- 参数验证
                        if not window or not limit or not current then
                            return 0
                        end
                        if window <= 0 or limit <= 0 then
                            return 0
                        end
                        
                        -- 清除旧数据
                        redis.call('zremrangebyscore', key, 0, current - window * 1000)
                        
                        local currentRequests = redis.call('zcard', key)
                        if currentRequests < limit then
                            redis.call('zadd', key, current, current)
                            -- 设置过期时间避免内存泄漏
                            redis.call('expire', key, window)
                            return 1
                        else
                            return 0
                        end
                        """;
        log.debug("滑动窗口限流，key: {}, windowSize: {}, maxRequests: {}", key, windowSize, maxRequests);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script,
                Arrays.asList(key),
                String.valueOf(windowSize),
                String.valueOf(maxRequests),
                String.valueOf(System.currentTimeMillis()));

        return result != null && result == 1;
    }

    /**
     * 令牌桶限流
     * @param key 限流键
     * @param capacity 桶容量
     * @param refillRate 令牌补充速率（每秒）
     * @return 是否允许通过
     */
    public boolean tokenBucketRateLimit(String key, int capacity, int refillRate) {
        String luaScript =
            "local key = KEYS[1] " +
            "local capacity = tonumber(ARGV[1]) " +
            "local refillRate = tonumber(ARGV[2]) " +
            "local currentTime = tonumber(ARGV[3]) " +
            "local bucket = redis.call('hmget', key, 'tokens', 'lastRefill') " +
            "local tokens = tonumber(bucket[1]) or capacity " +
            "local lastRefill = tonumber(bucket[2]) or currentTime " +
            "local timePassed = math.max(0, currentTime - lastRefill) " +
            "local tokensToAdd = math.floor(timePassed / 1000 * refillRate) " +
            "tokens = math.min(capacity, tokens + tokensToAdd) " +
            "if tokens >= 1 then " +
                "tokens = tokens - 1 " +
                "redis.call('hmset', key, 'tokens', tokens, 'lastRefill', currentTime) " +
                "redis.call('expire', key, 3600) " +
                "return 1 " +
            "else " +
                "redis.call('hmset', key, 'tokens', tokens, 'lastRefill', currentTime) " +
                "redis.call('expire', key, 3600) " +
                "return 0 " +
            "end";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script,
                Arrays.asList(key),
                String.valueOf(capacity),
                String.valueOf(refillRate),
                String.valueOf(System.currentTimeMillis()));

        return result != null && result == 1;
    }

    /**
     * 固定窗口限流
     * @param key 限流键
     * @param windowSize 时间窗口大小（秒）
     * @param maxRequests 窗口内最大请求数
     * @return 是否允许通过
     */
    public boolean fixedWindowRateLimit(String key, int windowSize, int maxRequests) {
        String luaScript =
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local window = tonumber(ARGV[2]) " +
            "local current = redis.call('incr', key) " +
            "if current == 1 then " +
                "redis.call('expire', key, window) " +
            "end " +
            "if current <= limit then " +
                "return 1 " +
            "else " +
                "return 0 " +
            "end";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script,
                Arrays.asList(key),
                String.valueOf(maxRequests),
                String.valueOf(windowSize));

        return result != null && result == 1;
    }

    /**
     * 分布式限流（基于计数器）
     * @param key 限流键
     * @param maxRequests 最大请求数
     * @param windowSeconds 时间窗口（秒）
     * @return 是否允许通过
     */
    public boolean distributedRateLimit(String key, int maxRequests, int windowSeconds) {
        try {
            // 使用当前时间窗口作为key的一部分
            long currentWindow = System.currentTimeMillis() / (windowSeconds * 1000);
            String windowKey = key + ":" + currentWindow;
            
            Long currentCount = redisTemplate.opsForValue().increment(windowKey);
            
            if (currentCount == 1) {
                // 第一次访问，设置过期时间
                redisTemplate.expire(windowKey, windowSeconds, TimeUnit.SECONDS);
            }
            
            return currentCount <= maxRequests;
            
        } catch (Exception e) {
            log.error("分布式限流异常，key: {}", key, e);
            // 异常时允许通过，避免影响业务
            return true;
        }
    }

    /**
     * 用户级限流
     * @param userId 用户ID
     * @param action 操作类型
     * @param windowSize 时间窗口大小（秒）
     * @param maxRequests 最大请求数
     * @return 是否允许通过
     */
    public boolean userRateLimit(Long userId, String action, int windowSize, int maxRequests) {
        String key = String.format("rate_limit:user:%d:%s", userId, action);
        return slidingWindowRateLimit(key, windowSize, maxRequests);
    }

    /**
     * IP级限流
     * @param ip IP地址
     * @param action 操作类型
     * @param windowSize 时间窗口大小（秒）
     * @param maxRequests 最大请求数
     * @return 是否允许通过
     */
    public boolean ipRateLimit(String ip, String action, int windowSize, int maxRequests) {
        String key = String.format("rate_limit:ip:%s:%s", ip, action);
        return slidingWindowRateLimit(key, windowSize, maxRequests);
    }

    /**
     * 全局限流
     * @param action 操作类型
     * @param windowSize 时间窗口大小（秒）
     * @param maxRequests 最大请求数
     * @return 是否允许通过
     */
    public boolean globalRateLimit(String action, int windowSize, int maxRequests) {
        String key = String.format("rate_limit:global:%s", action);
        return slidingWindowRateLimit(key, windowSize, maxRequests);
    }

    /**
     * 抢红包专用限流
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 是否允许通过
     */
    public boolean redPacketGrabRateLimit(Long userId, Long activityId) {
        // 用户级限流：每秒最多1次
        String userKey = String.format("rate_limit:red_packet:user:%d", userId);
        if (!slidingWindowRateLimit(userKey, 1, 1)) {
            log.warn("用户抢红包频率限制，用户ID: {}", userId);
            return false;
        }
        
        // 活动级限流：每秒最多5000次
        String activityKey = String.format("rate_limit:red_packet:activity:%d", activityId);
        if (!slidingWindowRateLimit(activityKey, 1, 5000)) {
            log.warn("活动抢红包频率限制，活动ID: {}", activityId);
            return false;
        }
        
        // 全局限流：每秒最多10000次
        String globalKey = "rate_limit:red_packet:global";
        if (!slidingWindowRateLimit(globalKey, 1, 10000)) {
            log.warn("全局抢红包频率限制");
            return false;
        }
        
        return true;
    }

    /**
     * 检查限流状态
     * @param key 限流键
     * @param windowSize 时间窗口大小（秒）
     * @return 当前窗口内的请求数
     */
    public long getCurrentRequestCount(String key, int windowSize) {
        try {
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - windowSize * 1000L;
            
            return redisTemplate.opsForZSet().count(key, windowStart, currentTime);
        } catch (Exception e) {
            log.error("获取限流状态异常，key: {}", key, e);
            return 0;
        }
    }

    /**
     * 清理过期的限流数据
     * @param key 限流键
     * @param windowSize 时间窗口大小（秒）
     */
    public void cleanupExpiredData(String key, int windowSize) {
        try {
            long currentTime = System.currentTimeMillis();
            long expiredTime = currentTime - windowSize * 1000L;
            
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, expiredTime);
        } catch (Exception e) {
            log.error("清理过期限流数据异常，key: {}", key, e);
        }
    }
}
