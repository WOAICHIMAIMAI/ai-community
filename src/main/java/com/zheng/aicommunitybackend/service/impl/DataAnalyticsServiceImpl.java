package com.zheng.aicommunitybackend.service.impl;

import com.zheng.aicommunitybackend.domain.dto.analytics.*;
import com.zheng.aicommunitybackend.mapper.AnalyticsMapper;
import com.zheng.aicommunitybackend.service.DataAnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 数据分析服务实现类
 */
@Slf4j
@Service
public class DataAnalyticsServiceImpl implements DataAnalyticsService {

    @Autowired
    private AnalyticsMapper analyticsMapper;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public List<RepairTypeDistributionDTO> getRepairTypeDistribution(Date startDate, Date endDate) {
        try {
            log.info("获取保修类型分布统计, 时间范围: {} - {}", dateFormat.format(startDate), dateFormat.format(endDate));
            List<RepairTypeDistributionDTO> result = analyticsMapper.getRepairTypeDistribution(startDate, endDate);
            
            // 计算百分比
            int totalCount = result.stream().mapToInt(RepairTypeDistributionDTO::getOrderCount).sum();
            if (totalCount > 0) {
                result.forEach(item -> {
                    BigDecimal percentage = BigDecimal.valueOf(item.getOrderCount())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
                    item.setPercentage(percentage);
                });
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取保修类型分布统计失败", e);
            throw new RuntimeException("获取保修类型分布统计失败");
        }
    }

    @Override
    public List<ServiceResponseTimeDTO> getServiceResponseTime(Date startDate, Date endDate, String serviceType) {
        try {
            log.info("获取服务响应时长统计, 时间范围: {} - {}, 服务类型: {}", 
                dateFormat.format(startDate), dateFormat.format(endDate), serviceType);
            
            List<ServiceResponseTimeDTO> result = analyticsMapper.getServiceResponseTime(startDate, endDate, serviceType);
            
            // 格式化日期字符串
            result.forEach(item -> {
                if (item.getDate() != null) {
                    item.setDateStr(dateFormat.format(item.getDate()));
                }
            });
            
            return result;
        } catch (Exception e) {
            log.error("获取服务响应时长统计失败", e);
            throw new RuntimeException("获取服务响应时长统计失败");
        }
    }

    @Override
    public List<UserActivityDTO> getUserActivityStatistics(Date startDate, Date endDate) {
        try {
            log.info("获取用户活跃度统计, 时间范围: {} - {}", dateFormat.format(startDate), dateFormat.format(endDate));
            
            List<UserActivityDTO> result = analyticsMapper.getUserActivityStatistics(startDate, endDate);
            
            // 格式化日期字符串并计算留存率
            result.forEach(item -> {
                if (item.getDate() != null) {
                    item.setDateStr(dateFormat.format(item.getDate()));
                }
                
                // 计算用户留存率（活跃用户数/新增用户数）
                if (item.getNewUsers() != null && item.getNewUsers() > 0 && 
                    item.getDailyActiveUsers() != null) {
                    BigDecimal retentionRate = BigDecimal.valueOf(item.getDailyActiveUsers())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(item.getNewUsers()), 2, RoundingMode.HALF_UP);
                    item.setRetentionRate(retentionRate);
                }
            });
            
            return result;
        } catch (Exception e) {
            log.error("获取用户活跃度统计失败", e);
            throw new RuntimeException("获取用户活跃度统计失败");
        }
    }

    @Override
    public List<ServiceStatisticsDTO> getServiceCompletionStatistics(Date startDate, Date endDate, Integer statisticsType) {
        try {
            log.info("获取服务完成量统计, 时间范围: {} - {}, 统计类型: {}", 
                dateFormat.format(startDate), dateFormat.format(endDate), statisticsType);
            
            List<ServiceStatisticsDTO> result = analyticsMapper.getServiceCompletionStatistics(startDate, endDate, statisticsType);
            
            // 计算完成率
            result.forEach(item -> {
                if (item.getOrderCount() != null && item.getOrderCount() > 0 && 
                    item.getCompletedCount() != null) {
                    BigDecimal completionRate = BigDecimal.valueOf(item.getCompletedCount())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(item.getOrderCount()), 2, RoundingMode.HALF_UP);
                    item.setCompletionRate(completionRate);
                }
                
                // 设置期间描述
                if (item.getDate() != null) {
                    item.setPeriod(formatPeriod(item.getDate(), statisticsType));
                }
            });
            
            return result;
        } catch (Exception e) {
            log.error("获取服务完成量统计失败", e);
            throw new RuntimeException("获取服务完成量统计失败");
        }
    }

    @Override
    public List<ServiceStatisticsDTO> getSatisfactionScoreStatistics(Date startDate, Date endDate, String serviceType) {
        try {
            log.info("获取满意度评分统计, 时间范围: {} - {}, 服务类型: {}", 
                dateFormat.format(startDate), dateFormat.format(endDate), serviceType);
            
            return analyticsMapper.getSatisfactionScoreStatistics(startDate, endDate, serviceType);
        } catch (Exception e) {
            log.error("获取满意度评分统计失败", e);
            throw new RuntimeException("获取满意度评分统计失败");
        }
    }

    @Override
    public List<ServiceStatisticsDTO> getHistoricalComparisonData(Date startDate, Date endDate, 
                                                                   Date compareStartDate, Date compareEndDate) {
        try {
            log.info("获取历史对比数据, 当前期间: {} - {}, 对比期间: {} - {}", 
                dateFormat.format(startDate), dateFormat.format(endDate),
                dateFormat.format(compareStartDate), dateFormat.format(compareEndDate));
            
            return analyticsMapper.getHistoricalComparisonData(startDate, endDate, compareStartDate, compareEndDate);
        } catch (Exception e) {
            log.error("获取历史对比数据失败", e);
            throw new RuntimeException("获取历史对比数据失败");
        }
    }

    @Override
    public AnalyticsReportDTO generateAnalyticsReport(AnalyticsQueryDTO queryDTO) {
        try {
            log.info("生成数据分析报告, 查询条件: {}", queryDTO);
            
            AnalyticsReportDTO report = new AnalyticsReportDTO();
            
            // 设置报告基本信息
            report.setTitle("智慧社区数据分析报告");
            report.setTimeRange(dateFormat.format(queryDTO.getStartDate()) + " 至 " + dateFormat.format(queryDTO.getEndDate()));
            
            // 获取各项统计数据
            report.setRepairTypeDistribution(getRepairTypeDistribution(queryDTO.getStartDate(), queryDTO.getEndDate()));
            report.setServiceResponseTime(getServiceResponseTime(queryDTO.getStartDate(), queryDTO.getEndDate(), queryDTO.getServiceType()));
            report.setUserActivity(getUserActivityStatistics(queryDTO.getStartDate(), queryDTO.getEndDate()));
            report.setServiceCompletionStats(getServiceCompletionStatistics(queryDTO.getStartDate(), queryDTO.getEndDate(), queryDTO.getStatisticsType()));
            report.setSatisfactionScoreStats(getSatisfactionScoreStatistics(queryDTO.getStartDate(), queryDTO.getEndDate(), queryDTO.getServiceType()));
            
            // 生成总结数据
            AnalyticsSummaryDTO summary = generateSummary(report);
            report.setSummary(summary);
            
            return report;
        } catch (Exception e) {
            log.error("生成数据分析报告失败", e);
            throw new RuntimeException("生成数据分析报告失败");
        }
    }

    /**
     * 生成总结数据
     */
    private AnalyticsSummaryDTO generateSummary(AnalyticsReportDTO report) {
        AnalyticsSummaryDTO summary = new AnalyticsSummaryDTO();
        
        // 统计总工单数和完成数
        int totalOrders = 0;
        int completedOrders = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        if (report.getServiceCompletionStats() != null) {
            for (ServiceStatisticsDTO stats : report.getServiceCompletionStats()) {
                if (stats.getOrderCount() != null) {
                    totalOrders += stats.getOrderCount();
                }
                if (stats.getCompletedCount() != null) {
                    completedOrders += stats.getCompletedCount();
                }
                if (stats.getTotalRevenue() != null) {
                    totalRevenue = totalRevenue.add(stats.getTotalRevenue());
                }
            }
        }
        
        summary.setTotalOrders(totalOrders);
        summary.setCompletedOrders(completedOrders);
        
        // 计算完成率
        if (totalOrders > 0) {
            BigDecimal completionRate = BigDecimal.valueOf(completedOrders)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
            summary.setCompletionRate(completionRate);
        }
        
        summary.setTotalRevenue(totalRevenue);
        
        // 统计用户活跃度
        if (report.getUserActivity() != null && !report.getUserActivity().isEmpty()) {
            UserActivityDTO latestActivity = report.getUserActivity().get(report.getUserActivity().size() - 1);
            summary.setActiveUsers(latestActivity.getDailyActiveUsers());
            summary.setUserActiveRate(latestActivity.getRetentionRate());
        }
        
        // 计算平均响应时长
        if (report.getServiceResponseTime() != null && !report.getServiceResponseTime().isEmpty()) {
            BigDecimal totalResponseTime = BigDecimal.ZERO;
            int count = 0;
            for (ServiceResponseTimeDTO responseTime : report.getServiceResponseTime()) {
                if (responseTime.getAvgResponseTime() != null) {
                    totalResponseTime = totalResponseTime.add(responseTime.getAvgResponseTime());
                    count++;
                }
            }
            if (count > 0) {
                summary.setAvgResponseTime(totalResponseTime.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
            }
        }
        
        // 计算平均满意度
        if (report.getSatisfactionScoreStats() != null && !report.getSatisfactionScoreStats().isEmpty()) {
            BigDecimal totalScore = BigDecimal.ZERO;
            int count = 0;
            for (ServiceStatisticsDTO stats : report.getSatisfactionScoreStats()) {
                if (stats.getAvgRating() != null) {
                    totalScore = totalScore.add(stats.getAvgRating());
                    count++;
                }
            }
            if (count > 0) {
                summary.setAvgSatisfactionScore(totalScore.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
            }
        }
        
        return summary;
    }

    /**
     * 格式化期间描述
     */
    private String formatPeriod(Date date, Integer statisticsType) {
        SimpleDateFormat formatter;
        switch (statisticsType) {
            case 1: // 日统计
                formatter = new SimpleDateFormat("yyyy-MM-dd");
                break;
            case 2: // 周统计
                formatter = new SimpleDateFormat("yyyy年第ww周");
                break;
            case 3: // 月统计
                formatter = new SimpleDateFormat("yyyy-MM月");
                break;
            case 4: // 季度统计
                formatter = new SimpleDateFormat("yyyy年Q季度");
                break;
            case 5: // 年统计
                formatter = new SimpleDateFormat("yyyy年");
                break;
            default:
                formatter = new SimpleDateFormat("yyyy-MM-dd");
        }
        return formatter.format(date);
    }
}
