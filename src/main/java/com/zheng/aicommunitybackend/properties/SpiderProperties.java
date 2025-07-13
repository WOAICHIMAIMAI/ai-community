package com.zheng.aicommunitybackend.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 新闻爬虫配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "spider")
public class SpiderProperties {

    /**
     * 是否启用爬虫
     */
    private Boolean enabled = true;

    /**
     * 用户代理
     */
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    /**
     * 连接超时时间(毫秒)
     */
    private Integer timeout = 10000;

    /**
     * 财经新闻爬虫配置
     */
    private FinanceNews financeNews = new FinanceNews();

    /**
     * 财经新闻配置
     */
    @Data
    public static class FinanceNews {
        /**
         * 是否启用财经新闻爬虫
         */
        private Boolean enabled = true;

        /**
         * 华尔街见闻URL
         */
        private String wallStreetCnUrl = "https://wallstreetcn.com/news/global";

        /**
         * 财新网URL
         */
        private String caixinUrl = "https://www.caixin.com/finance/";
    }
} 