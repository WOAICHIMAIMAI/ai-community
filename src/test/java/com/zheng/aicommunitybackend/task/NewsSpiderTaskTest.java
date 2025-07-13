package com.zheng.aicommunitybackend.task;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class NewsSpiderTaskTest {

    @Resource
    private NewsSpiderTask newsSpiderTask;

    @Test
    void crawlFinanceNewsFromWallStreetCn() {
        newsSpiderTask.crawlFinanceNewsFromWallStreetCn();
    }

    @Test
    void crawlFinanceNewsFromCaixin() {
        newsSpiderTask.crawlFinanceNewsFromCaixin();
    }

    @Test
    void crawlAllFinanceNews() {
    }
}