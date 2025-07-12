package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.RepairOrderVO;
import com.zheng.aicommunitybackend.domain.vo.RepairWorkerVO;
import com.zheng.aicommunitybackend.service.RepairWorkersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端维修工控制器
 */
@RestController
@RequestMapping("/user/workers")
@Tag(name = "用户端维修工接口", description = "用户端维修工相关接口")
public class UserRepairWorkerController {

    @Autowired
    private RepairWorkersService repairWorkersService;
    
    /**
     * 获取可用维修工列表
     */
    @GetMapping("/available")
    @Operation(summary = "获取可用维修工", description = "根据服务类型获取可用维修工列表")
    public Result<List<RepairWorkerVO>> getAvailableWorkers(
            @Parameter(description = "服务类型") @RequestParam(required = false) String serviceType) {
        List<RepairWorkerVO> workers = repairWorkersService.getAvailableWorkersByType(serviceType);
        return Result.success(workers);
    }
    
    /**
     * 获取维修工详情
     */
    @GetMapping("/{workerId}")
    @Operation(summary = "获取维修工详情", description = "获取维修工详细信息")
    public Result<RepairWorkerVO> getWorkerDetail(
            @Parameter(description = "维修工ID") @PathVariable Long workerId) {
        RepairWorkerVO worker = repairWorkersService.getWorkerById(workerId);
        return Result.success(worker);
    }
    
    /**
     * 获取维修工历史工单
     */
    @GetMapping("/{workerId}/orders")
    @Operation(summary = "获取维修工历史工单", description = "获取维修工已完成的工单列表")
    public Result<List<RepairOrderVO>> getWorkerCompletedOrders(
            @Parameter(description = "维修工ID") @PathVariable Long workerId) {
        // 只查询已完成的工单（状态为3）
        List<RepairOrderVO> orders = repairWorkersService.getWorkerOrders(workerId, 3);
        return Result.success(orders);
    }
} 