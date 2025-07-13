package com.zheng.aicommunitybackend.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.zheng.aicommunitybackend.domain.entity.HotNews;
import com.zheng.aicommunitybackend.mapper.HotNewsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 布隆过滤器配置
 * 用于初始化布隆过滤器并加载数据库中已有的URL
 */
@Slf4j
@Configuration
public class BloomFilterConfig {

    @Autowired
    private HotNewsMapper hotNewsMapper;

    /**
     * 预计插入的元素数量
     */
    private static final int EXPECTED_INSERTIONS = 100000;

    /**
     * 可接受的误判率
     */
    private static final double FALSE_POSITIVE_PROBABILITY = 0.01;

    /**
     * 创建布隆过滤器Bean
     * @return 布隆过滤器实例
     */
    @Bean
    public BloomFilter<String> urlBloomFilter() {
        // 创建布隆过滤器，预计存储100000个URL，误判率为1%
        BloomFilter<String> bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                EXPECTED_INSERTIONS,
                FALSE_POSITIVE_PROBABILITY);
        
        // 加载数据库中已有的URL
        loadExistingUrls(bloomFilter);
        
        return bloomFilter;
    }
    
    /**
     * 从数据库加载已有的URL到布隆过滤器
     * @param bloomFilter 布隆过滤器实例
     */
    private void loadExistingUrls(BloomFilter<String> bloomFilter) {
        try {
            log.info("开始加载数据库中已有的URL到布隆过滤器...");
            
            // 只查询source_url字段，减少数据传输量
            LambdaQueryWrapper<HotNews> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(HotNews::getSourceUrl);
            
            List<HotNews> newsList = hotNewsMapper.selectList(queryWrapper);
            
            int count = 0;
            for (HotNews news : newsList) {
                String url = news.getSourceUrl();
                if (url != null && !url.isEmpty()) {
                    bloomFilter.put(url);
                    count++;
                }
            }
            
            log.info("成功加载{}个URL到布隆过滤器", count);
        } catch (Exception e) {
            log.error("加载URL到布隆过滤器时出错: {}", e.getMessage(), e);
        }
    }
} 