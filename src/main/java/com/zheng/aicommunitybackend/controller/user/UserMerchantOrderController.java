package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.common.Result;
import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.AppointmentPageQuery;
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
 * 商家服务订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/merchant/orders")
@Tag(name = "商家服务订单接口", description = "商家查看和管理服务订单")
public class UserMerchantOrderController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 分页查询商家服务订单
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询服务订单", description = "商家分页查询自己服务的订单列表")
    public Result<PageResult<AppointmentOrderVO>> getMerchantOrderPage(@Validated AppointmentPageQuery query) {
        Long userId = UserContext.getUserId();
        log.info("商家查询服务订单列表，用户ID：{}，参数：{}", userId, query);
        PageResult<AppointmentOrderVO> result = appointmentService.getMerchantOrderPage(query, userId);
        return Result.success(result);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情", description = "商家获取订单详情")
    public Result<AppointmentOrderVO> getMerchantOrderDetail(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = UserContext.getUserId();
        log.info("商家获取订单详情，用户ID：{}，订单ID：{}", userId, orderId);
        AppointmentOrderVO order = appointmentService.getMerchantOrderDetail(orderId, userId);
        return Result.success(order);
    }

    /**
     * 获取订单统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取订单统计", description = "商家获取订单统计信息")
    public Result<Map<String, Object>> getMerchantOrderStats() {
        Long userId = UserContext.getUserId();
        log.info("商家获取订单统计，用户ID：{}", userId);
        Map<String, Object> stats = appointmentService.getMerchantOrderStats(userId);
        return Result.success(stats);
    }

    /**
     * 确认接单
     */
    @PutMapping("/{orderId}/confirm")
    @Operation(summary = "确认接单", description = "商家确认接单")
    public Result<Boolean> confirmOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = UserContext.getUserId();
        log.info("商家确认接单，用户ID：{}，订单ID：{}", userId, orderId);
        Boolean result = appointmentService.merchantConfirmOrder(orderId, userId);
        return Result.success(result);
    }

    /**
     * 开始服务
     */
    @PutMapping("/{orderId}/start")
    @Operation(summary = "开始服务", description = "商家开始服务")
    public Result<Boolean> startService(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = UserContext.getUserId();
        log.info("商家开始服务，用户ID：{}，订单ID：{}", userId, orderId);
        Boolean result = appointmentService.merchantStartService(orderId, userId);
        return Result.success(result);
    }

    /**
     * 完成服务
     */
    @PutMapping("/{orderId}/finish")
    @Operation(summary = "完成服务", description = "商家完成服务")
    public Result<Boolean> finishService(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = UserContext.getUserId();
        log.info("商家完成服务，用户ID：{}，订单ID：{}", userId, orderId);
        Boolean result = appointmentService.merchantFinishService(orderId, userId);
        return Result.success(result);
    }

    /**
     * 拒绝订单
     */
    @PutMapping("/{orderId}/reject")
    @Operation(summary = "拒绝订单", description = "商家拒绝订单")
    public Result<Boolean> rejectOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            @Parameter(description = "拒绝原因") @RequestParam(required = false) String reason) {
        Long userId = UserContext.getUserId();
        log.info("商家拒绝订单，用户ID：{}，订单ID：{}，原因：{}", userId, orderId, reason);
        Boolean result = appointmentService.merchantRejectOrder(orderId, userId, reason);
        return Result.success(result);
    }
}

