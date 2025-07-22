package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.common.Result;
import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.AppointmentCreateDTO;
import com.zheng.aicommunitybackend.domain.dto.AppointmentPageQuery;
import com.zheng.aicommunitybackend.domain.dto.AppointmentRateDTO;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.AppointmentOrderVO;
import com.zheng.aicommunitybackend.domain.vo.AppointmentServiceVO;
import com.zheng.aicommunitybackend.domain.vo.AppointmentRecommendVO;
import com.zheng.aicommunitybackend.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 用户预约服务控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/appointment")
@Tag(name = "用户预约服务接口", description = "用户预约服务相关接口")
public class UserAppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 获取所有服务类型
     */
    @GetMapping("/services")
    @Operation(summary = "获取所有服务类型", description = "获取所有可预约的服务类型列表")
    public Result<List<AppointmentServiceVO>> getAllServices() {
        List<AppointmentServiceVO> services = appointmentService.getAllServices();
        return Result.success(services);
    }

    /**
     * 获取热门服务
     */
    @GetMapping("/services/hot")
    @Operation(summary = "获取热门服务", description = "获取热门服务列表")
    public Result<List<AppointmentServiceVO>> getHotServices() {
        List<AppointmentServiceVO> services = appointmentService.getHotServices();
        return Result.success(services);
    }

    /**
     * 获取推荐服务
     */
    @GetMapping("/services/recommend")
    @Operation(summary = "获取推荐服务", description = "基于用户历史获取推荐服务")
    public Result<List<AppointmentRecommendVO>> getRecommendServices() {
        Long userId = UserContext.getUserId();
        List<AppointmentRecommendVO> services = appointmentService.getRecommendServices(userId);
        return Result.success(services);
    }

    /**
     * 创建预约
     */
    @PostMapping
    @Operation(summary = "创建预约", description = "用户创建预约订单")
    public Result<String> createAppointment(@RequestBody @Validated AppointmentCreateDTO dto) {
        Long userId = UserContext.getUserId();
        String orderNo = appointmentService.createAppointment(dto, userId);
        return Result.success(orderNo);
    }

    /**
     * 分页查询用户预约记录
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询预约记录", description = "分页查询用户的预约记录")
    public Result<PageResult<AppointmentOrderVO>> getUserAppointmentPage(@Validated AppointmentPageQuery query) {
        Long userId = UserContext.getUserId();
        PageResult<AppointmentOrderVO> result = appointmentService.getUserAppointmentPage(query, userId);
        return Result.success(result);
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取预约详情", description = "根据ID获取预约详情")
    public Result<AppointmentOrderVO> getAppointmentDetail(
            @Parameter(description = "预约ID") @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        AppointmentOrderVO detail = appointmentService.getAppointmentDetail(id, userId);
        return Result.success(detail);
    }

    /**
     * 取消预约
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消预约", description = "用户取消预约")
    public Result<Boolean> cancelAppointment(
            @Parameter(description = "预约ID") @PathVariable Long id,
            @RequestBody(required = false) String reason) {
        Long userId = UserContext.getUserId();
        Boolean result = appointmentService.cancelAppointment(id, userId, reason);
        return Result.success(result);
    }

    /**
     * 改期预约
     */
    @PutMapping("/{id}/reschedule")
    @Operation(summary = "改期预约", description = "用户改期预约")
    public Result<Boolean> rescheduleAppointment(
            @Parameter(description = "预约ID") @PathVariable Long id,
            @RequestBody Date appointmentTime) {
        Long userId = UserContext.getUserId();
        Boolean result = appointmentService.rescheduleAppointment(id, userId, appointmentTime);
        return Result.success(result);
    }

    /**
     * 评价服务
     */
    @PostMapping("/rate")
    @Operation(summary = "评价服务", description = "用户评价预约服务")
    public Result<Boolean> rateAppointment(@RequestBody @Validated AppointmentRateDTO dto) {
        Long userId = UserContext.getUserId();
        Boolean result = appointmentService.rateAppointment(dto, userId);
        return Result.success(result);
    }

    /**
     * 获取最近预约记录
     */
    @GetMapping("/recent")
    @Operation(summary = "获取最近预约记录", description = "获取用户最近的预约记录")
    public Result<List<AppointmentOrderVO>> getRecentAppointments(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "3") Integer limit) {
        Long userId = UserContext.getUserId();
        List<AppointmentOrderVO> appointments = appointmentService.getRecentAppointments(userId, limit);
        return Result.success(appointments);
    }

    /**
     * 获取可用时间段
     */
    @GetMapping("/time-slots")
    @Operation(summary = "获取可用时间段", description = "获取指定服务类型和日期的可用时间段")
    public Result<List<String>> getAvailableTimeSlots(
            @Parameter(description = "服务类型") @RequestParam String serviceType,
            @Parameter(description = "日期") @RequestParam String date) {
        List<String> timeSlots = appointmentService.getAvailableTimeSlots(serviceType, date);
        return Result.success(timeSlots);
    }

    /**
     * 获取服务人员列表
     */
    @GetMapping("/workers")
    @Operation(summary = "获取服务人员列表", description = "根据服务类型获取可用的服务人员")
    public Result<List<Object>> getServiceWorkers(
            @Parameter(description = "服务类型") @RequestParam String serviceType) {
        List<Object> workers = appointmentService.getServiceWorkers(serviceType);
        return Result.success(workers);
    }

    /**
     * 获取预约统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取预约统计信息", description = "获取用户的预约统计信息")
    public Result<Object> getAppointmentStats() {
        Long userId = UserContext.getUserId();
        Object stats = appointmentService.getAppointmentStats(userId);
        return Result.success(stats);
    }
}
