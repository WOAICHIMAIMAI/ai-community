package com.zheng.aicommunitybackend.controller.admin;

import com.zheng.aicommunitybackend.common.Result;
import com.zheng.aicommunitybackend.domain.dto.ServiceApprovalDTO;
import com.zheng.aicommunitybackend.domain.dto.ServicePageQuery;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.AppointmentServiceVO;
import com.zheng.aicommunitybackend.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员预约服务管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/appointment/services")
@Tag(name = "管理员服务管理接口", description = "管理预约服务类型")
public class AdminAppointmentServiceController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 分页查询服务列表
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询服务列表", description = "管理员分页查询所有预约服务")
    public Result<PageResult<AppointmentServiceVO>> getServiceList(@RequestBody @Validated ServicePageQuery query) {
        log.info("管理员查询服务列表，参数：{}", query);
        PageResult<AppointmentServiceVO> result = appointmentService.adminGetServicePage(query);
        return Result.success(result);
    }

    /**
     * 获取服务详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取服务详情", description = "根据ID获取服务详情")
    public Result<AppointmentServiceVO> getServiceDetail(
            @Parameter(description = "服务ID") @PathVariable Long id) {
        log.info("管理员获取服务详情，ID：{}", id);
        AppointmentServiceVO service = appointmentService.adminGetServiceDetail(id);
        return Result.success(service);
    }

    /**
     * 审核服务（通过/拒绝）
     */
    @PostMapping("/approve")
    @Operation(summary = "审核服务", description = "审核用户创建的服务")
    public Result<Void> approveService(@RequestBody @Validated ServiceApprovalDTO dto) {
        log.info("管理员审核服务，参数：{}", dto);
        appointmentService.adminApproveService(dto);
        return Result.success();
    }

    /**
     * 修改服务状态（启用/禁用）
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "修改服务状态", description = "启用或禁用服务")
    public Result<Void> updateServiceStatus(
            @Parameter(description = "服务ID") @PathVariable Long id,
            @Parameter(description = "状态：0-禁用 1-启用") @RequestParam Integer status) {
        log.info("管理员修改服务状态，ID：{}，状态：{}", id, status);
        appointmentService.adminUpdateServiceStatus(id, status);
        return Result.success();
    }

    /**
     * 设置热门服务
     */
    @PutMapping("/{id}/hot")
    @Operation(summary = "设置热门服务", description = "设置或取消热门服务")
    public Result<Void> setHotService(
            @Parameter(description = "服务ID") @PathVariable Long id,
            @Parameter(description = "是否热门：0-否 1-是") @RequestParam Integer isHot) {
        log.info("管理员设置热门服务，ID：{}，是否热门：{}", id, isHot);
        appointmentService.adminSetHotService(id, isHot);
        return Result.success();
    }

    /**
     * 删除服务
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除服务", description = "删除预约服务")
    public Result<Void> deleteService(
            @Parameter(description = "服务ID") @PathVariable Long id) {
        log.info("管理员删除服务，ID：{}", id);
        appointmentService.adminDeleteService(id);
        return Result.success();
    }

    /**
     * 获取待审核服务数量
     */
    @GetMapping("/pending/count")
    @Operation(summary = "获取待审核服务数量", description = "获取等待审核的服务数量")
    public Result<Integer> getPendingServiceCount() {
        Integer count = appointmentService.adminGetPendingServiceCount();
        return Result.success(count);
    }
    
    /**
     * 获取服务统计数据
     */
    @GetMapping("/{id}/statistics")
    @Operation(summary = "获取服务统计数据", description = "获取服务的统计数据（预约数、收入等）")
    public Result<com.zheng.aicommunitybackend.domain.vo.ServiceStatisticsVO> getServiceStatistics(
            @Parameter(description = "服务ID") @PathVariable Long id) {
        log.info("管理员获取服务统计数据，ID：{}", id);
        com.zheng.aicommunitybackend.domain.vo.ServiceStatisticsVO statistics = appointmentService.adminGetServiceStatistics(id);
        return Result.success(statistics);
    }
    
    /**
     * 获取服务最近预约列表
     */
    @GetMapping("/{id}/recent-orders")
    @Operation(summary = "获取服务最近预约列表", description = "获取服务的最近预约记录")
    public Result<java.util.List<com.zheng.aicommunitybackend.domain.vo.ServiceRecentOrderVO>> getServiceRecentOrders(
            @Parameter(description = "服务ID") @PathVariable Long id,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("管理员获取服务最近预约列表，ID：{}，限制：{}", id, limit);
        java.util.List<com.zheng.aicommunitybackend.domain.vo.ServiceRecentOrderVO> recentOrders = appointmentService.adminGetServiceRecentOrders(id, limit);
        return Result.success(recentOrders);
    }
}

