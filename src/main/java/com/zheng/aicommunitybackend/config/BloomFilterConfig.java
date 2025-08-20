package com.zheng.aicommunitybackend.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.zheng.aicommunitybackend.domain.entity.HotNews;
import com.zheng.aicommunitybackend.mapper.HotNewsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 布隆过滤器配置
 * 用于初始化布隆过滤器并加载数据库中已有的URL
 * 支持动态重建和数据生命周期管理
 */
@Slf4j
@Configuration
public class BloomFilterConfig {

    @Autowired
    private HotNewsMapper hotNewsMapper;

    /**
     * 预计插入的元素数量 - 可通过配置文件调整
     */
    @Value("${bloom-filter.expected-insertions:200000}")
    private int expectedInsertions;

    /**
     * 可接受的误判率 - 可通过配置文件调整
     */
    @Value("${bloom-filter.false-positive-probability:0.01}")
    private double falsePositiveProbability;

    /**
     * 数据保留天数 - 只加载最近N天的数据到布隆过滤器
     */
    @Value("${bloom-filter.data-retention-days:60}")
    private int dataRetentionDays;

    /**
     * 是否启用自动重建
     */
    @Value("${bloom-filter.auto-rebuild-enabled:true}")
    private boolean autoRebuildEnabled;

    /**
     * 布隆过滤器实例
     */
    private volatile BloomFilter<String> bloomFilter;

    /**
     * 当前加载的URL数量
     */
    private final AtomicLong loadedUrlCount = new AtomicLong(0);

    /**
     * 最后重建时间
     */
    private volatile LocalDateTime lastRebuildTime;

    /**
     * 当前告警级别
     */
    private final AtomicReference<AlertLevel> currentAlertLevel = new AtomicReference<>(AlertLevel.NORMAL);

    /**
     * 告警级别枚举
     */
    public enum AlertLevel {
        NORMAL(0, "正常", "使用率正常"),
        INFO(1, "注意", "使用率较高，需要关注"),
        WARNING(2, "警告", "使用率过高，建议扩容或清理"),
        CRITICAL(3, "危险", "使用率极高，需要立即处理");

        private final int level;
        private final String name;
        private final String description;

        AlertLevel(int level, String name, String description) {
            this.level = level;
            this.name = name;
            this.description = description;
        }

        public int getLevel() { return level; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    /**
     * 创建布隆过滤器Bean
     * @return 布隆过滤器实例
     */
    @Bean
    public BloomFilter<String> urlBloomFilter() {
        log.info("初始化布隆过滤器，预计容量: {}, 误判率: {}, 数据保留天数: {}",
                expectedInsertions, falsePositiveProbability, dataRetentionDays);

        // 创建布隆过滤器
        this.bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                expectedInsertions,
                falsePositiveProbability);

        // 加载数据库中已有的URL
        loadExistingUrls(this.bloomFilter);
        this.lastRebuildTime = LocalDateTime.now();

        return this.bloomFilter;
    }

    /**
     * 获取布隆过滤器使用统计信息
     * @return 统计信息
     */
    public BloomFilterStats getStats() {
        return BloomFilterStats.builder()
                .expectedInsertions(expectedInsertions)
                .falsePositiveProbability(falsePositiveProbability)
                .loadedUrlCount(loadedUrlCount.get())
                .lastRebuildTime(lastRebuildTime)
                .dataRetentionDays(dataRetentionDays)
                .alertLevel(getCurrentAlertLevel())
                .alertRecommendation(getAlertRecommendation())
                .build();
    }

    /**
     * 手动重建布隆过滤器
     * @return 重建后加载的URL数量
     */
    public synchronized long rebuildBloomFilter() {
        log.info("开始手动重建布隆过滤器...");

        // 创建新的布隆过滤器
        BloomFilter<String> newBloomFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                expectedInsertions,
                falsePositiveProbability);

        // 加载数据
        long loadedCount = loadExistingUrls(newBloomFilter);

        // 原子性替换
        this.bloomFilter = newBloomFilter;
        this.lastRebuildTime = LocalDateTime.now();

        log.info("布隆过滤器重建完成，加载了{}个URL", loadedCount);
        return loadedCount;
    }

    /**
     * 更新告警级别
     * 针对7天数据周期优化的告警阈值
     * @param usageRatio 使用率
     */
    private void updateAlertLevel(double usageRatio) {
        AlertLevel newLevel;

        // 7天周期的告警阈值调整：更早预警，更频繁清理
        if (usageRatio >= 0.9) {
            newLevel = AlertLevel.CRITICAL;  // 90%即为危险，因为7天内可能快速增长
        } else if (usageRatio >= 0.7) {
            newLevel = AlertLevel.WARNING;   // 70%开始警告，预留更多缓冲时间
        } else if (usageRatio >= 0.5) {
            newLevel = AlertLevel.INFO;      // 50%开始关注，因为增长速度较快
        } else {
            newLevel = AlertLevel.NORMAL;
        }

        AlertLevel oldLevel = currentAlertLevel.getAndSet(newLevel);

        // 只有当告警级别发生变化时才记录日志
        if (oldLevel != newLevel) {
            logAlertLevelChange(oldLevel, newLevel, usageRatio);
        }

        // 根据告警级别记录不同级别的日志
        switch (newLevel) {
            case CRITICAL:
                log.error("🚨 布隆过滤器使用率危险: {:.1%} - {}", usageRatio, newLevel.getDescription());
                break;
            case WARNING:
                log.warn("⚠️ 布隆过滤器使用率警告: {:.1%} - {}", usageRatio, newLevel.getDescription());
                break;
            case INFO:
                log.info("ℹ️ 布隆过滤器使用率注意: {:.1%} - {}", usageRatio, newLevel.getDescription());
                break;
            case NORMAL:
                log.debug("✅ 布隆过滤器使用率正常: {:.1%}", usageRatio);
                break;
        }
    }

    /**
     * 记录告警级别变化
     */
    private void logAlertLevelChange(AlertLevel oldLevel, AlertLevel newLevel, double usageRatio) {
        if (newLevel.getLevel() > oldLevel.getLevel()) {
            log.warn("📈 布隆过滤器告警级别升级: {} -> {} (使用率: {:.1%})",
                    oldLevel.getName(), newLevel.getName(), usageRatio);
        } else {
            log.info("📉 布隆过滤器告警级别降级: {} -> {} (使用率: {:.1%})",
                    oldLevel.getName(), newLevel.getName(), usageRatio);
        }
    }

    /**
     * 获取当前告警级别
     */
    public AlertLevel getCurrentAlertLevel() {
        return currentAlertLevel.get();
    }

    /**
     * 计算当前使用率（内部方法，避免循环调用）
     * @return 使用率 (0.0 - 1.0)
     */
    private double calculateUsageRatio() {
        return (double) loadedUrlCount.get() / expectedInsertions;
    }

    /**
     * 检查是否需要立即处理
     */
    public boolean needsImmediateAction() {
        AlertLevel level = currentAlertLevel.get();
        return level == AlertLevel.CRITICAL || level == AlertLevel.WARNING;
    }

    /**
     * 获取告警建议（针对7天数据周期优化）
     */
    public String getAlertRecommendation() {
        AlertLevel level = currentAlertLevel.get();
        // 使用内部方法计算使用率，避免循环调用getStats()
        double usageRatio = calculateUsageRatio();

        switch (level) {
            case CRITICAL:
                return String.format("使用率已达到 %.1f%%，7天数据周期下需立即处理：\n" +
                        "1. 立即手动重建布隆过滤器\n" +
                        "2. 检查是否有异常数据增长\n" +
                        "3. 考虑临时减少保留天数至5天\n" +
                        "4. 如持续增长，需增加容量至80000", usageRatio * 100);
            case WARNING:
                return String.format("使用率已达到 %.1f%%，7天周期下建议：\n" +
                        "1. 密切监控数据增长（每日约3000条）\n" +
                        "2. 准备在24小时内执行重建\n" +
                        "3. 检查数据清理任务是否正常\n" +
                        "4. 评估是否需要扩容", usageRatio * 100);
            case INFO:
                return String.format("使用率为 %.1f%%，7天周期下请关注：\n" +
                        "1. 当前约有 %d 条URL数据\n" +
                        "2. 预计2-3天内可能达到警告级别\n" +
                        "3. 建议每日检查数据增长情况",
                        usageRatio * 100, loadedUrlCount.get());
            case NORMAL:
            default:
                return String.format("使用率正常(%.1f%%)，7天数据周期运行良好", usageRatio * 100);
        }
    }
    
    /**
     * 从数据库加载已有的URL到布隆过滤器
     * 只加载最近N天的数据，避免过载
     * @param bloomFilter 布隆过滤器实例
     * @return 加载的URL数量
     */
    private long loadExistingUrls(BloomFilter<String> bloomFilter) {
        try {
            log.info("开始加载数据库中最近{}天的URL到布隆过滤器...", dataRetentionDays);

            // 只查询最近N天的数据和source_url字段，减少数据传输量
            LambdaQueryWrapper<HotNews> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(HotNews::getSourceUrl);
            queryWrapper.ge(HotNews::getCreateTime,
                    LocalDateTime.now().minusDays(dataRetentionDays));
            queryWrapper.isNotNull(HotNews::getSourceUrl);
            queryWrapper.ne(HotNews::getSourceUrl, "");

            List<HotNews> newsList = hotNewsMapper.selectList(queryWrapper);

            long count = 0;
            for (HotNews news : newsList) {
                String url = news.getSourceUrl();
                if (url != null && !url.trim().isEmpty()) {
                    bloomFilter.put(url);
                    count++;
                }
            }

            loadedUrlCount.set(count);
            log.info("成功加载{}个URL到布隆过滤器（最近{}天的数据）", count, dataRetentionDays);

            // 检查使用率并更新告警级别
            double usageRatio = (double) count / expectedInsertions;
            updateAlertLevel(usageRatio);

            return count;
        } catch (Exception e) {
            log.error("加载URL到布隆过滤器时出错: {}", e.getMessage(), e);
            loadedUrlCount.set(0);
            return 0;
        }
    }

    /**
     * 自动重建任务 - 每天凌晨2点执行
     * 清理过期数据并重建布隆过滤器
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoRebuildTask() {
        if (!autoRebuildEnabled) {
            log.debug("自动重建功能已禁用，跳过执行");
            return;
        }

        try {
            log.info("开始执行布隆过滤器自动重建任务...");

            // 先清理过期数据
            cleanExpiredData();

            // 重建布隆过滤器
            rebuildBloomFilter();

            log.info("布隆过滤器自动重建任务完成");
        } catch (Exception e) {
            log.error("布隆过滤器自动重建任务执行失败", e);
        }
    }

    /**
     * 清理过期的新闻数据
     * @return 清理的记录数量
     */
    private int cleanExpiredData() {
        try {
            log.info("开始清理{}天前的过期新闻数据...", dataRetentionDays);

            LambdaQueryWrapper<HotNews> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.lt(HotNews::getCreateTime,
                    LocalDateTime.now().minusDays(dataRetentionDays));

            // 先查询要删除的数量
            Long expiredCount = hotNewsMapper.selectCount(queryWrapper);

            if (expiredCount > 0) {
                // 分批删除，避免一次性删除太多数据
                int batchSize = 1000;
                int deletedTotal = 0;

                while (true) {
                    queryWrapper.last("LIMIT " + batchSize);
                    List<HotNews> expiredNews = hotNewsMapper.selectList(queryWrapper);

                    if (expiredNews.isEmpty()) {
                        break;
                    }

                    for (HotNews news : expiredNews) {
                        hotNewsMapper.deleteById(news.getId());
                        deletedTotal++;
                    }

                    log.info("已清理{}条过期数据，总计需清理{}条", deletedTotal, expiredCount);

                    // 避免删除操作过于频繁
                    Thread.sleep(100);
                }

                log.info("过期数据清理完成，共清理{}条记录", deletedTotal);
                return deletedTotal;
            } else {
                log.info("没有找到需要清理的过期数据");
                return 0;
            }
        } catch (Exception e) {
            log.error("清理过期数据时出错", e);
            return 0;
        }
    }

    /**
     * 布隆过滤器统计信息
     */
    public static class BloomFilterStats {
        private final int expectedInsertions;
        private final double falsePositiveProbability;
        private final long loadedUrlCount;
        private final LocalDateTime lastRebuildTime;
        private final int dataRetentionDays;
        private final AlertLevel alertLevel;
        private final String alertRecommendation;

        public BloomFilterStats(int expectedInsertions, double falsePositiveProbability,
                               long loadedUrlCount, LocalDateTime lastRebuildTime, int dataRetentionDays,
                               AlertLevel alertLevel, String alertRecommendation) {
            this.expectedInsertions = expectedInsertions;
            this.falsePositiveProbability = falsePositiveProbability;
            this.loadedUrlCount = loadedUrlCount;
            this.lastRebuildTime = lastRebuildTime;
            this.dataRetentionDays = dataRetentionDays;
            this.alertLevel = alertLevel;
            this.alertRecommendation = alertRecommendation;
        }

        public static BloomFilterStatsBuilder builder() {
            return new BloomFilterStatsBuilder();
        }

        // Getters
        public int getExpectedInsertions() { return expectedInsertions; }
        public double getFalsePositiveProbability() { return falsePositiveProbability; }
        public long getLoadedUrlCount() { return loadedUrlCount; }
        public LocalDateTime getLastRebuildTime() { return lastRebuildTime; }
        public int getDataRetentionDays() { return dataRetentionDays; }
        public AlertLevel getAlertLevel() { return alertLevel; }
        public String getAlertRecommendation() { return alertRecommendation; }
        public double getUsageRatio() { return (double) loadedUrlCount / expectedInsertions; }
        public boolean needsAttention() { return alertLevel.getLevel() >= AlertLevel.INFO.getLevel(); }

        public static class BloomFilterStatsBuilder {
            private int expectedInsertions;
            private double falsePositiveProbability;
            private long loadedUrlCount;
            private LocalDateTime lastRebuildTime;
            private int dataRetentionDays;
            private AlertLevel alertLevel;
            private String alertRecommendation;

            public BloomFilterStatsBuilder expectedInsertions(int expectedInsertions) {
                this.expectedInsertions = expectedInsertions;
                return this;
            }

            public BloomFilterStatsBuilder falsePositiveProbability(double falsePositiveProbability) {
                this.falsePositiveProbability = falsePositiveProbability;
                return this;
            }

            public BloomFilterStatsBuilder loadedUrlCount(long loadedUrlCount) {
                this.loadedUrlCount = loadedUrlCount;
                return this;
            }

            public BloomFilterStatsBuilder lastRebuildTime(LocalDateTime lastRebuildTime) {
                this.lastRebuildTime = lastRebuildTime;
                return this;
            }

            public BloomFilterStatsBuilder dataRetentionDays(int dataRetentionDays) {
                this.dataRetentionDays = dataRetentionDays;
                return this;
            }

            public BloomFilterStatsBuilder alertLevel(AlertLevel alertLevel) {
                this.alertLevel = alertLevel;
                return this;
            }

            public BloomFilterStatsBuilder alertRecommendation(String alertRecommendation) {
                this.alertRecommendation = alertRecommendation;
                return this;
            }

            public BloomFilterStats build() {
                return new BloomFilterStats(expectedInsertions, falsePositiveProbability,
                                          loadedUrlCount, lastRebuildTime, dataRetentionDays,
                                          alertLevel, alertRecommendation);
            }
        }
    }
}