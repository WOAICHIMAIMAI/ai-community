package com.zheng.aicommunitybackend.controller.admin;

import com.zheng.aicommunitybackend.domain.dto.AdminCommentPageQuery;
import com.zheng.aicommunitybackend.domain.dto.CommentStatusDTO;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.AdminCommentVO;
import com.zheng.aicommunitybackend.service.PostCommentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员评论控制器
 */
@RestController
@RequestMapping("/admin/comments")
@Tag(name = "管理员评论接口", description = "管理员评论相关接口")
public class AdminCommentController {

    @Autowired
    private PostCommentsService postCommentsService;
    
    /**
     * 分页查询评论
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询评论", description = "管理员分页查询评论列表")
    public Result<PageResult<AdminCommentVO>> pageComments(AdminCommentPageQuery query) {
        PageResult<AdminCommentVO> pageResult = postCommentsService.adminPageComments(query);
        return Result.success(pageResult);
    }
    
    /**
     * 更新评论状态
     */
    @PutMapping("/status")
    @Operation(summary = "更新评论状态", description = "更新评论的显示/隐藏状态")
    public Result<Boolean> updateStatus(@RequestBody @Validated CommentStatusDTO dto) {
        boolean result = postCommentsService.updateCommentStatus(dto);
        return Result.success(result);
    }
    
    /**
     * 批量删除评论
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除评论", description = "批量删除评论")
    public Result<Boolean> batchDelete(@RequestBody List<Long> ids) {
        boolean result = postCommentsService.batchDeleteComments(ids);
        return Result.success(result);
    }
    
    /**
     * 删除单个评论
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除评论", description = "删除单个评论")
    public Result<Boolean> delete(
            @Parameter(description = "评论ID") @PathVariable Long commentId) {
        List<Long> ids = List.of(commentId);
        boolean result = postCommentsService.batchDeleteComments(ids);
        return Result.success(result);
    }
    
    /**
     * 获取评论统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取评论统计信息", description = "获取评论总数、今日新增、隐藏数量等统计信息")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = postCommentsService.getCommentStatistics();
        return Result.success(statistics);
    }
} 