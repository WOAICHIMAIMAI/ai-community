package com.zheng.aicommunitybackend.aspect;

import com.zheng.aicommunitybackend.annotation.RateLimit;
import com.zheng.aicommunitybackend.annotation.RateLimits;
import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.component.RateLimitComponent;
import com.zheng.aicommunitybackend.component.SimpleRateLimitComponent;
import com.zheng.aicommunitybackend.domain.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 限流切面
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {
    
    @Autowired
    private RateLimitComponent rateLimitComponent;

    @Autowired
    private SimpleRateLimitComponent simpleRateLimitComponent;
    
    @Around("@annotation(com.zheng.aicommunitybackend.annotation.RateLimit) || @annotation(com.zheng.aicommunitybackend.annotation.RateLimits)")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        // 获取方法上的所有限流注解
        Method method = ((org.aspectj.lang.reflect.MethodSignature) point.getSignature()).getMethod();
        RateLimit[] rateLimits = method.getAnnotationsByType(RateLimit.class);

        // 检查每个限流注解
        for (RateLimit rateLimit : rateLimits) {
            String key = buildKey(rateLimit, point);
            log.info("限流检查开始: key={}, limitType={}, windowSize={}, maxRequests={}",
                    key, rateLimit.limitType(), rateLimit.windowSize(), rateLimit.maxRequests());
            boolean allowed = false;

            try {
                // 根据算法选择限流器
                switch (rateLimit.algorithm()) {
                    case SLIDING_WINDOW:
                        allowed = rateLimitComponent.slidingWindowRateLimit(
                            key, rateLimit.windowSize(), rateLimit.maxRequests());
                        break;
                    case TOKEN_BUCKET:
                        // 令牌桶算法，容量为最大请求数，补充速率为每秒最大请求数/窗口大小
                        int refillRate = Math.max(1, rateLimit.maxRequests() / rateLimit.windowSize());
                        allowed = rateLimitComponent.tokenBucketRateLimit(
                            key, rateLimit.maxRequests(), refillRate);
                        break;
                    case FIXED_WINDOW:
                        allowed = rateLimitComponent.fixedWindowRateLimit(
                            key, rateLimit.windowSize(), rateLimit.maxRequests());
                        break;
                }
            } catch (Exception e) {
                log.error("限流组件异常，使用简化版限流，key: {}", key, e);
                // 如果原限流组件出错，使用简化版限流
                allowed = simpleRateLimitComponent.simpleRateLimit(
                    key, rateLimit.windowSize(), rateLimit.maxRequests());
            }

            log.debug("限流检查结果: key={}, allowed={}, algorithm={}, windowSize={}, maxRequests={}",
                    key, allowed, rateLimit.algorithm(), rateLimit.windowSize(), rateLimit.maxRequests());

            if (!allowed) {
                log.warn("Rate limit exceeded for key: {}, algorithm: {}, windowSize: {}, maxRequests: {}",
                        key, rateLimit.algorithm(), rateLimit.windowSize(), rateLimit.maxRequests());

                // 返回限流错误
                return Result.error(429, rateLimit.message());
            }
        }

        return point.proceed();
    }
    
    /**
     * 构建限流key
     */
    private String buildKey(RateLimit rateLimit, ProceedingJoinPoint point) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("rate_limit:");
        
        if (StringUtils.hasText(rateLimit.key())) {
            keyBuilder.append(rateLimit.key()).append(":");
        } else {
            // 使用方法名作为默认key
            keyBuilder.append(point.getSignature().getName()).append(":");
        }
        
        switch (rateLimit.limitType()) {
            case USER:
                Long userId = getCurrentUserId();
                String userKey = userId != null ? userId.toString() : "anonymous";
                keyBuilder.append("user:").append(userKey);
                log.debug("构建用户限流key: {}, userId: {}", keyBuilder.toString(), userId);
                break;
            case IP:
                String ip = getClientIP();
                keyBuilder.append("ip:").append(ip);
                break;
            case GLOBAL:
                keyBuilder.append("global");
                break;
        }
        
        return keyBuilder.toString();
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            // 优先从UserContext获取，这与Controller中的逻辑保持一致
            Long userId = UserContext.getUserId();
            if (userId != null) {
                return userId;
            }

            // 如果UserContext中没有，说明可能是未登录用户或token有问题
            log.debug("UserContext中未找到用户ID，可能是未登录用户");
            return null;
        } catch (Exception e) {
            log.debug("获取用户ID失败", e);
            return null;
        }
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIP() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return "unknown";
            }
            
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            
            // 处理多个IP的情况，取第一个
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            
            return ip;
        } catch (Exception e) {
            log.debug("获取客户端IP失败", e);
            return "unknown";
        }
    }
    
    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
