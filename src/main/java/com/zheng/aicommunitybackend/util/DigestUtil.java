package com.zheng.aicommunitybackend.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 密码加密工具类
 */
public class DigestUtil {
    
    /**
     * MD5加密字符串
     *
     * @param text 明文
     * @return 加密后的字符串
     */
    public static String md5Hex(String text) {
        return DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 校验密码是否正确
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return md5Hex(rawPassword).equals(encodedPassword);
    }
} 