package com.zheng.aicommunitybackend.controller.admin;

import com.zheng.aicommunitybackend.domain.dto.RepairWorkerDTO;
import com.zheng.aicommunitybackend.domain.dto.RepairWorkerPageQuery;
import com.zheng.aicommunitybackend.domain.dto.RepairWorkerStatusDTO;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.RepairOrderVO;
import com.zheng.aicommunitybackend.domain.vo.RepairWorkerVO;
import com.zheng.aicommunitybackend.domain.vo.WorkerStatsVO;
import com.zheng.aicommunitybackend.service.RepairWorkersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端维修工控制器
 */
@RestController
@RequestMapping("/admin/workers")
@Tag(name = "管理端维修工接口", description = "管理端维修工相关接口")
public class AdminRepairWorkerController {

    @Autowired
    private RepairWorkersService repairWorkersService;
    
    /**
     * 分页查询维修工列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询维修工", description = "管理员分页查询维修工列表")
    public Result<PageResult<RepairWorkerVO>> pageWorkers(@Validated RepairWorkerPageQuery query) {
        PageResult<RepairWorkerVO> pageResult = repairWorkersService.pageWorkers(query);
        return Result.success(pageResult);
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
     * 添加维修工
     */
    @PostMapping
    @Operation(summary = "添加维修工", description = "添加新维修工")
    public Result<Long> addWorker(@RequestBody @Validated RepairWorkerDTO dto) {
        Long workerId = repairWorkersService.addWorker(dto);
        return Result.success(workerId);
    }
    
    /**
     * 更新维修工信息
     */
    @PutMapping
    @Operation(summary = "更新维修工", description = "更新维修工信息")
    public Result<Boolean> updateWorker(@RequestBody @Validated RepairWorkerDTO dto) {
        if (dto.getId() == null) {
            return Result.error("维修工ID不能为空");
        }
        boolean result = repairWorkersService.updateWorker(dto);
        return Result.success(result);
    }
    
    /**
     * 删除维修工
     */
    @DeleteMapping("/{workerId}")
    @Operation(summary = "删除维修工", description = "删除维修工")
    public Result<Boolean> deleteWorker(
            @Parameter(description = "维修工ID") @PathVariable Long workerId) {
        boolean result = repairWorkersService.deleteWorker(workerId);
        return Result.success(result);
    }
    
    /**
     * 更新维修工工作状态
     */
    @PutMapping("/status")
    @Operation(summary = "更新工作状态", description = "更新维修工工作状态")
    public Result<Boolean> updateWorkerStatus(@RequestBody @Validated RepairWorkerStatusDTO dto) {
        boolean result = repairWorkersService.updateWorkerStatus(dto);
        return Result.success(result);
    }
    
    /**
     * 获取维修工绩效统计
     */
    @GetMapping("/{workerId}/stats")
    @Operation(summary = "获取绩效统计", description = "获取维修工绩效统计数据")
    public Result<WorkerStatsVO> getWorkerStats(
            @Parameter(description = "维修工ID") @PathVariable Long workerId) {
        WorkerStatsVO stats = repairWorkersService.getWorkerStats(workerId);
        return Result.success(stats);
    }
    
    /**
     * 获取维修工负责的工单列表
     */
    @GetMapping("/{workerId}/orders")
    @Operation(summary = "获取工单列表", description = "获取维修工负责的工单列表")
    public Result<List<RepairOrderVO>> getWorkerOrders(
            @Parameter(description = "维修工ID") @PathVariable Long workerId,
            @Parameter(description = "工单状态") @RequestParam(required = false) Integer status) {
        List<RepairOrderVO> orders = repairWorkersService.getWorkerOrders(workerId, status);
        return Result.success(orders);
    }
    
    /**
     * 手动更新维修工评分
     */
    @PutMapping("/{workerId}/rating")
    @Operation(summary = "更新维修工评分", description = "手动更新维修工评分")
    public Result<Boolean> updateWorkerRating(
            @Parameter(description = "维修工ID") @PathVariable Long workerId) {
        repairWorkersService.updateWorkerRating(workerId);
        return Result.success(true);
    }
} 