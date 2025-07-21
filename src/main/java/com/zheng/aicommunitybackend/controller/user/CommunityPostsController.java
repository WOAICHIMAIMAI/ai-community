package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.domain.dto.PostDTO;
import com.zheng.aicommunitybackend.domain.dto.PostPageQuery;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.PostVO;
import com.zheng.aicommunitybackend.service.CommunityPostsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端帖子相关接口
 */
@RestController
@RequestMapping("/user/posts")
@Slf4j
@Tag(name = "帖子接口", description = "用户帖子相关接口")
public class CommunityPostsController {

    private final CommunityPostsService communityPostsService;

    public CommunityPostsController(CommunityPostsService communityPostsService) {
        this.communityPostsService = communityPostsService;
    }

    /**
     * 创建帖子
     *
     * @param postDTO 帖子信息
     * @return 帖子ID
     */
    @PostMapping
    @Operation(summary = "创建帖子", description = "用户创建新帖子")
    public Result<Long> createPost(@RequestBody PostDTO postDTO) {
        log.info("创建帖子: {}", postDTO.getTitle());
        Long postId = communityPostsService.createPost(postDTO);
        return Result.success(postId);
    }

    /**
     * 更新帖子
     *
     * @param postDTO 帖子信息
     * @return 更新结果
     */
    @PutMapping
    @Operation(summary = "更新帖子", description = "用户更新已有帖子")
    public Result<Void> updatePost(@RequestBody PostDTO postDTO) {
        log.info("更新帖子: {}", postDTO.getId());
        communityPostsService.updatePost(postDTO);
        return Result.success();
    }

    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     * @return 删除结果
     */
    @DeleteMapping("/{postId}")
    @Operation(summary = "删除帖子", description = "用户删除帖子")
    public Result<Void> deletePost(@PathVariable @Parameter(description = "帖子ID") Long postId) {
        log.info("删除帖子: {}", postId);
        communityPostsService.deletePost(postId);
        return Result.success();
    }

    /**
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @return 帖子详情
     */
    @GetMapping("/{postId}")
    @Operation(summary = "获取帖子详情", description = "获取帖子详细信息")
    public Result<PostVO> getPostDetail(@PathVariable @Parameter(description = "帖子ID") Long postId) {
        log.info("获取帖子详情: {}", postId);
        PostVO postVO = communityPostsService.getPostDetail(postId);
        return Result.success(postVO);
    }

    /**
     * 分页查询帖子列表
     *
     * @param query 分页查询参数
     * @return 帖子列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询帖子列表", description = "分页查询帖子列表")
    public Result<PageResult> listPosts(PostPageQuery query) {
        log.info("查询帖子列表: {}", query);
        PageResult pageResult = communityPostsService.listPosts(query);
        return Result.success(pageResult);
    }

    /**
     * 获取当前用户发布的帖子
     *
     * @param query 分页查询参数
     * @return 帖子列表
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的帖子", description = "获取当前用户发布的帖子列表")
    public Result<PageResult> listUserPosts(PostPageQuery query) {
        log.info("获取我的帖子: {}", query);
        PageResult pageResult = communityPostsService.listUserPosts(query);
        return Result.success(pageResult);
    }

    /**
     * 根据用户ID获取用户发布的帖子
     *
     * @param userId 用户ID
     * @param query 分页查询参数
     * @return 帖子列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取指定用户的帖子", description = "根据用户ID获取该用户发布的帖子列表")
    public Result<PageResult> getUserPosts(
            @PathVariable @Parameter(description = "用户ID") Long userId,
            PostPageQuery query) {
        log.info("获取用户{}的帖子列表: {}", userId, query);
        PageResult pageResult = communityPostsService.getUserPostsByUserId(userId, query);
        return Result.success(pageResult);
    }
}
