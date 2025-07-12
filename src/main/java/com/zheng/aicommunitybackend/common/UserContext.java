package com.zheng.aicommunitybackend.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户上下文工具类，基于ThreadLocal实现
 */
public class UserContext {

    private static final Logger log = LoggerFactory.getLogger(UserContext.class);

    /**
     * 使用ThreadLocal存储用户上下文信息
     */
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        Map<String, Object> map = getThreadLocalMap();
        map.put(Constants.CURRENT_USER_ID, userId);
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        Object value = getThreadLocalMap().get(Constants.CURRENT_USER_ID);
        return value == null ? null : (Long) value;
    }

    /**
     * 设置用户名
     *
     * @param username 用户名
     */
    public static void setUsername(String username) {
        Map<String, Object> map = getThreadLocalMap();
        map.put(Constants.CURRENT_USERNAME, username);
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public static String getUsername() {
        Object value = getThreadLocalMap().get(Constants.CURRENT_USERNAME);
        return value == null ? null : (String) value;
    }

    /**
     * 设置用户角色
     *
     * @param role 用户角色
     */
    public static void setUserRole(Integer role) {
        Map<String, Object> map = getThreadLocalMap();
        map.put(Constants.CURRENT_USER_ROLE, role);
    }

    /**
     * 获取当前用户角色
     *
     * @return 用户角色
     */
    public static Integer getUserRole() {
        Object value = getThreadLocalMap().get(Constants.CURRENT_USER_ROLE);
        return value == null ? null : (Integer) value;
    }

    /**
     * 获取ThreadLocal中的Map，如果不存在则创建
     *
     * @return ThreadLocal中存储的Map
     */
    private static Map<String, Object> getThreadLocalMap() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new HashMap<>(8);
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    /**
     * 清除ThreadLocal中的所有数据，防止内存泄漏
     */
    public static void clear() {
        THREAD_LOCAL.remove();
    }
} 