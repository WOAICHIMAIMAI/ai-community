package com.zheng.aicommunitybackend.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zheng.aicommunitybackend.annotation.RateLimit;
import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.dto.RedPacketGrabDTO;
import com.zheng.aicommunitybackend.domain.dto.RedPacketRecordQueryDTO;
import com.zheng.aicommunitybackend.domain.vo.RedPacketActivityListVO;
import com.zheng.aicommunitybackend.domain.vo.RedPacketActivityVO;
import com.zheng.aicommunitybackend.domain.vo.RedPacketGrabVO;
import com.zheng.aicommunitybackend.domain.vo.RedPacketRecordVO;
import com.zheng.aicommunitybackend.service.RedPacketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户端红包控制器
 */
@RestController
@RequestMapping("/user/red-packet")
@Slf4j
@Tag(name = "用户端红包接口", description = "用户端红包相关接口")
public class UserRedPacketController {

    @Autowired
    private RedPacketService redPacketService;

    /**
     * 获取进行中的红包活动列表
     */
    @GetMapping("/activities")
    @Operation(summary = "获取进行中的红包活动列表", description = "获取当前正在进行的红包活动")
    public Result<List<RedPacketActivityListVO>> getActiveActivities() {
        log.info("获取进行中的红包活动列表");
        Long userId = UserContext.getUserId();
        List<RedPacketActivityListVO> activities = redPacketService.getActiveActivitiesForUser(userId);
        return Result.success(activities);
    }

    /**
     * 获取红包活动详情
     */
    @GetMapping("/activities/{id}")
    @Operation(summary = "获取红包活动详情", description = "根据ID获取红包活动详情")
    public Result<RedPacketActivityVO> getActivityDetail(
            @Parameter(description = "活动ID") @PathVariable Long id) {

        Long userId = UserContext.getUserId();
        log.info("获取红包活动详情，活动ID: {}, 用户ID: {}", id, userId);

        RedPacketActivityVO activity = redPacketService.getActivityDetail(id, userId);
        if (activity == null) {
            return Result.error("活动不存在");
        }

        return Result.success(activity);
    }

    /**
     * 抢红包
     */
    @PostMapping("/grab")
    /*@RateLimit(
        key = "grab_red_packet",
        limitType = RateLimit.LimitType.USER,
        windowSize = 1,        // 1秒窗口
        maxRequests = 5,       // 临时调整为每秒最多5次，便于测试
        algorithm = RateLimit.Algorithm.FIXED_WINDOW,  // 临时改为固定窗口
        message = "抢红包过于频繁，请稍后再试"
    )
    @RateLimit(
        key = "grab_red_packet_global",
        limitType = RateLimit.LimitType.GLOBAL,
        windowSize = 1,        // 1秒窗口
        maxRequests = 5000,    // 全局每秒最多5000次
        algorithm = RateLimit.Algorithm.SLIDING_WINDOW,
        message = "系统繁忙，请稍后再试"
    )*/
    @Operation(summary = "抢红包", description = "用户抢红包")
    public Result<RedPacketGrabVO> grabRedPacket(@Valid @RequestBody RedPacketGrabDTO grabDTO) {

        Long userId = UserContext.getUserId();
        log.info("用户抢红包，用户ID: {}, 活动ID: {}", userId, grabDTO.getActivityId());

        if (userId == null) {
            return Result.error("用户未登录");
        }

        RedPacketGrabVO result = redPacketService.grabRedPacket(userId, grabDTO.getActivityId());
        return Result.success(result);
    }

    /**
     * 获取用户抢红包记录
     */
    @PostMapping("/records")
    @Operation(summary = "获取用户抢红包记录", description = "分页获取用户的抢红包记录")
    public Result<IPage<RedPacketRecordVO>> getUserRecords(@Valid @RequestBody RedPacketRecordQueryDTO queryDTO) {

        Long userId = UserContext.getUserId();
        log.info("获取用户抢红包记录，用户ID: {}, 查询条件: {}", userId, queryDTO);

        if (userId == null) {
            return Result.error("用户未登录");
        }

        IPage<RedPacketRecordVO> records = redPacketService.getUserRecordPage(
                userId, queryDTO.getActivityId(), queryDTO.getPage(), queryDTO.getSize());
        return Result.success(records);
    }

    /**
     * 获取用户抢红包统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取用户抢红包统计", description = "获取用户的抢红包统计信息")
    public Result<Map<String, Object>> getUserStats() {

        Long userId = UserContext.getUserId();
        log.info("获取用户抢红包统计，用户ID: {}", userId);

        if (userId == null) {
            return Result.error("用户未登录");
        }

        Map<String, Object> stats = redPacketService.getUserStats(userId);
        return Result.success(stats);
    }

    /**
     * 检查用户是否可以抢红包
     */
    @GetMapping("/check/{activityId}")
    @Operation(summary = "检查是否可以抢红包", description = "检查用户是否可以抢指定活动的红包")
    public Result<Map<String, Object>> checkCanGrab(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {

        Long userId = UserContext.getUserId();
        log.info("检查用户是否可以抢红包，用户ID: {}, 活动ID: {}", userId, activityId);

        if (userId == null) {
            return Result.error("用户未登录");
        }

        RedPacketActivityVO activity = redPacketService.getActivityDetail(activityId, userId);
        if (activity == null) {
            return Result.error("活动不存在");
        }

        Map<String, Object> result = Map.of(
            "canGrab", activity.getCanGrab() != null ? activity.getCanGrab() : false,
            "reason", activity.getCannotGrabReason() != null ? activity.getCannotGrabReason() : "",
            "hasGrabbed", activity.getHasGrabbed() != null ? activity.getHasGrabbed() : false,
            "userGrabbedAmount", activity.getUserGrabbedAmount() != null ? activity.getUserGrabbedAmount() : 0,
            "remainingCount", activity.getRemainingCount(),
            "remainingAmount", activity.getRemainingAmount()
        );

        return Result.success(result);
    }
}
