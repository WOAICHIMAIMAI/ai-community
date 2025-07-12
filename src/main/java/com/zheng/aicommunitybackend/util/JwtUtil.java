package com.zheng.aicommunitybackend.util;



import com.zheng.aicommunitybackend.domain.entity.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类，用于生成、解析和验证JWT令牌
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token 令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return Long.parseLong(claims.get("userId").toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从令牌中获取用户角色
     *
     * @param token 令牌
     * @return 用户角色
     */
    public Integer getRoleFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return Integer.parseInt(claims.get("role").toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从令牌中获取过期日期
     *
     * @param token 令牌
     * @return 过期日期
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从令牌中获取所有声明
     *
     * @param token 令牌
     * @return 所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取签名密钥
     *
     * @return 签名密钥
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 检查令牌是否已过期
     *
     * @param token 令牌
     * @return 是否已过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 为用户生成令牌
     *
     * @param user 用户
     * @return 令牌
     */
    public String generateToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        return doGenerateToken(claims, user.getUsername());
    }

    /**
     * 生成令牌的核心方法
     *
     * @param claims  声明
     * @param subject 主题（用户名）
     * @return 令牌
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        final Date createdDate = new Date();
        final Date expirationDate = calculateExpirationDate(createdDate);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 计算过期日期
     *
     * @param createdDate 创建日期
     * @return 过期日期
     */
    private Date calculateExpirationDate(Date createdDate) {
        return new Date(createdDate.getTime() + expiration);
    }

    /**
     * 验证令牌
     *
     * @param token 令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
} 