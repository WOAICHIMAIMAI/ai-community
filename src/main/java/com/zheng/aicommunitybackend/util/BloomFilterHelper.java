package com.zheng.aicommunitybackend.util;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 布隆过滤器工具类
 * 用于高效检查URL是否已存在，减少数据库查询
 */
@Component
public class BloomFilterHelper {

    /**
     * 预计插入的元素数量
     */
    private static final int EXPECTED_INSERTIONS = 10000;

    /**
     * 可接受的误判率
     */
    private static final double FALSE_POSITIVE_PROBABILITY = 0.01;

    /**
     * 布隆过滤器实例
     */
    private BloomFilter<String> bloomFilter;

    /**
     * 初始化布隆过滤器
     */
    @PostConstruct
    public void init() {
        // 创建布隆过滤器，预计存储10000个URL，误判率为1%
        bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                EXPECTED_INSERTIONS,
                FALSE_POSITIVE_PROBABILITY);
    }

    /**
     * 将URL添加到布隆过滤器
     * @param url URL字符串
     */
    public void add(String url) {
        if (url != null && !url.isEmpty()) {
            bloomFilter.put(url);
        }
    }

    /**
     * 检查URL是否可能存在
     * 注意：布隆过滤器可能会有误判，返回true不一定表示元素存在
     * 但返回false一定表示元素不存在
     * @param url URL字符串
     * @return 如果可能存在返回true，一定不存在返回false
     */
    public boolean mightContain(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return bloomFilter.mightContain(url);
    }
} 