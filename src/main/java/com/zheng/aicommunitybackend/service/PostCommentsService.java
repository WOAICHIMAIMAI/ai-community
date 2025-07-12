package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.AdminCommentPageQuery;
import com.zheng.aicommunitybackend.domain.dto.CommentCreateDTO;
import com.zheng.aicommunitybackend.domain.dto.CommentPageQuery;
import com.zheng.aicommunitybackend.domain.dto.CommentStatusDTO;
import com.zheng.aicommunitybackend.domain.entity.PostComments;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.AdminCommentVO;
import com.zheng.aicommunitybackend.domain.vo.CommentVO;

import java.util.List;
import java.util.Map;

/**
* @author ZhengJJ
* @description 针对表【post_comments(帖子评论表)】的数据库操作Service
* @createDate 2025-07-10 11:03:49
*/
public interface PostCommentsService extends IService<PostComments> {

    /**
     * 创建评论
     * @param dto 评论创建参数
     * @param userId 当前用户ID
     * @return 评论ID
     */
    Long createComment(CommentCreateDTO dto, Long userId);

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param userId 当前用户ID
     * @return 是否成功
     */
    boolean deleteComment(Long commentId, Long userId);
    
    /**
     * 分页查询帖子的一级评论
     * @param query 查询参数
     * @param userId 当前用户ID，用于判断是否是楼主
     * @return 评论分页结果
     */
    PageResult<CommentVO> pageComments(CommentPageQuery query, Long userId);
    
    /**
     * 获取评论的回复列表（二级评论）
     * @param commentId 一级评论ID
     * @param userId 当前用户ID，用于判断是否是楼主
     * @return 回复列表
     */
    List<CommentVO> getCommentReplies(Long commentId, Long userId);
    
    /**
     * 获取帖子评论总数
     * @param postId 帖子ID
     * @return 评论总数
     */
    Integer countCommentsByPostId(Long postId);
    
    /**
     * 管理端分页查询评论
     * @param query 查询参数
     * @return 评论分页结果
     */
    PageResult<AdminCommentVO> adminPageComments(AdminCommentPageQuery query);
    
    /**
     * 管理端更新评论状态
     * @param dto 评论状态更新参数
     * @return 是否成功
     */
    boolean updateCommentStatus(CommentStatusDTO dto);
    
    /**
     * 管理端批量删除评论
     * @param ids 评论ID列表
     * @return 是否成功
     */
    boolean batchDeleteComments(List<Long> ids);
    
    /**
     * 管理端获取评论统计信息
     * @return 统计信息，包含总评论数、今日新增评论数、待审核评论数等
     */
    Map<String, Object> getCommentStatistics();
}
