package com.zheng.aicommunitybackend.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.aicommunitybackend.common.Result;
import com.zheng.aicommunitybackend.domain.entity.HotNews;
import com.zheng.aicommunitybackend.mapper.HotNewsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端热点新闻控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/news")
public class UserHotNewsController {

    @Autowired
    private HotNewsMapper hotNewsMapper;

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
        
        // 构建查询条件
        LambdaQueryWrapper<HotNews> queryWrapper = new LambdaQueryWrapper<>();
        // 只查询已发布的新闻
        queryWrapper.eq(HotNews::getStatus, 1);
        
        if (category != null && !category.isEmpty()) {
            queryWrapper.eq(HotNews::getCategory, category);
        }
        
        // 先按置顶排序，再按发布时间降序排序
        queryWrapper.orderByDesc(HotNews::getIsTop)
                   .orderByDesc(HotNews::getPublishTime);
        
        // 分页查询
        Page<HotNews> pageResult = hotNewsMapper.selectPage(new Page<>(page, pageSize), queryWrapper);
        
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
        
        HotNews news = hotNewsMapper.selectById(id);
        if (news == null) {
            return Result.error("新闻不存在");
        }
        
        // 更新浏览次数
        news.setViewCount(news.getViewCount() + 1);
        hotNewsMapper.updateById(news);
        
        return Result.success(news);
    }
    
    /**
     * 查询热点新闻列表
     * @param limit 限制数量
     * @return 热点新闻列表
     */
    @GetMapping("/hot")
    public Result<Page<HotNews>> getHotNews(@RequestParam(defaultValue = "5") Integer limit) {
        log.info("查询热点新闻列表：limit={}", limit);
        
        // 构建查询条件
        LambdaQueryWrapper<HotNews> queryWrapper = new LambdaQueryWrapper<>();
        // 只查询已发布且标记为热点的新闻
        queryWrapper.eq(HotNews::getStatus, 1)
                   .eq(HotNews::getIsHot, 1)
                   .orderByDesc(HotNews::getPublishTime);
        
        // 分页查询
        Page<HotNews> pageResult = hotNewsMapper.selectPage(new Page<>(1, limit), queryWrapper);
        
        return Result.success(pageResult);
    }
} 