package com.zheng.aicommunitybackend.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zheng.aicommunitybackend.common.Result;
import com.zheng.aicommunitybackend.util.JwtUtil;
import com.zheng.aicommunitybackend.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(JwtAuthInterceptor.class);

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    public JwtAuthInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的令牌
        String jwtToken = getJwtFromRequest(request);
        
        if (!StringUtils.hasText(jwtToken)) {
            handleAuthError(response, "未提供认证令牌");
            return false;
        }

        if (!jwtUtil.validateToken(jwtToken)) {
            handleAuthError(response, "令牌已过期或无效");
            return false;
        }

        // 获取用户信息
        String username = jwtUtil.getUsernameFromToken(jwtToken);
        Long userId = jwtUtil.getUserIdFromToken(jwtToken);
        Integer role = jwtUtil.getRoleFromToken(jwtToken);
        
        if (username == null || userId == null || role == null) {
            handleAuthError(response, "令牌中的用户信息无效");
            return false;
        }

        // 将用户信息存入ThreadLocal
        UserContext.setUsername(username);
        UserContext.setUserId(userId);
        UserContext.setUserRole(role);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清除ThreadLocal，防止内存泄漏
        UserContext.clear();
    }

    /**
     * 处理认证错误
     */
    private void handleAuthError(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, message)));
    }

    /**
     * 从请求中提取JWT令牌
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(tokenHeader);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenPrefix)) {
            return bearerToken.substring(tokenPrefix.length()).trim();
        }
        
        return bearerToken;
    }
} 