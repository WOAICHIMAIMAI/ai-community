package com.zheng.aicommunitybackend.task;

import com.zheng.aicommunitybackend.config.BloomFilterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 布隆过滤器监控任务
 * 定期检查布隆过滤器状态并发出告警
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "bloom-filter.monitoring.enabled", havingValue = "true", matchIfMissing = true)
public class BloomFilterMonitorTask {

    @Autowired
    private BloomFilterConfig bloomFilterConfig;

    /**
     * 监控检查间隔（分钟）
     */
    @Value("${bloom-filter.monitoring.check-interval-minutes:30}")
    private int checkIntervalMinutes;

    /**
     * 是否启用邮件告警
     */
    @Value("${bloom-filter.monitoring.email-alert-enabled:false}")
    private boolean emailAlertEnabled;

    /**
     * 定期监控布隆过滤器状态
     * 默认每30分钟检查一次
     */
    @Scheduled(fixedRateString = "#{${bloom-filter.monitoring.check-interval-minutes:30} * 60 * 1000}")
    public void monitorBloomFilterStatus() {
        try {
            BloomFilterConfig.BloomFilterStats stats = bloomFilterConfig.getStats();
            BloomFilterConfig.AlertLevel alertLevel = stats.getAlertLevel();
            
            // 记录监控日志
            log.debug("布隆过滤器状态检查 - 使用率: {:.1%}, 告警级别: {}", 
                    stats.getUsageRatio(), alertLevel.getName());
            
            // 如果需要关注，记录详细信息
            if (stats.needsAttention()) {
                logDetailedStatus(stats);
                
                // 如果是警告或危险级别，考虑发送告警
                if (alertLevel == BloomFilterConfig.AlertLevel.WARNING || 
                    alertLevel == BloomFilterConfig.AlertLevel.CRITICAL) {
                    handleHighLevelAlert(stats);
                }
            }
            
        } catch (Exception e) {
            log.error("布隆过滤器状态监控失败", e);
        }
    }

    /**
     * 记录详细状态信息
     */
    private void logDetailedStatus(BloomFilterConfig.BloomFilterStats stats) {
        log.info("📊 布隆过滤器详细状态:");
        log.info("   使用率: {:.1%} ({}/{})", 
                stats.getUsageRatio(), stats.getLoadedUrlCount(), stats.getExpectedInsertions());
        log.info("   告警级别: {} - {}", 
                stats.getAlertLevel().getName(), stats.getAlertLevel().getDescription());
        log.info("   数据保留天数: {} 天", stats.getDataRetentionDays());
        log.info("   最后重建时间: {}", stats.getLastRebuildTime());
        log.info("   建议操作: {}", stats.getAlertRecommendation());
    }

    /**
     * 处理高级别告警
     */
    private void handleHighLevelAlert(BloomFilterConfig.BloomFilterStats stats) {
        BloomFilterConfig.AlertLevel level = stats.getAlertLevel();
        
        // 记录高级别告警
        if (level == BloomFilterConfig.AlertLevel.CRITICAL) {
            log.error("🚨 布隆过滤器进入危险状态！");
            log.error("   使用率: {:.1%}", stats.getUsageRatio());
            log.error("   建议: {}", stats.getAlertRecommendation());
            
            // 可以在这里添加自动处理逻辑
            // 例如：自动触发重建、发送邮件、调用webhook等
            handleCriticalAlert(stats);
            
        } else if (level == BloomFilterConfig.AlertLevel.WARNING) {
            log.warn("⚠️ 布隆过滤器需要关注");
            log.warn("   使用率: {:.1%}", stats.getUsageRatio());
            log.warn("   建议: {}", stats.getAlertRecommendation());
        }
        
        // 发送邮件告警（如果启用）
        if (emailAlertEnabled) {
            sendEmailAlert(stats);
        }
    }

    /**
     * 处理危险级别告警（针对7天数据周期优化）
     */
    private void handleCriticalAlert(BloomFilterConfig.BloomFilterStats stats) {
        log.warn("7天数据周期下的危险状态处理...");

        // 7天周期下，90%就需要立即处理
        if (stats.getUsageRatio() > 0.9) {
            log.error("7天数据周期下使用率过高，强烈建议立即重建布隆过滤器");
            log.error("当前数据量: {} / {}", stats.getLoadedUrlCount(), stats.getExpectedInsertions());
            log.error("预计明日将新增约3000条数据，可能导致严重性能问题");

            // 7天周期下可以考虑更激进的自动处理
            // 因为重建频率高，影响相对较小
            if (stats.getUsageRatio() > 0.95) {
                log.warn("考虑在非高峰期自动重建（7天周期重建影响较小）");
                // 可选：在凌晨时段自动重建
                // autoRebuildIfSafe();
            }
        }
    }

    /**
     * 安全的自动重建（仅在7天周期下考虑）
     */
    private void autoRebuildIfSafe() {
        // 检查当前时间是否适合自动重建
        java.time.LocalTime now = java.time.LocalTime.now();
        // 凌晨1-5点相对安全
        if (now.getHour() >= 1 && now.getHour() <= 5) {
            log.info("在安全时间窗口内执行自动重建");
            try {
                long result = bloomFilterConfig.rebuildBloomFilter();
                log.info("自动重建完成，加载了{}个URL", result);
            } catch (Exception e) {
                log.error("自动重建失败", e);
            }
        } else {
            log.warn("当前时间不适合自动重建，等待安全时间窗口");
        }
    }

    /**
     * 发送邮件告警
     */
    private void sendEmailAlert(BloomFilterConfig.BloomFilterStats stats) {
        // 这里可以集成邮件服务
        log.info("📧 发送邮件告警 - 布隆过滤器状态: {}", stats.getAlertLevel().getName());
        
        // 示例邮件内容
        String subject = String.format("布隆过滤器告警 - %s", stats.getAlertLevel().getName());
        String content = String.format(
                "布隆过滤器状态告警\n\n" +
                "告警级别: %s\n" +
                "使用率: %.1f%%\n" +
                "已加载URL数量: %d\n" +
                "预期容量: %d\n" +
                "数据保留天数: %d\n" +
                "最后重建时间: %s\n\n" +
                "建议操作:\n%s",
                stats.getAlertLevel().getName(),
                stats.getUsageRatio() * 100,
                stats.getLoadedUrlCount(),
                stats.getExpectedInsertions(),
                stats.getDataRetentionDays(),
                stats.getLastRebuildTime(),
                stats.getAlertRecommendation()
        );
        
        // TODO: 实际的邮件发送逻辑
        log.debug("邮件内容: {}", content);
    }

    /**
     * 手动触发状态检查
     * @return 当前状态
     */
    public BloomFilterConfig.BloomFilterStats checkStatus() {
        BloomFilterConfig.BloomFilterStats stats = bloomFilterConfig.getStats();
        logDetailedStatus(stats);
        return stats;
    }

    /**
     * 获取监控配置信息
     */
    public String getMonitoringInfo() {
        return String.format(
                "布隆过滤器监控配置:\n" +
                "- 检查间隔: %d 分钟\n" +
                "- 邮件告警: %s\n" +
                "- 当前状态: %s",
                checkIntervalMinutes,
                emailAlertEnabled ? "启用" : "禁用",
                bloomFilterConfig.getCurrentAlertLevel().getName()
        );
    }
}
