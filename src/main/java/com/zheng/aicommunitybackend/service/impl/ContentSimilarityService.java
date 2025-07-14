package com.zheng.aicommunitybackend.service.impl;

import com.zheng.aicommunitybackend.domain.entity.HotNews;
import com.zheng.aicommunitybackend.mapper.HotNewsMapper;
import com.zheng.aicommunitybackend.util.SimHashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 内容相似度检测服务
 */
@Slf4j
@Service
public class ContentSimilarityService {

    @Autowired
    private HotNewsMapper hotNewsMapper;
    
    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;
    
    // Redis缓存键前缀
    private static final String CONTENT_HASH_KEY_PREFIX = "news:content_hash:";
    
    // 缓存过期时间（小时）
    private static final int CACHE_EXPIRE_HOURS = 24;
    
    // 最近指纹查询数量限制
    private static final int RECENT_HASH_LIMIT = 100;
    
    /**
     * 为新闻生成内容指纹
     * @param news 新闻对象
     */
    public void generateContentHash(HotNews news) {
        if (news == null || news.getTitle() == null || news.getContent() == null) {
            return;
        }
        
        String contentHash = SimHashUtil.generateNewsFingerprint(news.getTitle(), news.getContent());
        news.setContentHash(contentHash);
        
        // 缓存指纹
        cacheContentHash(contentHash);
    }
    
    /**
     * 检查新闻是否与已有内容相似
     * @param news 新闻对象
     * @return 是否相似
     */
    public boolean isSimilarToExisting(HotNews news) {
        if (news == null || news.getContentHash() == null) {
            return false;
        }
        
        String contentHash = news.getContentHash();
        
        // 先从缓存中查询
        boolean foundInCache = checkSimilarityInCache(contentHash);
        if (foundInCache) {
            log.info("在缓存中发现相似内容: {}", news.getTitle());
            return true;
        }
        
        // 缓存中未找到，查询数据库
        List<String> recentHashes = hotNewsMapper.selectRecentContentHashes(RECENT_HASH_LIMIT);
        
        for (String existingHash : recentHashes) {
            if (SimHashUtil.isSimilar(contentHash, existingHash)) {
                log.info("在数据库中发现相似内容: {}", news.getTitle());
                // 将相似的指纹对添加到缓存
                cacheContentHash(contentHash);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 在缓存中查询相似内容
     * @param contentHash 内容指纹
     * @return 是否找到相似内容
     */
    private boolean checkSimilarityInCache(String contentHash) {
        // 如果Redis不可用，直接返回false
        if (redisTemplate == null) {
            return false;
        }
        
        try {
            String cacheKey = CONTENT_HASH_KEY_PREFIX + contentHash;
            Boolean exists = redisTemplate.hasKey(cacheKey);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("查询缓存出错", e);
            return false;
        }
    }
    
    /**
     * 缓存内容指纹
     * @param contentHash 内容指纹
     */
    public void cacheContentHash(String contentHash) {
        if (redisTemplate == null || contentHash == null) {
            return;
        }
        
        try {
            String cacheKey = CONTENT_HASH_KEY_PREFIX + contentHash;
            redisTemplate.opsForValue().set(cacheKey, "1", CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("缓存内容指纹出错", e);
        }
    }
} 