package com.zheng.aicommunitybackend.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class IpUtil {
    public static String getClientIp(HttpServletRequest request) {
        // 1. 尝试从反向代理头获取真实IP（优先级由高到低）
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                // 多层代理时取第一个非unknown的IP
                if (ip.contains(",")) {
                    ip = ip.split(",")[0];
                }
                return ip;
            }
        }

        // 2. 没有代理时直接获取远程地址
        return request.getRemoteAddr();
    }
}