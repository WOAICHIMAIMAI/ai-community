package com.zheng.aicommunitybackend.config;

import com.google.common.hash.BloomFilter;
import com.zheng.aicommunitybackend.domain.entity.HotNews;
import com.zheng.aicommunitybackend.mapper.HotNewsMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 布隆过滤器配置测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class BloomFilterConfigTest {

    @Autowired
    private BloomFilter<String> urlBloomFilter;

    @Autowired
    private BloomFilterConfig bloomFilterConfig;

    @Autowired
    private HotNewsMapper hotNewsMapper;

    @Test
    public void testBloomFilterInitialization() {
        // 测试布隆过滤器是否正确初始化
        assertNotNull(urlBloomFilter);
        
        // 测试基本功能
        String testUrl = "https://test.example.com/news/123";
        
        // 初始状态应该不包含测试URL
        assertFalse(urlBloomFilter.mightContain(testUrl));
        
        // 添加URL后应该包含
        urlBloomFilter.put(testUrl);
        assertTrue(urlBloomFilter.mightContain(testUrl));
    }

    @Test
    public void testBloomFilterStats() {
        // 测试统计信息获取
        BloomFilterConfig.BloomFilterStats stats = bloomFilterConfig.getStats();
        
        assertNotNull(stats);
        assertTrue(stats.getExpectedInsertions() > 0);
        assertTrue(stats.getFalsePositiveProbability() > 0);
        assertTrue(stats.getFalsePositiveProbability() < 1);
        assertNotNull(stats.getLastRebuildTime());
    }

    @Test
    public void testBloomFilterRebuild() {
        // 测试手动重建功能
        long initialCount = bloomFilterConfig.getStats().getLoadedUrlCount();
        
        // 执行重建
        long rebuildCount = bloomFilterConfig.rebuildBloomFilter();
        
        // 验证重建后的统计信息
        BloomFilterConfig.BloomFilterStats newStats = bloomFilterConfig.getStats();
        assertEquals(rebuildCount, newStats.getLoadedUrlCount());
    }

    @Test
    public void testUrlDeduplication() {
        // 测试URL去重功能
        String[] testUrls = {
            "https://example1.com/news/1",
            "https://example2.com/news/2", 
            "https://example3.com/news/3"
        };
        
        // 添加测试URL
        for (String url : testUrls) {
            urlBloomFilter.put(url);
        }
        
        // 验证所有URL都能被检测到
        for (String url : testUrls) {
            assertTrue(urlBloomFilter.mightContain(url), 
                "URL should be detected: " + url);
        }
        
        // 验证不存在的URL
        assertFalse(urlBloomFilter.mightContain("https://nonexistent.com/news/999"));
    }

    @Test
    public void testUsageRatioCalculation() {
        // 测试使用率计算
        BloomFilterConfig.BloomFilterStats stats = bloomFilterConfig.getStats();

        double usageRatio = stats.getUsageRatio();
        assertTrue(usageRatio >= 0.0);
        assertTrue(usageRatio <= 1.0);

        // 使用率应该等于 loadedUrlCount / expectedInsertions
        double expectedRatio = (double) stats.getLoadedUrlCount() / stats.getExpectedInsertions();
        assertEquals(expectedRatio, usageRatio, 0.001);
    }

    @Test
    public void testNoCircularDependency() {
        // 测试修复循环调用问题
        try {
            // 这些调用不应该导致StackOverflowError
            BloomFilterConfig.BloomFilterStats stats = bloomFilterConfig.getStats();
            assertNotNull(stats);

            String recommendation = bloomFilterConfig.getAlertRecommendation();
            assertNotNull(recommendation);

            BloomFilterConfig.AlertLevel alertLevel = bloomFilterConfig.getCurrentAlertLevel();
            assertNotNull(alertLevel);

            // 多次调用应该都正常
            for (int i = 0; i < 10; i++) {
                stats = bloomFilterConfig.getStats();
                recommendation = bloomFilterConfig.getAlertRecommendation();
                assertNotNull(stats);
                assertNotNull(recommendation);
            }

        } catch (StackOverflowError e) {
            fail("应该不会出现StackOverflowError: " + e.getMessage());
        }
    }
}
