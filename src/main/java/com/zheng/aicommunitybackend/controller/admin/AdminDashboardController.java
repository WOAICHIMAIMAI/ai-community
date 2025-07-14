package com.zheng.aicommunitybackend.controller.admin;

import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.*;
import com.zheng.aicommunitybackend.service.DashboardService;
import com.zheng.aicommunitybackend.task.LikeReconciliationTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理端仪表盘控制器
 */
@RestController
@RequestMapping("/admin/dashboard")
@Slf4j
@Tag(name = "管理端仪表盘接口", description = "管理端仪表盘数据相关接口")
public class AdminDashboardController {

    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private LikeReconciliationTask likeReconciliationTask;
    
    /**
     * 获取仪表盘概览数据
     *
     * @return 仪表盘概览数据
     */
    @GetMapping("/overview")
    @Operation(summary = "获取仪表盘概览数据", description = "获取用户、帖子、工单、审核内容的统计数据")
    public Result<DashboardVO> getDashboardOverview() {
        log.info("获取仪表盘概览数据");
        DashboardVO dashboardVO = dashboardService.getDashboardOverview();
        return Result.success(dashboardVO);
    }
    
    /**
     * 获取用户增长趋势
     *
     * @param timeRange 时间范围：week-本周, month-本月, year-全年
     * @return 用户增长趋势数据
     */
    @GetMapping("/user-growth")
    @Operation(summary = "获取用户增长趋势", description = "获取指定时间范围的用户增长趋势数据")
    public Result<UserGrowthVO> getUserGrowthTrend(
            @RequestParam(required = false, defaultValue = "week") 
            @Parameter(description = "时间范围：week-本周, month-本月, year-全年") String timeRange) {
        log.info("获取用户增长趋势，时间范围：{}", timeRange);
        UserGrowthVO userGrowthVO = dashboardService.getUserGrowthTrend(timeRange);
        return Result.success(userGrowthVO);
    }
    
    /**
     * 获取报修工单类型分布
     *
     * @return 工单类型分布数据
     */
    @GetMapping("/repair-types")
    @Operation(summary = "获取报修工单类型分布", description = "获取报修工单类型分布数据")
    public Result<RepairTypeDistributionVO> getRepairTypeDistribution() {
        log.info("获取报修工单类型分布");
        RepairTypeDistributionVO distributionVO = dashboardService.getRepairTypeDistribution();
        return Result.success(distributionVO);
    }
    
    /**
     * 获取最新工单
     *
     * @param limit 数量限制
     * @return 最新工单列表
     */
    @GetMapping("/latest-orders")
    @Operation(summary = "获取最新工单", description = "获取最新的报修工单列表")
    public Result<List<LatestOrderVO>> getLatestOrders(
            @RequestParam(required = false, defaultValue = "5") 
            @Parameter(description = "数量限制") Integer limit) {
        log.info("获取最新工单，数量限制：{}", limit);
        List<LatestOrderVO> latestOrders = dashboardService.getLatestOrders(limit);
        return Result.success(latestOrders);
    }
    
    /**
     * 获取待审核内容
     *
     * @param limit 数量限制
     * @return 待审核内容列表
     */
    @GetMapping("/pending-reviews")
    @Operation(summary = "获取待审核内容", description = "获取待审核的帖子、评论等内容列表")
    public Result<List<PendingReviewVO>> getPendingReviews(
            @RequestParam(required = false, defaultValue = "5") 
            @Parameter(description = "数量限制") Integer limit) {
        log.info("获取待审核内容，数量限制：{}", limit);
        List<PendingReviewVO> pendingReviews = dashboardService.getPendingReviews(limit);
        return Result.success(pendingReviews);
    }
    
    /**
     * 手动触发点赞数据对账补偿
     * 
     * @return 处理结果
     */
    @PostMapping("/reconcile-like-data")
    @Operation(summary = "手动触发点赞数据对账补偿", description = "手动执行Redis和MySQL点赞数据对账补偿")
    public Result<Map<String, Object>> reconcileLikeData() {
        log.info("手动触发点赞数据对账补偿");
        Map<String, Object> result = likeReconciliationTask.manualReconcile();
        return Result.success(result);
    }
} 