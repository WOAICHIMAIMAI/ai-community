package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.CommentCreateDTO;
import com.zheng.aicommunitybackend.domain.dto.CommentPageQuery;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.CommentVO;
import com.zheng.aicommunitybackend.service.PostCommentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户评论控制器
 */
@RestController
@RequestMapping("/user/comments")
@Tag(name = "用户评论接口", description = "用户评论相关接口")
public class UserCommentController {

    @Autowired
    private PostCommentsService postCommentsService;
    
    /**
     * 创建评论
     */
    @PostMapping
    @Operation(summary = "创建评论", description = "创建帖子评论或回复评论")
    public Result<Long> createComment(@RequestBody @Validated CommentCreateDTO dto) {
        Long userId = UserContext.getUserId();
        Long commentId = postCommentsService.createComment(dto, userId);
        return Result.success(commentId);
    }
    
    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除评论", description = "删除自己发布的评论")
    public Result<Boolean> deleteComment(
            @Parameter(description = "评论ID") @PathVariable Long commentId) {
        Long userId = UserContext.getUserId();
        boolean result = postCommentsService.deleteComment(commentId, userId);
        return Result.success(result);
    }
    
    /**
     * 分页查询帖子的一级评论
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询帖子的一级评论", description = "分页查询帖子的一级评论列表")
    public Result<PageResult<CommentVO>> pageComments(@Validated CommentPageQuery query) {
        Long userId = UserContext.getUserId();
        PageResult<CommentVO> pageResult = postCommentsService.pageComments(query, userId);
        return Result.success(pageResult);
    }
    
    /**
     * 获取评论的回复列表
     */
    @GetMapping("/replies/{commentId}")
    @Operation(summary = "获取评论的回复列表", description = "获取一级评论下的所有回复")
    public Result<List<CommentVO>> getCommentReplies(
            @Parameter(description = "评论ID") @PathVariable Long commentId) {
        Long userId = UserContext.getUserId();
        List<CommentVO> replies = postCommentsService.getCommentReplies(commentId, userId);
        return Result.success(replies);
    }
    
    /**
     * 获取帖子评论数量
     */
    @GetMapping("/count/{postId}")
    @Operation(summary = "获取评论数量", description = "获取指定帖子的评论总数")
    public Result<Integer> countComments(
            @Parameter(description = "帖子ID") @PathVariable Long postId) {
        Integer count = postCommentsService.countCommentsByPostId(postId);
        return Result.success(count);
    }
} 