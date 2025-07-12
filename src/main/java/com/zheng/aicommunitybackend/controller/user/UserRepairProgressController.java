package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.RepairProgressDTO;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.RepairProgressVO;
import com.zheng.aicommunitybackend.service.RepairOrdersService;
import com.zheng.aicommunitybackend.service.RepairProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端报修进度跟踪控制器
 */
@RestController
@RequestMapping("/user/repair-progress")
@Tag(name = "用户端报修进度接口", description = "用户端物业报修进度跟踪相关接口")
public class UserRepairProgressController {

    @Autowired
    private RepairProgressService repairProgressService;
    
    @Autowired
    private RepairOrdersService repairOrdersService;
    
    /**
     * 添加维修进度记录
     */
    @PostMapping
    @Operation(summary = "添加维修进度", description = "用户添加维修进度记录")
    public Result<Long> addProgress(@RequestBody @Validated RepairProgressDTO dto) {
        Long userId = UserContext.getUserId();
        // 验证用户是否有权限操作此工单
        repairOrdersService.validateUserOrder(dto.getOrderId(), userId);
        Long progressId = repairProgressService.addProgress(dto, userId, 1); // 1-用户类型
        return Result.success(progressId);
    }
    
    /**
     * 获取维修进度列表
     */
    @GetMapping("/list/{orderId}")
    @Operation(summary = "获取进度列表", description = "获取工单的维修进度列表")
    public Result<List<RepairProgressVO>> getProgressList(
            @Parameter(description = "工单ID") @PathVariable Long orderId) {
        Long userId = UserContext.getUserId();
        // 验证用户是否有权限查看此工单
        repairOrdersService.validateUserOrder(orderId, userId);
        List<RepairProgressVO> progressList = repairProgressService.getOrderProgressList(orderId);
        return Result.success(progressList);
    }
    
    /**
     * 获取维修进度详情
     */
    @GetMapping("/{progressId}")
    @Operation(summary = "获取进度详情", description = "获取维修进度记录详情")
    public Result<RepairProgressVO> getProgressDetail(
            @Parameter(description = "进度记录ID") @PathVariable Long progressId) {
        RepairProgressVO vo = repairProgressService.getProgressById(progressId);
        Long userId = UserContext.getUserId();
        // 验证用户是否有权限查看此工单的进度
        repairOrdersService.validateUserOrder(vo.getOrderId(), userId);
        return Result.success(vo);
    }
} 