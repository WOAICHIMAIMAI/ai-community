package com.zheng.aicommunitybackend.controller;

import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.dto.analytics.*;
import com.zheng.aicommunitybackend.service.DataAnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * 数据分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DataAnalyticsController {

    @Autowired
    private DataAnalyticsService dataAnalyticsService;

    @GetMapping("/repair-type-distribution")
    public Result<List<RepairTypeDistributionDTO>> getRepairTypeDistribution(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        try {
            if (startDate == null || endDate == null) {
                return Result.error("开始日期和结束日期不能为空");
            }
            
            if (startDate.after(endDate)) {
                return Result.error("开始日期不能晚于结束日期");
            }
            
            List<RepairTypeDistributionDTO> result = dataAnalyticsService.getRepairTypeDistribution(startDate, endDate);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取保修类型分布统计失败", e);
            return Result.error("获取保修类型分布统计失败");
        }
    }

    @GetMapping("/service-response-time")
    public Result<List<ServiceResponseTimeDTO>> getServiceResponseTime(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) String serviceType) {
        
        try {
            if (startDate == null || endDate == null) {
                return Result.error("开始日期和结束日期不能为空");
            }
            
            if (startDate.after(endDate)) {
                return Result.error("开始日期不能晚于结束日期");
            }
            
            List<ServiceResponseTimeDTO> result = dataAnalyticsService.getServiceResponseTime(startDate, endDate, serviceType);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取服务响应时长统计失败", e);
            return Result.error("获取服务响应时长统计失败");
        }
    }

    @GetMapping("/user-activity")
    public Result<List<UserActivityDTO>> getUserActivityStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        try {
            if (startDate == null || endDate == null) {
                return Result.error("开始日期和结束日期不能为空");
            }
            
            if (startDate.after(endDate)) {
                return Result.error("开始日期不能晚于结束日期");
            }
            
            List<UserActivityDTO> result = dataAnalyticsService.getUserActivityStatistics(startDate, endDate);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户活跃度统计失败", e);
            return Result.error("获取用户活跃度统计失败");
        }
    }

    @GetMapping("/service-completion")
    public Result<List<ServiceStatisticsDTO>> getServiceCompletionStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(defaultValue = "3") Integer statisticsType) {
        
        try {
            if (startDate == null || endDate == null) {
                return Result.error("开始日期和结束日期不能为空");
            }
            
            if (startDate.after(endDate)) {
                return Result.error("开始日期不能晚于结束日期");
            }
            
            if (statisticsType == null || statisticsType < 1 || statisticsType > 5) {
                return Result.error("统计类型参数错误");
            }
            
            List<ServiceStatisticsDTO> result = dataAnalyticsService.getServiceCompletionStatistics(startDate, endDate, statisticsType);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取服务完成量统计失败", e);
            return Result.error("获取服务完成量统计失败");
        }
    }

    @GetMapping("/satisfaction-score")
    public Result<List<ServiceStatisticsDTO>> getSatisfactionScoreStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) String serviceType) {
        
        try {
            if (startDate == null || endDate == null) {
                return Result.error("开始日期和结束日期不能为空");
            }
            
            if (startDate.after(endDate)) {
                return Result.error("开始日期不能晚于结束日期");
            }
            
            List<ServiceStatisticsDTO> result = dataAnalyticsService.getSatisfactionScoreStatistics(startDate, endDate, serviceType);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取满意度评分统计失败", e);
            return Result.error("获取满意度评分统计失败");
        }
    }

    @GetMapping("/historical-comparison")
    public Result<List<ServiceStatisticsDTO>> getHistoricalComparisonData(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date compareStartDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date compareEndDate) {
        
        try {
            if (startDate == null || endDate == null || compareStartDate == null || compareEndDate == null) {
                return Result.error("日期参数不能为空");
            }
            
            if (startDate.after(endDate) || compareStartDate.after(compareEndDate)) {
                return Result.error("开始日期不能晚于结束日期");
            }
            
            List<ServiceStatisticsDTO> result = dataAnalyticsService.getHistoricalComparisonData(
                startDate, endDate, compareStartDate, compareEndDate);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取历史对比数据失败", e);
            return Result.error("获取历史对比数据失败");
        }
    }

    @PostMapping("/report")
    public Result<AnalyticsReportDTO> generateAnalyticsReport(
            @Valid @RequestBody AnalyticsQueryDTO queryDTO) {
        
        try {
            if (queryDTO.getStartDate() == null || queryDTO.getEndDate() == null) {
                return Result.error("开始日期和结束日期不能为空");
            }
            
            if (queryDTO.getStartDate().after(queryDTO.getEndDate())) {
                return Result.error("开始日期不能晚于结束日期");
            }
            
            AnalyticsReportDTO result = dataAnalyticsService.generateAnalyticsReport(queryDTO);
            return Result.success(result);
        } catch (Exception e) {
            log.error("生成数据分析报告失败", e);
            return Result.error("生成数据分析报告失败");
        }
    }
}
