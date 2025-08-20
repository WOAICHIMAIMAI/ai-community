package com.zheng.aicommunitybackend.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zheng.aicommunitybackend.annotation.RateLimit;
import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.dto.RedPacketActivityCreateDTO;
import com.zheng.aicommunitybackend.domain.dto.RedPacketActivityOperateDTO;
import com.zheng.aicommunitybackend.domain.dto.RedPacketActivityQueryDTO;
import com.zheng.aicommunitybackend.domain.dto.RedPacketRecordQueryDTO;
import com.zheng.aicommunitybackend.domain.vo.RedPacketActivityVO;
import com.zheng.aicommunitybackend.domain.vo.RedPacketRecordVO;
import com.zheng.aicommunitybackend.service.RedPacketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端红包控制器
 */
@RestController
@RequestMapping("/admin/red-packet")
@Slf4j
@Tag(name = "管理端红包接口", description = "管理端红包相关接口")
public class AdminRedPacketController {

    @Autowired
    private RedPacketService redPacketService;

    /**
     * 创建红包活动
     */
    @PostMapping("/activities")
    // 临时注释限流，避免Redis Lua脚本问题
    /*@RateLimit(
        key = "create_activity",
        limitType = RateLimit.LimitType.USER,
        windowSize = 60,       // 1分钟窗口
        maxRequests = 5,       // 每分钟最多5次
        message = "创建活动过于频繁，请稍后再试"
    )*/
    @Operation(summary = "创建红包活动", description = "管理员创建红包活动")
    public Result<Long> createActivity(@Valid @RequestBody RedPacketActivityCreateDTO createDTO) {

        try {
            log.info("收到创建红包活动请求: {}", createDTO);

            Long creatorId = UserContext.getUserId();
            log.info("创建红包活动，创建者ID: {}, 活动名称: {}", creatorId, createDTO.getActivityName());

            // 临时处理：如果获取不到用户ID，使用默认测试用户ID
            if (creatorId == null) {
                log.warn("UserContext中获取不到用户ID，使用默认测试用户ID: 1");
                creatorId = 1L; // 临时使用测试用户ID
            }

            // 额外的参数校验
            if (createDTO.getTotalAmount() == null || createDTO.getTotalAmount() <= 0) {
                return Result.error("红包总金额必须大于0");
            }

            if (createDTO.getTotalCount() == null || createDTO.getTotalCount() <= 0) {
                return Result.error("红包数量必须大于0");
            }

            // 检查平均金额
            double avgAmount = createDTO.getTotalAmount() / createDTO.getTotalCount();
            if (avgAmount < 0.01) {
                return Result.error("平均红包金额不能小于0.01元");
            }

            Long activityId = redPacketService.createActivity(createDTO, creatorId);
            log.info("红包活动创建成功，活动ID: {}", activityId);
            return Result.success(activityId);

        } catch (Exception e) {
            log.error("创建红包活动失败", e);
            return Result.error("创建活动失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询红包活动列表
     */
    @PostMapping("/activities/list")
    @Operation(summary = "分页查询红包活动列表", description = "管理端分页查询红包活动")
    public Result<IPage<RedPacketActivityVO>> getActivityPage(
            @Valid @RequestBody RedPacketActivityQueryDTO queryDTO) {

        log.info("分页查询红包活动，查询条件: {}", queryDTO);

        IPage<RedPacketActivityVO> page = redPacketService.getActivityPage(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取红包活动详情
     */
    @GetMapping("/activities/{id}")
    @Operation(summary = "获取红包活动详情", description = "根据ID获取红包活动详情")
    public Result<RedPacketActivityVO> getActivityDetail(
            @Parameter(description = "活动ID") @PathVariable Long id) {
        
        log.info("获取红包活动详情，活动ID: {}", id);
        
        RedPacketActivityVO activity = redPacketService.getActivityDetail(id, null);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        
        return Result.success(activity);
    }

    /**
     * 活动操作（开始、结束、取消）
     */
    @PostMapping("/activities/operate")
    @Operation(summary = "活动操作", description = "管理员操作红包活动（开始、结束、取消）")
    public Result<Boolean> operateActivity(@Valid @RequestBody RedPacketActivityOperateDTO operateDTO) {

        Long operatorId = UserContext.getUserId();
        log.info("操作活动，活动ID: {}, 操作类型: {}, 操作者: {}",
                operateDTO.getActivityId(), operateDTO.getOperation(), operatorId);

        if (operatorId == null) {
            return Result.error("用户未登录");
        }

        boolean success = false;
        String operation = operateDTO.getOperation();
        Long activityId = operateDTO.getActivityId();

        switch (operation) {
            case "start":
                success = redPacketService.startActivity(activityId, operatorId);
                break;
            case "end":
                success = redPacketService.endActivity(activityId, operatorId);
                break;
            case "cancel":
                success = redPacketService.cancelActivity(activityId, operatorId);
                break;
            default:
                return Result.error("不支持的操作类型");
        }

        if (success) {
            return Result.success(true);
        } else {
            return Result.error("操作失败，请检查活动状态");
        }
    }



    /**
     * 获取活动的抢红包记录
     */
    @PostMapping("/activities/records")
    @Operation(summary = "获取活动抢红包记录", description = "分页获取指定活动的抢红包记录")
    public Result<IPage<RedPacketRecordVO>> getActivityRecords(@Valid @RequestBody RedPacketRecordQueryDTO queryDTO) {

        log.info("获取活动抢红包记录，查询条件: {}", queryDTO);

        if (queryDTO.getActivityId() == null) {
            return Result.error("活动ID不能为空");
        }

        IPage<RedPacketRecordVO> records = redPacketService.getActivityRecordPage(
                queryDTO.getActivityId(), queryDTO.getPage(), queryDTO.getSize());
        return Result.success(records);
    }

    /**
     * 获取活动统计信息
     */
    @GetMapping("/activities/{id}/stats")
    @Operation(summary = "获取活动统计信息", description = "获取指定活动的统计信息")
    public Result<Map<String, Object>> getActivityStats(
            @Parameter(description = "活动ID") @PathVariable Long id) {
        
        log.info("获取活动统计信息，活动ID: {}", id);
        
        Map<String, Object> stats = redPacketService.getActivityStats(id);
        return Result.success(stats);
    }

    /**
     * 预加载活动到Redis
     */
    @PostMapping("/activities/{id}/preload")
    @Operation(summary = "预加载活动到Redis", description = "手动预加载活动数据到Redis")
    public Result<Boolean> preloadActivity(@Parameter(description = "活动ID") @PathVariable Long id) {

        Long operatorId = UserContext.getUserId();
        log.info("预加载活动到Redis，活动ID: {}, 操作者: {}", id, operatorId);

        if (operatorId == null) {
            return Result.error("用户未登录");
        }

        boolean success = redPacketService.preloadActivityToRedis(id);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error("预加载失败");
        }
    }

    /**
     * 清理活动Redis数据
     */
    @PostMapping("/activities/{id}/clear-cache")
    @Operation(summary = "清理活动Redis数据", description = "手动清理活动的Redis缓存数据")
    public Result<Boolean> clearActivityCache(@Parameter(description = "活动ID") @PathVariable Long id) {

        Long operatorId = UserContext.getUserId();
        log.info("清理活动Redis数据，活动ID: {}, 操作者: {}", id, operatorId);

        if (operatorId == null) {
            return Result.error("用户未登录");
        }

        boolean success = redPacketService.clearActivityFromRedis(id);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error("清理缓存失败");
        }
    }

    /**
     * 处理未更新账户的记录
     */
    @PostMapping("/process-unprocessed")
    @Operation(summary = "处理未更新账户的记录", description = "补偿处理未更新账户的抢红包记录")
    public Result<Integer> processUnprocessedRecords(
            @Parameter(description = "处理数量限制") @RequestParam(defaultValue = "100") Integer limit) {

        Long operatorId = UserContext.getUserId();
        log.info("处理未更新账户的记录，限制数量: {}, 操作者: {}", limit, operatorId);

        if (operatorId == null) {
            return Result.error("用户未登录");
        }

        int processedCount = redPacketService.processUnprocessedRecords(limit);
        return Result.success(processedCount);
    }
}
