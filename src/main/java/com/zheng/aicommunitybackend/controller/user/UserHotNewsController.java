package com.zheng.aicommunitybackend.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.aicommunitybackend.common.Result;
import com.zheng.aicommunitybackend.domain.entity.HotNews;
import com.zheng.aicommunitybackend.service.HotNewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端热点新闻控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/news")
public class UserHotNewsController {

    @Autowired
    private HotNewsService hotNewsService;

    /**
     * 分页查询热点新闻
     * @param page 页码
     * @param pageSize 每页大小
     * @param category 分类
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<Page<HotNews>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category) {
        
        log.info("用户分页查询热点新闻：page={}, pageSize={}, category={}",
                page, pageSize, category);

        // 使用Service层方法，自动应用缓存
        Page<HotNews> pageResult = hotNewsService.getHotNewsByPage(new Page<>(page, pageSize), category);

        return Result.success(pageResult);
    }
    
    /**
     * 查询热点新闻详情
     * @param id 新闻ID
     * @return 新闻详情
     */
    @GetMapping("/{id}")
    public Result<HotNews> getById(@PathVariable Long id) {
        log.info("查询热点新闻详情：id={}", id);

        // 使用Service层方法，自动应用缓存
        HotNews news = hotNewsService.getNewsDetail(id);
        if (news == null) {
            return Result.error("新闻不存在");
        }

        return Result.success(news);
    }
    
    /**
     * 查询热点新闻列表
     * @param limit 限制数量
     * @return 热点新闻列表
     */
    @GetMapping("/hot")
    public Result<List<HotNews>> getHotNews(@RequestParam(defaultValue = "5") Integer limit) {
        log.info("查询热点新闻列表：limit={}", limit);

        // 使用Service层方法，自动应用缓存
        List<HotNews> hotNewsList = hotNewsService.getLatestHotNews(limit, null);

        return Result.success(hotNewsList);
    }
} 