package com.zheng.aicommunitybackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.entity.HotNews;

import java.util.List;

/**
* @author ZhengJJ
* @description 针对表【hot_news(热点新闻表)】的数据库操作Service
* @createDate 2025-07-13 12:19:41
*/
public interface HotNewsService extends IService<HotNews> {

    /**
     * 分页获取热点新闻列表
     * @param page 分页参数
     * @param category 新闻分类
     * @return 分页结果
     */
    Page<HotNews> getHotNewsByPage(Page<HotNews> page, String category);

    /**
     * 获取最新的热点新闻
     * @param limit 条数限制
     * @param category 新闻分类，可为null
     * @return 热点新闻列表
     */
    List<HotNews> getLatestHotNews(Integer limit, String category);

    /**
     * 根据ID获取新闻详情
     * @param id 新闻ID
     * @return 新闻详情
     */
    HotNews getNewsDetail(Long id);

    /**
     * 手动触发爬取财经新闻
     * @return 成功抓取的新闻数量
     */
    int triggerCrawlFinanceNews();
}
