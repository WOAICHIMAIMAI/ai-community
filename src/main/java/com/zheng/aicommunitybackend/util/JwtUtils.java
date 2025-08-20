package com.zheng.aicommunitybackend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * JWT静态工具类
 * 提供静态方法访问JWT功能
 */
@Component
@Slf4j
public class JwtUtils {

    @Autowired
    private JwtUtil jwtUtil;

    private static JwtUtil staticJwtUtil;

    @PostConstruct
    public void init() {
        staticJwtUtil = jwtUtil;
    }

    /**
     * 从令牌中获取用户ID
     * @param token JWT令牌（可能包含Bearer前缀）
     * @return 用户ID
     */
    public static Long getUserId(String token) {
        if (token == null) {
            return null;
        }
        
        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            return staticJwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.debug("解析JWT令牌失败", e);
            return null;
        }
    }

    /**
     * 从令牌中获取用户名
     * @param token JWT令牌（可能包含Bearer前缀）
     * @return 用户名
     */
    public static String getUsername(String token) {
        if (token == null) {
            return null;
        }
        
        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            return staticJwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            log.debug("解析JWT令牌失败", e);
            return null;
        }
    }

    /**
     * 从令牌中获取用户角色
     * @param token JWT令牌（可能包含Bearer前缀）
     * @return 用户角色
     */
    public static Integer getRole(String token) {
        if (token == null) {
            return null;
        }
        
        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            return staticJwtUtil.getRoleFromToken(token);
        } catch (Exception e) {
            log.debug("解析JWT令牌失败", e);
            return null;
        }
    }

    /**
     * 验证令牌是否有效
     * @param token JWT令牌（可能包含Bearer前缀）
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        if (token == null) {
            return false;
        }
        
        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            return staticJwtUtil.validateToken(token);
        } catch (Exception e) {
            log.debug("验证JWT令牌失败", e);
            return false;
        }
    }
}
