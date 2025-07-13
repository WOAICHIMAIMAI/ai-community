package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.entity.HotNews;
import com.zheng.aicommunitybackend.mapper.HotNewsMapper;
import com.zheng.aicommunitybackend.service.HotNewsService;
import com.zheng.aicommunitybackend.task.NewsSpiderTask;
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
@Service
public class HotNewsServiceImpl extends ServiceImpl<HotNewsMapper, HotNews>
    implements HotNewsService{

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public Page<HotNews> getHotNewsByPage(Page<HotNews> page, String category) {
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
        
        return page(page, queryWrapper);
    }

    @Override
    public List<HotNews> getLatestHotNews(Integer limit, String category) {
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
        
        return list(queryWrapper);
    }

    @Override
    public HotNews getNewsDetail(Long id) {
        HotNews news = getById(id);
        
        if (news != null && news.getStatus() == 1) {
            // 更新浏览次数
            news.setViewCount(news.getViewCount() + 1);
            updateById(news);
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
        return (int)(afterCount - beforeCount);
    }
}




