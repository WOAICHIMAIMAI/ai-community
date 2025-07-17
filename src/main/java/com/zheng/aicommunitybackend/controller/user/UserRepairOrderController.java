package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.RepairFeedbackDTO;
import com.zheng.aicommunitybackend.domain.dto.RepairOrderCreateDTO;
import com.zheng.aicommunitybackend.domain.dto.RepairOrderPageQuery;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.RepairOrderVO;
import com.zheng.aicommunitybackend.service.RepairOrdersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



/**
 * 用户报修工单控制器
 */
@RestController
@RequestMapping("/user/repair-orders")
@Tag(name = "用户报修工单接口", description = "用户物业报修工单相关接口")
public class UserRepairOrderController {

    @Autowired
    private RepairOrdersService repairOrdersService;
    
    /**
     * 创建报修工单
     */
    @PostMapping
    @Operation(summary = "创建报修工单", description = "用户创建物业报修工单")
    public Result<String> createRepairOrder(@RequestBody @Validated RepairOrderCreateDTO dto) {
        Long userId = UserContext.getUserId();
        String orderNumber = repairOrdersService.createRepairOrder(dto, userId);
        return Result.success(orderNumber);
    }
    
    /**
     * 取消报修工单
     */
    @PutMapping("/cancel/{orderId}")
    @Operation(summary = "取消报修工单", description = "用户取消报修工单")
    public Result<Boolean> cancelRepairOrder(
            @Parameter(description = "工单ID") @PathVariable Long orderId) {
        Long userId = UserContext.getUserId();
        boolean result = repairOrdersService.cancelRepairOrder(orderId, userId);
        return Result.success(result);
    }
    
    /**
     * 分页查询用户的报修工单
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询报修工单", description = "分页查询用户的报修工单列表")
    public Result<PageResult<RepairOrderVO>> pageRepairOrders(@Validated RepairOrderPageQuery query) {
        Long userId = UserContext.getUserId();
        PageResult<RepairOrderVO> pageResult = repairOrdersService.pageUserRepairOrders(query, userId);
        return Result.success(pageResult);
    }
    
    /**
     * 获取报修工单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "获取工单详情", description = "获取报修工单详情")
    public Result<RepairOrderVO> getRepairOrderDetail(
            @Parameter(description = "工单ID") @PathVariable Long orderId) {
        Long userId = UserContext.getUserId();
        RepairOrderVO vo = repairOrdersService.getRepairOrderDetail(orderId, userId);
        return Result.success(vo);
    }
    

    
    /**
     * 提交维修评价
     */
    @PostMapping("/feedback")
    @Operation(summary = "提交评价", description = "提交维修工单评价")
    public Result<Boolean> submitFeedback(@RequestBody @Validated RepairFeedbackDTO dto) {
        Long userId = UserContext.getUserId();
        boolean result = repairOrdersService.submitFeedback(dto, userId);
        return Result.success(result);
    }
} 