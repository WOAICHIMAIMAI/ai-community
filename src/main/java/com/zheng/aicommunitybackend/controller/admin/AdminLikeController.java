package com.zheng.aicommunitybackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zheng.aicommunitybackend.domain.dto.AdminLikePageQuery;
import com.zheng.aicommunitybackend.domain.entity.LikeRecords;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.AdminLikeVO;
import com.zheng.aicommunitybackend.service.LikeRecordsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

/**
 * 管理员点赞控制器
 */
@RestController
@RequestMapping("/admin/likes")
@Tag(name = "管理员点赞接口", description = "管理员点赞相关接口")
public class AdminLikeController {

    @Autowired
    private LikeRecordsService likeRecordsService;
    
    /**
     * 分页查询点赞记录
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询点赞记录", description = "管理员分页查询点赞记录列表")
    public Result<PageResult<AdminLikeVO>> page(AdminLikePageQuery query) {
        PageResult<AdminLikeVO> pageResult = likeRecordsService.getLikesPage(query);
        return Result.success(pageResult);
    }
    
    /**
     * 获取点赞详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取点赞详情", description = "根据ID获取点赞记录详情")
    public Result<AdminLikeVO> getById(
            @Parameter(description = "点赞记录ID") @PathVariable Long id) {
        AdminLikePageQuery query = new AdminLikePageQuery();
        query.setId(id);
        query.setPage(1);
        query.setPageSize(1);
        
        PageResult<AdminLikeVO> pageResult = likeRecordsService.getLikesPage(query);
        
        return Result.success(pageResult.getRecords().isEmpty() ? null : pageResult.getRecords().get(0));
    }
    
    /**
     * 按用户查询点赞记录
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "按用户查询点赞记录", description = "根据用户ID查询点赞记录")
    public Result<PageResult<AdminLikeVO>> getByUserId(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        AdminLikePageQuery query = new AdminLikePageQuery();
        query.setUserId(userId);
        query.setPage(page);
        query.setPageSize(pageSize);
        
        PageResult<AdminLikeVO> pageResult = likeRecordsService.getLikesPage(query);
        return Result.success(pageResult);
    }
    
    /**
     * 按内容查询点赞记录
     */
    @GetMapping("/target")
    @Operation(summary = "按内容查询点赞记录", description = "根据目标类型和ID查询点赞记录")
    public Result<PageResult<AdminLikeVO>> getByTarget(
            @Parameter(description = "目标类型：1-帖子 2-评论") @RequestParam Integer targetType,
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        AdminLikePageQuery query = new AdminLikePageQuery();
        query.setTargetType(targetType);
        query.setTargetId(targetId);
        query.setPage(page);
        query.setPageSize(pageSize);
        
        PageResult<AdminLikeVO> pageResult = likeRecordsService.getLikesPage(query);
        return Result.success(pageResult);
    }
    
    /**
     * 删除点赞记录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除点赞记录", description = "根据ID删除点赞记录")
    public Result<Boolean> delete(
            @Parameter(description = "点赞记录ID") @PathVariable Long id) {
        boolean result = likeRecordsService.removeById(id);
        return Result.success(result);
    }
    
    /**
     * 批量删除点赞记录
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除点赞记录", description = "批量删除点赞记录")
    public Result<Boolean> batchDelete(@RequestBody List<Long> ids) {
        boolean result = likeRecordsService.removeByIds(ids);
        return Result.success(result);
    }
    
    /**
     * 获取点赞统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取点赞统计信息", description = "获取点赞总数、今日新增等统计信息")
    public Result<Map<String, Object>> getStatistics(
            @Parameter(description = "点赞类型：1-帖子 2-评论") @RequestParam(required = false) Integer targetType,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {
        
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<LikeRecords> totalQuery = new LambdaQueryWrapper<>();
        if (targetType != null) {
            totalQuery.eq(LikeRecords::getTargetType, targetType);
        }
        result.put("totalLikes", likeRecordsService.count(totalQuery));
        
        // 今日数据
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime tomorrow = today.plusDays(1);
        LambdaQueryWrapper<LikeRecords> todayQuery = new LambdaQueryWrapper<>();
        if (targetType != null) {
            todayQuery.eq(LikeRecords::getTargetType, targetType);
        }
        todayQuery.between(LikeRecords::getCreateTime, today, tomorrow);
        result.put("todayLikes", likeRecordsService.count(todayQuery));
        
        // 昨日数据
        LocalDateTime yesterday = today.minusDays(1);
        LambdaQueryWrapper<LikeRecords> yesterdayQuery = new LambdaQueryWrapper<>();
        if (targetType != null) {
            yesterdayQuery.eq(LikeRecords::getTargetType, targetType);
        }
        yesterdayQuery.between(LikeRecords::getCreateTime, yesterday, today);
        result.put("yesterdayLikes", likeRecordsService.count(yesterdayQuery));
        
        return Result.success(result);
    }
}
