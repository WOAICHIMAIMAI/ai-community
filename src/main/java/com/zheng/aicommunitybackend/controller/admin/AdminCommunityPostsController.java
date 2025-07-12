package com.zheng.aicommunitybackend.controller.admin;

import com.zheng.aicommunitybackend.domain.dto.AdminPostPageQuery;
import com.zheng.aicommunitybackend.domain.dto.PostStatusDTO;
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
 * 管理端帖子相关接口
 */
@RestController
@RequestMapping("/admin/posts")
@Slf4j
@Tag(name = "管理员帖子接口", description = "管理员帖子相关接口")
public class AdminCommunityPostsController {

    private final CommunityPostsService communityPostsService;

    public AdminCommunityPostsController(CommunityPostsService communityPostsService) {
        this.communityPostsService = communityPostsService;
    }

    /**
     * 管理员分页查询帖子列表
     *
     * @param query 分页查询参数
     * @return 帖子列表
     */
    @GetMapping("/list")
    @Operation(summary = "管理员查询帖子列表", description = "管理员分页查询帖子列表，支持多种筛选条件")
    public Result<PageResult> adminListPosts(AdminPostPageQuery query) {
        log.info("管理员查询帖子列表: {}", query);
        PageResult pageResult = communityPostsService.adminListPosts(query);
        return Result.success(pageResult);
    }

    /**
     * 管理员获取帖子详情
     *
     * @param postId 帖子ID
     * @return 帖子详情
     */
    @GetMapping("/{postId}")
    @Operation(summary = "管理员获取帖子详情", description = "管理员获取帖子详细信息")
    public Result<PostVO> getPostDetail(@PathVariable @Parameter(description = "帖子ID") Long postId) {
        log.info("管理员获取帖子详情: {}", postId);
        PostVO postVO = communityPostsService.getPostDetail(postId);
        return Result.success(postVO);
    }

    /**
     * 管理员更新帖子状态
     *
     * @param postStatusDTO 帖子状态信息
     * @return 更新结果
     */
    @PutMapping("/status")
    @Operation(summary = "管理员更新帖子状态", description = "管理员更新帖子状态、置顶、精华等属性")
    public Result<Void> updatePostStatus(@RequestBody PostStatusDTO postStatusDTO) {
        log.info("管理员更新帖子状态: {}", postStatusDTO);
        boolean success = communityPostsService.updatePostStatus(postStatusDTO);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 管理员设置帖子置顶状态
     *
     * @param postId 帖子ID
     * @param isTop  是否置顶：0-否 1-是
     * @return 更新结果
     */
    @PutMapping("/top/{postId}")
    @Operation(summary = "管理员设置帖子置顶状态", description = "管理员设置帖子是否置顶")
    public Result<Void> setPostTop(
            @PathVariable @Parameter(description = "帖子ID") Long postId,
            @RequestParam @Parameter(description = "是否置顶：0-否 1-是") Integer isTop) {
        log.info("管理员设置帖子置顶状态: postId={}, isTop={}", postId, isTop);
        
        PostStatusDTO dto = new PostStatusDTO();
        dto.setPostId(postId);
        dto.setIsTop(isTop);
        
        boolean success = communityPostsService.updatePostStatus(dto);
        return success ? Result.success() : Result.error("设置失败");
    }

    /**
     * 管理员设置帖子精华状态
     *
     * @param postId    帖子ID
     * @param isEssence 是否精华：0-否 1-是
     * @return 更新结果
     */
    @PutMapping("/essence/{postId}")
    @Operation(summary = "管理员设置帖子精华状态", description = "管理员设置帖子是否精华")
    public Result<Void> setPostEssence(
            @PathVariable @Parameter(description = "帖子ID") Long postId,
            @RequestParam @Parameter(description = "是否精华：0-否 1-是") Integer isEssence) {
        log.info("管理员设置帖子精华状态: postId={}, isEssence={}", postId, isEssence);
        
        PostStatusDTO dto = new PostStatusDTO();
        dto.setPostId(postId);
        dto.setIsEssence(isEssence);
        
        boolean success = communityPostsService.updatePostStatus(dto);
        return success ? Result.success() : Result.error("设置失败");
    }

    /**
     * 管理员物理删除帖子
     *
     * @param postId 帖子ID
     * @return 删除结果
     */
    @DeleteMapping("/{postId}")
    @Operation(summary = "管理员物理删除帖子", description = "管理员物理删除帖子（慎用）")
    public Result<Void> adminDeletePost(@PathVariable @Parameter(description = "帖子ID") Long postId) {
        log.info("管理员物理删除帖子: {}", postId);
        boolean success = communityPostsService.adminDeletePost(postId);
        return success ? Result.success() : Result.error("删除失败");
    }
} 