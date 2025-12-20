package com.zheng.aicommunitybackend.controller.admin;

import com.zheng.aicommunitybackend.common.Result;
import com.zheng.aicommunitybackend.domain.dto.AppointmentPageQuery;
import com.zheng.aicommunitybackend.domain.dto.OrderStatusUpdateDTO;
import com.zheng.aicommunitybackend.domain.dto.WorkerAssignDTO;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.AppointmentOrderVO;
import com.zheng.aicommunitybackend.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员预约订单管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/appointment/orders")
@Tag(name = "管理员订单管理接口", description = "管理预约订单")
public class AdminAppointmentOrderController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 分页查询订单列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询订单列表", description = "管理员分页查询所有预约订单")
    public Result<PageResult<AppointmentOrderVO>> getOrderList(@Validated AppointmentPageQuery query) {
        log.info("管理员查询订单列表，参数：{}", query);
        PageResult<AppointmentOrderVO> result = appointmentService.adminGetOrderPage(query);
        return Result.success(result);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情", description = "根据ID获取订单详情")
    public Result<AppointmentOrderVO> getOrderDetail(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        log.info("管理员获取订单详情，ID：{}", id);
        AppointmentOrderVO order = appointmentService.adminGetOrderDetail(id);
        return Result.success(order);
    }

    /**
     * 确认订单
     */
    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认订单", description = "管理员确认预约订单")
    public Result<Void> confirmOrder(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        log.info("管理员确认订单，ID：{}", id);
        appointmentService.adminConfirmOrder(id);
        return Result.success();
    }

    /**
     * 分配服务人员
     */
    @PutMapping("/{id}/assign")
    @Operation(summary = "分配服务人员", description = "为订单分配服务人员")
    public Result<Void> assignWorker(@RequestBody @Validated WorkerAssignDTO dto) {
        log.info("管理员分配服务人员，参数：{}", dto);
        appointmentService.adminAssignWorker(dto);
        return Result.success();
    }

    /**
     * 修改订单状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "修改订单状态", description = "修改订单状态")
    public Result<Void> updateOrderStatus(@RequestBody @Validated OrderStatusUpdateDTO dto) {
        log.info("管理员修改订单状态，参数：{}", dto);
        appointmentService.adminUpdateOrderStatus(dto);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "管理员取消订单")
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "取消原因") @RequestParam String reason) {
        log.info("管理员取消订单，ID：{}，原因：{}", id, reason);
        appointmentService.adminCancelOrder(id, reason);
        return Result.success();
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单", description = "删除预约订单")
    public Result<Void> deleteOrder(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        log.info("管理员删除订单，ID：{}", id);
        appointmentService.adminDeleteOrder(id);
        return Result.success();
    }

    /**
     * 获取订单统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取订单统计", description = "获取订单各状态的统计信息")
    public Result<Map<String, Object>> getOrderStats() {
        Map<String, Object> stats = appointmentService.adminGetOrderStats();
        return Result.success(stats);
    }
}

