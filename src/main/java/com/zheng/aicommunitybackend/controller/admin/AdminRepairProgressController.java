package com.zheng.aicommunitybackend.controller.admin;

import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.AdminProgressPageQuery;
import com.zheng.aicommunitybackend.domain.dto.RepairProgressDTO;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.RepairProgressVO;
import com.zheng.aicommunitybackend.service.RepairProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端报修进度跟踪控制器
 */
@RestController
@RequestMapping("/admin/repair-progress")
@Tag(name = "管理端报修进度接口", description = "管理端物业报修进度跟踪相关接口")
public class AdminRepairProgressController {

    @Autowired
    private RepairProgressService repairProgressService;
    
    /**
     * 分页查询维修进度记录
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询进度记录", description = "管理员分页查询维修进度记录")
    public Result<PageResult<RepairProgressVO>> pageProgressRecords(@Validated AdminProgressPageQuery query) {
        PageResult<RepairProgressVO> pageResult = repairProgressService.pageProgressRecords(query);
        return Result.success(pageResult);
    }
    
    /**
     * 获取维修进度详情
     */
    @GetMapping("/{progressId}")
    @Operation(summary = "获取进度详情", description = "获取维修进度记录详情")
    public Result<RepairProgressVO> getProgressDetail(
            @Parameter(description = "进度记录ID") @PathVariable Long progressId) {
        RepairProgressVO vo = repairProgressService.getProgressById(progressId);
        return Result.success(vo);
    }
    
    /**
     * 添加维修进度记录
     */
    @PostMapping
    @Operation(summary = "添加维修进度", description = "管理员添加维修进度记录")
    public Result<Long> addProgress(@RequestBody @Validated RepairProgressDTO dto) {
        Long adminId = UserContext.getUserId();
        Long progressId = repairProgressService.addProgress(dto, adminId, 4); // 4-管理员类型
        return Result.success(progressId);
    }
    
    /**
     * 获取工单的维修进度列表
     */
    @GetMapping("/list/{orderId}")
    @Operation(summary = "获取进度列表", description = "获取工单的维修进度记录列表")
    public Result<List<RepairProgressVO>> getOrderProgressList(
            @Parameter(description = "工单ID") @PathVariable Long orderId) {
        List<RepairProgressVO> progressList = repairProgressService.getOrderProgressList(orderId);
        return Result.success(progressList);
    }
    
    /**
     * 删除维修进度记录
     */
    @DeleteMapping("/{progressId}")
    @Operation(summary = "删除进度记录", description = "删除维修进度记录")
    public Result<Boolean> deleteProgress(
            @Parameter(description = "进度记录ID") @PathVariable Long progressId) {
        Long adminId = UserContext.getUserId();
        boolean result = repairProgressService.deleteProgress(progressId, adminId);
        return Result.success(result);
    }
} 