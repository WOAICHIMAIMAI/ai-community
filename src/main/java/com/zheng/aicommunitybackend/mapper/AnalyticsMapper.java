package com.zheng.aicommunitybackend.mapper;

import com.zheng.aicommunitybackend.domain.dto.analytics.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 数据分析专用Mapper - 用于复杂查询
 */
@Mapper
public interface AnalyticsMapper {

    /**
     * 获取保修类型分布
     */
    List<RepairTypeDistributionDTO> getRepairTypeDistribution(@Param("startDate") Date startDate, 
                                                               @Param("endDate") Date endDate);

    /**
     * 获取服务响应时长统计
     */
    List<ServiceResponseTimeDTO> getServiceResponseTime(@Param("startDate") Date startDate, 
                                                         @Param("endDate") Date endDate,
                                                         @Param("serviceType") String serviceType);

    /**
     * 获取用户活跃度统计
     */
    List<UserActivityDTO> getUserActivityStatistics(@Param("startDate") Date startDate, 
                                                     @Param("endDate") Date endDate);

    /**
     * 获取服务完成量统计
     */
    List<ServiceStatisticsDTO> getServiceCompletionStatistics(@Param("startDate") Date startDate, 
                                                               @Param("endDate") Date endDate,
                                                               @Param("statisticsType") Integer statisticsType);

    /**
     * 获取满意度评分统计
     */
    List<ServiceStatisticsDTO> getSatisfactionScoreStatistics(@Param("startDate") Date startDate, 
                                                               @Param("endDate") Date endDate,
                                                               @Param("serviceType") String serviceType);

    /**
     * 获取历史对比数据
     */
    List<ServiceStatisticsDTO> getHistoricalComparisonData(@Param("startDate") Date startDate, 
                                                            @Param("endDate") Date endDate,
                                                            @Param("compareStartDate") Date compareStartDate,
                                                            @Param("compareEndDate") Date compareEndDate);
}
