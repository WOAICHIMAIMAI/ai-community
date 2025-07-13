package com.zheng.aicommunitybackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.aicommunitybackend.common.Result;
import com.zheng.aicommunitybackend.domain.entity.HotNews;
import com.zheng.aicommunitybackend.mapper.HotNewsMapper;
import com.zheng.aicommunitybackend.task.NewsSpiderTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员热点新闻控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/news")
public class AdminHotNewsController {

    @Autowired
    private NewsSpiderTask newsSpiderTask;
    
    @Autowired
    private HotNewsMapper hotNewsMapper;

    /**
     * 手动触发爬虫任务
     * @return 爬取结果
     */
    @PostMapping("/crawl")
    public Result<Map<String, Object>> crawlNews() {
        log.info("手动触发新闻爬取任务");
        int addedCount = newsSpiderTask.crawlAndCleanAllNews();
        
        Map<String, Object> result = new HashMap<>();
        result.put("addedCount", addedCount);
        result.put("totalCount", hotNewsMapper.selectCount(null));
        
        return Result.success("新闻爬取任务执行完成", result);
    }
    
    /**
     * 清理无效新闻记录
     * @return 清理结果
     */
    @PostMapping("/clean")
    public Result<Map<String, Object>> cleanInvalidNews() {
        log.info("手动触发无效新闻清理任务");
        int cleanedCount = newsSpiderTask.cleanInvalidNewsRecords();
        
        Map<String, Object> result = new HashMap<>();
        result.put("cleanedCount", cleanedCount);
        result.put("remainingCount", hotNewsMapper.selectCount(null));
        
        return Result.success("无效新闻清理任务执行完成", result);
    }
    
    /**
     * 分页查询热点新闻
     * @param page 页码
     * @param pageSize 每页大小
     * @param category 分类
     * @param isHot 是否热点
     * @param status 状态
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<Page<HotNews>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer isHot,
            @RequestParam(required = false) Integer status) {
        
        log.info("分页查询热点新闻：page={}, pageSize={}, category={}, isHot={}, status={}", 
                page, pageSize, category, isHot, status);
        
        // 构建查询条件
        LambdaQueryWrapper<HotNews> queryWrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            queryWrapper.eq(HotNews::getCategory, category);
        }
        if (isHot != null) {
            queryWrapper.eq(HotNews::getIsHot, isHot);
        }
        if (status != null) {
            queryWrapper.eq(HotNews::getStatus, status);
        }
        
        // 按发布时间降序排序
        queryWrapper.orderByDesc(HotNews::getPublishTime);
        
        // 分页查询
        Page<HotNews> pageResult = hotNewsMapper.selectPage(new Page<>(page, pageSize), queryWrapper);
        
        return Result.success(pageResult);
    }
    
    /**
     * 更新热点新闻状态
     * @param id 新闻ID
     * @param status 状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("更新热点新闻状态：id={}, status={}", id, status);
        
        HotNews news = hotNewsMapper.selectById(id);
        if (news == null) {
            return Result.error("新闻不存在");
        }
        
        news.setStatus(status);
        hotNewsMapper.updateById(news);
        
        return Result.success("更新成功");
    }
    
    /**
     * 更新热点新闻是否热点
     * @param id 新闻ID
     * @param isHot 是否热点
     * @return 更新结果
     */
    @PutMapping("/{id}/hot")
    public Result<String> updateHot(@PathVariable Long id, @RequestParam Integer isHot) {
        log.info("更新热点新闻是否热点：id={}, isHot={}", id, isHot);
        
        HotNews news = hotNewsMapper.selectById(id);
        if (news == null) {
            return Result.error("新闻不存在");
        }
        
        news.setIsHot(isHot);
        hotNewsMapper.updateById(news);
        
        return Result.success("更新成功");
    }
    
    /**
     * 更新热点新闻是否置顶
     * @param id 新闻ID
     * @param isTop 是否置顶
     * @return 更新结果
     */
    @PutMapping("/{id}/top")
    public Result<String> updateTop(@PathVariable Long id, @RequestParam Integer isTop) {
        log.info("更新热点新闻是否置顶：id={}, isTop={}", id, isTop);
        
        HotNews news = hotNewsMapper.selectById(id);
        if (news == null) {
            return Result.error("新闻不存在");
        }
        
        news.setIsTop(isTop);
        hotNewsMapper.updateById(news);
        
        return Result.success("更新成功");
    }
    
    /**
     * 删除热点新闻
     * @param id 新闻ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除热点新闻：id={}", id);
        
        HotNews news = hotNewsMapper.selectById(id);
        if (news == null) {
            return Result.error("新闻不存在");
        }
        
        hotNewsMapper.deleteById(id);
        
        return Result.success("删除成功");
    }
} 