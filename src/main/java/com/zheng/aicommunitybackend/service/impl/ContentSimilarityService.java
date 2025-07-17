package com.zheng.aicommunitybackend.service.impl;

import com.zheng.aicommunitybackend.domain.entity.HotNews;
import com.zheng.aicommunitybackend.mapper.HotNewsMapper;
import com.zheng.aicommunitybackend.util.SimHashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
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
    private StringRedisTemplate stringRedisTemplate;
    
    // Redis缓存键前缀
    private static final String CONTENT_HASH_KEY_PREFIX = "news:content_hash:";
    
    // Redis中存储所有内容哈希的集合键
    private static final String CONTENT_HASH_SET_KEY = "news:all_content_hashes";
    
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
        log.debug("检查内容是否相似，标题: {}, 哈希值: {}", news.getTitle(), contentHash);
        
        // 先从缓存中查询
        boolean foundInCache = checkSimilarityInCache(contentHash);
        if (foundInCache) {
            log.info("在Redis缓存中发现相似内容: {}", news.getTitle());
            return true;
        }
        
        // 缓存中未找到，查询数据库
        List<String> recentHashes = hotNewsMapper.selectRecentContentHashes(RECENT_HASH_LIMIT);
        log.debug("从数据库中查询到 {} 条哈希记录", recentHashes != null ? recentHashes.size() : 0);
        
        if (recentHashes == null || recentHashes.isEmpty()) {
            log.debug("数据库中没有找到内容哈希记录");
            return false;
        }
        
        for (String existingHash : recentHashes) {
            if (existingHash != null && SimHashUtil.isSimilar(contentHash, existingHash)) {
                log.info("在数据库中发现相似内容: {}, 哈希值: {}, 相似哈希: {}", 
                        news.getTitle(), contentHash, existingHash);
                
                // 不要在这里缓存，避免缓存与数据库不一致
                // 只有在真正保存到数据库后才缓存
                return true;
            }
        }
        
        log.debug("没有找到相似内容: {}", news.getTitle());
        return false;
    }
    
    /**
     * 在缓存中查询相似内容
     * @param contentHash 内容指纹
     * @return 是否找到相似内容
     */
    private boolean checkSimilarityInCache(String contentHash) {
        // 如果Redis不可用，直接返回false
        if (stringRedisTemplate == null) {
            log.warn("Redis不可用，跳过缓存相似度检查");
            return false;
        }
        
        try {
            // 从Redis集合中获取所有内容哈希
            Set<String> cachedHashes = stringRedisTemplate.opsForSet().members(CONTENT_HASH_SET_KEY);
            
            if (cachedHashes == null || cachedHashes.isEmpty()) {
                log.debug("Redis缓存中没有内容哈希记录");
                return false;
            }
            
            log.debug("从Redis缓存中获取到 {} 条哈希值进行比较", cachedHashes.size());
            
            // 逐个比较相似度
            for (String hash : cachedHashes) {
                if (hash != null && SimHashUtil.isSimilar(contentHash, hash)) {
                    log.debug("内容哈希[{}]与缓存中的哈希[{}]相似", contentHash, hash);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("查询Redis缓存出错", e);
            return false;
        }
    }
    
    /**
     * 缓存内容指纹
     * @param contentHash 内容指纹
     */
    public void cacheContentHash(String contentHash) {
        if (stringRedisTemplate == null || contentHash == null) {
            return;
        }
        
        try {
            // 1. 将哈希值单独存储
            String cacheKey = CONTENT_HASH_KEY_PREFIX + contentHash;
            stringRedisTemplate.opsForValue().set(cacheKey, "1", CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            // 2. 同时将哈希值添加到集合中，用于相似度检查
            stringRedisTemplate.opsForSet().add(CONTENT_HASH_SET_KEY, contentHash);
            // 为集合设置过期时间
            stringRedisTemplate.expire(CONTENT_HASH_SET_KEY, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            log.debug("成功将内容哈希[{}]添加到Redis缓存", contentHash);
        } catch (Exception e) {
            log.error("缓存内容指纹出错", e);
        }
    }
    
    /**
     * 清理Redis中的内容哈希缓存
     * 当数据与缓存不一致时可调用此方法
     */
    public void clearContentHashCache() {
        if (stringRedisTemplate == null) {
            return;
        }
        
        try {
            // 删除集合键
            stringRedisTemplate.delete(CONTENT_HASH_SET_KEY);
            
            // 删除所有以前缀开头的键
            Set<String> keys = stringRedisTemplate.keys(CONTENT_HASH_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
            
            log.info("已清理Redis中的内容哈希缓存");
        } catch (Exception e) {
            log.error("清理内容哈希缓存出错", e);
        }
    }
} 