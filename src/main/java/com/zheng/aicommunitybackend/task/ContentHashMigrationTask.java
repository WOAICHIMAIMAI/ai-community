package com.zheng.aicommunitybackend.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.aicommunitybackend.domain.entity.HotNews;
import com.zheng.aicommunitybackend.mapper.HotNewsMapper;
import com.zheng.aicommunitybackend.service.ContentSimilarityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 内容指纹迁移任务
 * 为现有新闻生成内容指纹
 */
@Slf4j
@Component
@Order(100) // 设置较低优先级，确保在其他初始化完成后执行
public class ContentHashMigrationTask implements CommandLineRunner {

    @Autowired
    private HotNewsMapper hotNewsMapper;
    
    @Autowired
    private ContentSimilarityService contentSimilarityService;
    
    @Override
    public void run(String... args) {
        log.info("开始为现有新闻生成内容指纹...");
        
        int pageSize = 100;
        int currentPage = 1;
        int total = 0;
        int processed = 0;
        
        // 查询没有内容指纹的新闻
        LambdaQueryWrapper<HotNews> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNull(HotNews::getContentHash);
        
        // 获取总数
        total = hotNewsMapper.selectCount(queryWrapper).intValue();
        log.info("共有{}条新闻需要生成内容指纹", total);
        
        if (total == 0) {
            log.info("没有需要处理的新闻，迁移任务结束");
            return;
        }
        
        // 分页处理
        while (true) {
            Page<HotNews> page = hotNewsMapper.selectPage(new Page<>(currentPage, pageSize), queryWrapper);
            if (page.getRecords().isEmpty()) {
                break;
            }
            
            for (HotNews news : page.getRecords()) {
                try {
                    // 生成内容指纹
                    contentSimilarityService.generateContentHash(news);
                    
                    // 更新到数据库
                    hotNewsMapper.updateById(news);
                    
                    processed++;
                    if (processed % 100 == 0) {
                        log.info("已处理 {}/{} 条新闻", processed, total);
                    }
                } catch (Exception e) {
                    log.error("处理新闻指纹失败: ID={}, 标题={}, 错误: {}", 
                            news.getId(), news.getTitle(), e.getMessage());
                }
            }
            
            currentPage++;
        }
        
        log.info("内容指纹迁移任务完成，共处理{}条新闻", processed);
    }
} 