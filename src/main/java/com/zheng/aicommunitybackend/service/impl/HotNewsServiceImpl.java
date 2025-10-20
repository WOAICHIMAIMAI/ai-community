package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.entity.HotNews;
import com.zheng.aicommunitybackend.mapper.HotNewsMapper;
import com.zheng.aicommunitybackend.service.HotNewsService;
import com.zheng.aicommunitybackend.task.NewsSpiderTask;
import com.zheng.aicommunitybackend.utils.RedisUtils;
import com.zheng.aicommunitybackend.constant.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
* @author ZhengJJ
* @description 针对表【hot_news(热点新闻表)】的数据库操作Service实现
* @createDate 2025-07-13 12:19:41
*/
@Slf4j
@Service
public class HotNewsServiceImpl extends ServiceImpl<HotNewsMapper, HotNews>
    implements HotNewsService{

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public Page<HotNews> getHotNewsByPage(Page<HotNews> page, String category) {
        log.info("分页查询新闻：page={}, size={}, category={}", page.getCurrent(), page.getSize(), category);
        
        // 先尝试从缓存获取
        String cacheKey = CacheConstants.buildNewsPageKey(
            (int) page.getCurrent(), (int) page.getSize(), category);
        Page<HotNews> cachedResult = (Page<HotNews>) redisUtils.get(cacheKey);
        if (cachedResult != null) {
            log.info("从缓存获取新闻列表：total={}, records={}", cachedResult.getTotal(), cachedResult.getRecords().size());
            return cachedResult;
        }

        LambdaQueryWrapper<HotNews> queryWrapper = new LambdaQueryWrapper<>();

        // 只查询已发布的新闻
        queryWrapper.eq(HotNews::getStatus, 1);

        // 如果指定了分类，按分类查询
        if (StringUtils.hasText(category)) {
            queryWrapper.eq(HotNews::getCategory, category);
        }

        // 按是否置顶、是否热点、发布时间排序
        queryWrapper.orderByDesc(HotNews::getIsTop);
        queryWrapper.orderByDesc(HotNews::getIsHot);
        queryWrapper.orderByDesc(HotNews::getPublishTime);

        Page<HotNews> result = page(page, queryWrapper);
        
        log.info("查询新闻列表结果：total={}, records={}, current={}, size={}", 
            result.getTotal(), result.getRecords().size(), result.getCurrent(), result.getSize());

        // 缓存结果
        redisUtils.set(cacheKey, result, CacheConstants.DEFAULT_EXPIRE_TIME);

        return result;
    }

    @Override
    public List<HotNews> getLatestHotNews(Integer limit, String category) {
        // 先尝试从缓存获取
        String cacheKey = CacheConstants.buildHotNewsKey(limit == null ? 10 : limit);
        if (StringUtils.hasText(category)) {
            cacheKey += ":" + category;
        }
        List<HotNews> cachedResult = (List<HotNews>) redisUtils.get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        LambdaQueryWrapper<HotNews> queryWrapper = new LambdaQueryWrapper<>();

        // 只查询已发布的新闻
        queryWrapper.eq(HotNews::getStatus, 1);

        // 如果指定了分类，按分类查询
        if (StringUtils.hasText(category)) {
            queryWrapper.eq(HotNews::getCategory, category);
        }

        // 按是否热点、发布时间排序，并限制条数
        queryWrapper.orderByDesc(HotNews::getIsHot);
        queryWrapper.orderByDesc(HotNews::getPublishTime);
        queryWrapper.last("LIMIT " + (limit == null || limit <= 0 ? 10 : limit));

        List<HotNews> result = list(queryWrapper);

        // 缓存结果（热点新闻缓存时间较短）
        redisUtils.set(cacheKey, result, CacheConstants.HOT_DATA_EXPIRE_TIME);

        return result;
    }

    @Override
    public HotNews getNewsDetail(Long id) {
        // 先尝试从缓存获取
        String cacheKey = CacheConstants.buildNewsDetailKey(id);
        HotNews cachedNews = (HotNews) redisUtils.get(cacheKey);
        if (cachedNews != null) {
            // 异步更新浏览量
            updateViewCountAsync(id);
            return cachedNews;
        }

        HotNews news = getById(id);

        if (news != null && news.getStatus() == 1) {
            // 更新浏览次数
            news.setViewCount(news.getViewCount() + 1);
            updateById(news);

            // 缓存新闻详情
            redisUtils.set(cacheKey, news, CacheConstants.DETAIL_EXPIRE_TIME);
        }

        return news;
    }

    @Override
    public int triggerCrawlFinanceNews() {
        // 记录抓取前数据库中的新闻数量
        long beforeCount = count();
        
        // 手动触发爬取任务 - 使用ApplicationContext获取NewsSpiderTask，避免循环依赖
        NewsSpiderTask newsSpiderTask = applicationContext.getBean(NewsSpiderTask.class);
        newsSpiderTask.crawlAllFinanceNews();
        
        // 计算新增数量并返回
        long afterCount = count();
        int addedCount = (int)(afterCount - beforeCount);

        // 如果有新增新闻，清空相关缓存
        if (addedCount > 0) {
            clearNewsCache();
        }

        return addedCount;
    }

    /**
     * 清空新闻相关缓存
     */
    private void clearNewsCache() {
        // 清空新闻列表缓存
        redisUtils.deleteByPattern(CacheConstants.NEWS_PAGE_PREFIX + "*");
        // 清空热点新闻缓存
        redisUtils.deleteByPattern(CacheConstants.HOT_NEWS_PREFIX + "*");
        // 清空新闻详情缓存
        redisUtils.deleteByPattern(CacheConstants.NEWS_DETAIL_PREFIX + "*");
    }

    /**
     * 异步更新浏览量
     */
    private void updateViewCountAsync(Long newsId) {
        // 这里可以使用异步方式更新浏览量，避免影响查询性能
        // 简单实现：直接更新数据库
        HotNews news = getById(newsId);
        if (news != null) {
            news.setViewCount(news.getViewCount() + 1);
            updateById(news);
        }
    }
}




