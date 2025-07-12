package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.vo.*;

import java.util.List;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {
    
    /**
     * 获取仪表盘概览数据
     *
     * @return 仪表盘概览数据
     */
    DashboardVO getDashboardOverview();
    
    /**
     * 获取用户增长趋势
     *
     * @param timeRange 时间范围：week-本周, month-本月, year-全年
     * @return 用户增长趋势数据
     */
    UserGrowthVO getUserGrowthTrend(String timeRange);
    
    /**
     * 获取报修工单类型分布
     *
     * @return 工单类型分布数据
     */
    RepairTypeDistributionVO getRepairTypeDistribution();
    
    /**
     * 获取最新工单
     *
     * @param limit 数量限制
     * @return 最新工单列表
     */
    List<LatestOrderVO> getLatestOrders(Integer limit);
    
    /**
     * 获取待审核内容
     *
     * @param limit 数量限制
     * @return 待审核内容列表
     */
    List<PendingReviewVO> getPendingReviews(Integer limit);
} 