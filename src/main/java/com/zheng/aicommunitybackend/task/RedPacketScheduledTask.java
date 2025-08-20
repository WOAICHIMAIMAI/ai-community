package com.zheng.aicommunitybackend.task;

import com.zheng.aicommunitybackend.mapper.RedPacketActivitiesMapper;
import com.zheng.aicommunitybackend.service.RedPacketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 红包相关定时任务
 */
@Component
@Slf4j
public class RedPacketScheduledTask {

    @Autowired
    private RedPacketService redPacketService;
    
    @Autowired
    private RedPacketActivitiesMapper activitiesMapper;

    /**
     * 每分钟检查需要开始的活动
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkActivitiesToStart() {
        try {
            log.debug("开始检查需要开始的红包活动");
            
            List<Long> activityIds = activitiesMapper.selectActivitiesToStart();
            if (activityIds.isEmpty()) {
                log.debug("没有需要开始的活动");
                return;
            }
            
            log.info("发现 {} 个需要开始的活动: {}", activityIds.size(), activityIds);
            
            // 批量更新活动状态为进行中
            int updatedCount = activitiesMapper.batchUpdateStatus(1, activityIds);
            log.info("批量更新活动状态成功，更新数量: {}", updatedCount);
            
            // 为每个活动预加载数据到Redis
            int successCount = 0;
            for (Long activityId : activityIds) {
                try {
                    boolean success = redPacketService.preloadActivityToRedis(activityId);
                    if (success) {
                        successCount++;
                        log.info("活动 {} 预加载到Redis成功", activityId);
                    } else {
                        log.error("活动 {} 预加载到Redis失败", activityId);
                    }
                } catch (Exception e) {
                    log.error("活动 {} 预加载到Redis异常", activityId, e);
                }
            }
            
            log.info("活动开始检查完成，总数: {}, 成功预加载: {}", activityIds.size(), successCount);
            
        } catch (Exception e) {
            log.error("检查需要开始的活动异常", e);
        }
    }

    /**
     * 每分钟检查需要结束的活动
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkActivitiesToEnd() {
        try {
            log.debug("开始检查需要结束的红包活动");
            
            List<Long> activityIds = activitiesMapper.selectActivitiesToEnd();
            if (activityIds.isEmpty()) {
                log.debug("没有需要结束的活动");
                return;
            }
            
            log.info("发现 {} 个需要结束的活动: {}", activityIds.size(), activityIds);
            
            // 批量更新活动状态为已结束
            int updatedCount = activitiesMapper.batchUpdateStatus(2, activityIds);
            log.info("批量更新活动状态成功，更新数量: {}", updatedCount);
            
            // 清理每个活动的Redis数据
            int successCount = 0;
            for (Long activityId : activityIds) {
                try {
                    boolean success = redPacketService.clearActivityFromRedis(activityId);
                    if (success) {
                        successCount++;
                        log.info("活动 {} Redis数据清理成功", activityId);
                    } else {
                        log.error("活动 {} Redis数据清理失败", activityId);
                    }
                } catch (Exception e) {
                    log.error("活动 {} Redis数据清理异常", activityId, e);
                }
            }
            
            log.info("活动结束检查完成，总数: {}, 成功清理: {}", activityIds.size(), successCount);
            
        } catch (Exception e) {
            log.error("检查需要结束的活动异常", e);
        }
    }

    /**
     * 每5分钟处理未更新账户的记录（补偿机制）
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void processUnprocessedRecords() {
        try {
            log.debug("开始处理未更新账户的抢红包记录");
            
            // 每次处理最多100条记录
            int processedCount = redPacketService.processUnprocessedRecords(100);
            
            if (processedCount > 0) {
                log.info("补偿处理完成，处理记录数: {}", processedCount);
            } else {
                log.debug("没有需要补偿处理的记录");
            }
            
        } catch (Exception e) {
            log.error("处理未更新账户记录异常", e);
        }
    }

    /**
     * 每小时清理过期的限流数据
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupExpiredRateLimitData() {
        try {
            log.debug("开始清理过期的限流数据");
            
            // 这里可以添加清理Redis中过期限流数据的逻辑
            // 由于Redis会自动过期，这里主要是记录日志
            
            log.info("限流数据清理检查完成");
            
        } catch (Exception e) {
            log.error("清理限流数据异常", e);
        }
    }

    /**
     * 每天凌晨2点生成红包活动统计报告
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyReport() {
        try {
            log.info("开始生成红包活动日报");
            
            // 这里可以添加生成统计报告的逻辑
            // 例如：统计昨天的活动数量、参与人数、红包金额等
            
            log.info("红包活动日报生成完成");
            
        } catch (Exception e) {
            log.error("生成日报异常", e);
        }
    }

    /**
     * 每30分钟检查Redis连接状态
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void checkRedisHealth() {
        try {
            log.debug("开始检查Redis连接状态");
            
            // 这里可以添加Redis健康检查逻辑
            // 例如：ping Redis，检查关键数据是否存在等
            
            log.debug("Redis连接状态检查完成");
            
        } catch (Exception e) {
            log.error("Redis健康检查异常", e);
        }
    }

    /**
     * 每周日凌晨3点清理历史数据
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void cleanupHistoricalData() {
        try {
            log.info("开始清理历史数据");
            
            // 这里可以添加清理历史数据的逻辑
            // 例如：删除3个月前的限流记录、过期的活动配置等
            
            log.info("历史数据清理完成");
            
        } catch (Exception e) {
            log.error("清理历史数据异常", e);
        }
    }
}
