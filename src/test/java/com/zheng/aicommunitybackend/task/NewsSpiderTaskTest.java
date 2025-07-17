package com.zheng.aicommunitybackend.task;

import com.zheng.aicommunitybackend.service.impl.ContentSimilarityService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NewsSpiderTaskTest {

    @Resource
    private NewsSpiderTask newsSpiderTask;
    
    @Resource
    private ContentSimilarityService contentSimilarityService;


    @Test
    void crawlFinanceNewsFromWallStreetCn() {
        newsSpiderTask.crawlFinanceNewsFromWallStreetCn();
    }

    @Test
    void crawlFinanceNewsFromCaixin() {
        newsSpiderTask.crawlFinanceNewsFromCaixin();
    }
    
    @Test
    void crawlFinanceNewsFromSina() {
        long start = System.currentTimeMillis();
        newsSpiderTask.crawlFinanceNewsFromSina();
        long end = System.currentTimeMillis();
        System.out.println("爬取新浪财经新闻耗时: " + (end - start) + "ms");
    }

    @Test
    void crawlAllFinanceNews() {
        newsSpiderTask.crawlAllFinanceNews();
    }
    
    @Test
    void resetAndCrawlSinaNews() {
        // 先清理内容哈希缓存
        contentSimilarityService.clearContentHashCache();
        
        // 然后重新爬取新浪财经新闻
        int count = newsSpiderTask.resetAndCrawlSinaNews();
        System.out.println("重新爬取新浪财经新闻数量: " + count);
    }
}