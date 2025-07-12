package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.LikeRecordDTO;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.LikeStatusVO;
import com.zheng.aicommunitybackend.service.LikeRecordsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户点赞控制器
 */
@RestController
@RequestMapping("/user/likes")
@Tag(name = "用户点赞接口", description = "用户点赞相关接口")
public class UserLikeController {

    @Autowired
    private LikeRecordsService likeRecordsService;
    
    /**
     * 点赞或取消点赞
     */
    @PostMapping
    @Operation(summary = "点赞或取消点赞", description = "点赞或取消点赞帖子/评论")
    public Result<LikeStatusVO> likeOrUnlike(@RequestBody @Validated LikeRecordDTO dto) {
        Long userId = UserContext.getUserId();
        LikeStatusVO result = likeRecordsService.likeOrUnlike(dto, userId);
        return Result.success(result);
    }
    
    /**
     * 获取点赞状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取点赞状态", description = "获取用户对特定帖子/评论的点赞状态")
    public Result<LikeStatusVO> getLikeStatus(
            @Parameter(description = "点赞类型：1-帖子 2-评论") @RequestParam Integer type,
            @Parameter(description = "目标ID") @RequestParam Long targetId) {
        Long userId = UserContext.getUserId();
        LikeStatusVO result = likeRecordsService.getLikeStatus(type, targetId, userId);
        return Result.success(result);
    }
    
    /**
     * 获取用户点赞列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户点赞列表", description = "获取用户点赞过的帖子/评论ID列表")
    public Result<List<Long>> getUserLikedList(
            @Parameter(description = "点赞类型：1-帖子 2-评论") @RequestParam Integer type) {
        Long userId = UserContext.getUserId();
        List<Long> result = likeRecordsService.getUserLikedList(userId, type);
        return Result.success(result);
    }
    
    /**
     * 批量获取点赞状态
     */
    @PostMapping("/batch-status")
    @Operation(summary = "批量获取点赞状态", description = "批量获取用户对多个帖子/评论的点赞状态")
    public Result<List<LikeStatusVO>> batchGetLikeStatus(
            @Parameter(description = "点赞类型：1-帖子 2-评论") @RequestParam Integer type,
            @RequestBody List<Long> targetIds) {
        Long userId = UserContext.getUserId();
        List<LikeStatusVO> result = likeRecordsService.batchGetLikeStatus(type, targetIds, userId);
        return Result.success(result);
    }
} 