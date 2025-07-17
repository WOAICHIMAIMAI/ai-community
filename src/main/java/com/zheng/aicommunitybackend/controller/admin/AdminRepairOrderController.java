package com.zheng.aicommunitybackend.controller.admin;

import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.AdminRepairOrderAssignDTO;
import com.zheng.aicommunitybackend.domain.dto.AdminRepairOrderPageQuery;
import com.zheng.aicommunitybackend.domain.dto.AdminRepairOrderUpdateDTO;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.AdminRepairOrderVO;
import com.zheng.aicommunitybackend.domain.vo.RepairStatsVO;
import com.zheng.aicommunitybackend.service.RepairOrdersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



/**
 * 管理端报修工单控制器
 */
@RestController
@RequestMapping("/admin/repair-orders")
@Tag(name = "管理端报修工单接口", description = "管理端物业报修工单相关接口")
public class AdminRepairOrderController {

    @Autowired
    private RepairOrdersService repairOrdersService;
    
    /**
     * 分页查询报修工单
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询报修工单", description = "管理员分页查询报修工单列表")
    public Result<PageResult<AdminRepairOrderVO>> pageRepairOrders(@Validated AdminRepairOrderPageQuery query) {
        PageResult<AdminRepairOrderVO> pageResult = repairOrdersService.pageAdminRepairOrders(query);
        return Result.success(pageResult);
    }
    
    /**
     * 获取报修工单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "获取工单详情", description = "管理员获取报修工单详情")
    public Result<AdminRepairOrderVO> getRepairOrderDetail(
            @Parameter(description = "工单ID") @PathVariable Long orderId) {
        AdminRepairOrderVO vo = repairOrdersService.getAdminRepairOrderDetail(orderId);
        return Result.success(vo);
    }
    
    /**
     * 分配报修工单
     */
    @PutMapping("/assign")
    @Operation(summary = "分配报修工单", description = "将工单分配给维修工")
    public Result<Boolean> assignRepairOrder(@RequestBody @Validated AdminRepairOrderAssignDTO dto) {
        boolean result = repairOrdersService.assignRepairWorker(dto);
        return Result.success(result);
    }
    
    /**
     * 更新工单状态
     */
    @PutMapping("/status")
    @Operation(summary = "更新工单状态", description = "更新报修工单状态")
    public Result<Boolean> updateOrderStatus(@RequestBody @Validated AdminRepairOrderUpdateDTO dto) {
        boolean result = repairOrdersService.updateRepairOrder(dto);
        return Result.success(result);
    }
    
    /**
     * 获取工单统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取工单统计", description = "获取报修工单统计数据")
    public Result<RepairStatsVO> getRepairStats() {
        RepairStatsVO stats = repairOrdersService.getRepairStats();
        return Result.success(stats);
    }
} 