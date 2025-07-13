package com.zheng.aicommunitybackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.aicommunitybackend.domain.entity.HotNews;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.service.HotNewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 热点新闻控制器
 */
@RestController
@RequestMapping("/hotnews")
@Tag(name = "热点新闻接口", description = "提供热点新闻相关的接口")
public class HotNewsController {

    @Autowired
    private HotNewsService hotNewsService;

    /**
     * 分页获取热点新闻
     * @param current 当前页
     * @param size 每页大小
     * @param category 新闻分类
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取热点新闻")
    public Result<Page<HotNews>> getHotNewsByPage(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "新闻分类") @RequestParam(required = false) String category) {
        
        Page<HotNews> page = new Page<>(current, size);
        Page<HotNews> result = hotNewsService.getHotNewsByPage(page, category);
        return Result.success(result);
    }

    /**
     * 获取最新的热点新闻
     * @param limit 条数限制
     * @param category 新闻分类
     * @return 新闻列表
     */
    @GetMapping("/latest")
    @Operation(summary = "获取最新的热点新闻")
    public Result<List<HotNews>> getLatestHotNews(
            @Parameter(description = "条数限制") @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "新闻分类") @RequestParam(required = false) String category) {
        
        List<HotNews> newsList = hotNewsService.getLatestHotNews(limit, category);
        return Result.success(newsList);
    }

    /**
     * 获取新闻详情
     * @param id 新闻ID
     * @return 新闻详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取新闻详情")
    public Result<HotNews> getNewsDetail(@PathVariable Long id) {
        HotNews news = hotNewsService.getNewsDetail(id);
        if (news == null) {
            return Result.error("新闻不存在或已下线");
        }
        return Result.success(news);
    }
} 