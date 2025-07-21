package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.AdminPostPageQuery;
import com.zheng.aicommunitybackend.domain.dto.PostDTO;
import com.zheng.aicommunitybackend.domain.dto.PostPageQuery;
import com.zheng.aicommunitybackend.domain.dto.PostStatusDTO;
import com.zheng.aicommunitybackend.domain.entity.CommunityPosts;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.PostVO;

/**
* @author ZhengJJ
* @description 针对表【community_posts(社区帖子表)】的数据库操作Service
* @createDate 2025-07-10 11:03:49
*/
public interface CommunityPostsService extends IService<CommunityPosts> {
    
    /**
     * 创建帖子
     * 
     * @param postDTO 帖子信息
     * @return 创建的帖子ID
     */
    Long createPost(PostDTO postDTO);
    
    /**
     * 更新帖子
     * 
     * @param postDTO 帖子信息
     * @return 是否更新成功
     */
    boolean updatePost(PostDTO postDTO);
    
    /**
     * 删除帖子
     * 
     * @param postId 帖子ID
     * @return 是否删除成功
     */
    boolean deletePost(Long postId);
    
    /**
     * 获取帖子详情
     * 
     * @param postId 帖子ID
     * @return 帖子详情
     */
    PostVO getPostDetail(Long postId);
    
    /**
     * 分页查询帖子列表
     * 
     * @param query 分页查询参数
     * @return 帖子列表
     */
    PageResult listPosts(PostPageQuery query);
    
    /**
     * 获取当前用户发布的帖子列表
     * 
     * @param query 分页查询参数
     * @return 帖子列表
     */
    PageResult listUserPosts(PostPageQuery query);
    
    /**
     * 管理员分页查询帖子列表
     * 
     * @param query 管理员分页查询参数
     * @return 帖子列表
     */
    PageResult adminListPosts(AdminPostPageQuery query);
    
    /**
     * 管理员更新帖子状态
     * 
     * @param postStatusDTO 帖子状态信息
     * @return 是否更新成功
     */
    boolean updatePostStatus(PostStatusDTO postStatusDTO);
    
    /**
     * 管理员物理删除帖子
     * 
     * @param postId 帖子ID
     * @return 是否删除成功
     */
    boolean adminDeletePost(Long postId);
    
    /**
     * 根据用户ID获取用户发布的帖子列表
     * 
     * @param userId 用户ID
     * @param query 分页查询参数
     * @return 帖子列表
     */
    PageResult getUserPostsByUserId(Long userId, PostPageQuery query);
}
