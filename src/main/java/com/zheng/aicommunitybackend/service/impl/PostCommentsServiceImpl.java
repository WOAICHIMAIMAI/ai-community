package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.dto.AdminCommentPageQuery;
import com.zheng.aicommunitybackend.domain.dto.CommentCreateDTO;
import com.zheng.aicommunitybackend.domain.dto.CommentPageQuery;
import com.zheng.aicommunitybackend.domain.dto.CommentStatusDTO;
import com.zheng.aicommunitybackend.domain.entity.CommunityPosts;
import com.zheng.aicommunitybackend.domain.entity.PostComments;
import com.zheng.aicommunitybackend.domain.entity.Users;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.AdminCommentVO;
import com.zheng.aicommunitybackend.domain.vo.CommentVO;
import com.zheng.aicommunitybackend.exception.BaseException;
import com.zheng.aicommunitybackend.mapper.CommunityPostsMapper;
import com.zheng.aicommunitybackend.mapper.PostCommentsMapper;
import com.zheng.aicommunitybackend.mapper.UsersMapper;
import com.zheng.aicommunitybackend.service.PostCommentsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author ZhengJJ
* @description 针对表【post_comments(帖子评论表)】的数据库操作Service实现
* @createDate 2025-07-10 11:03:49
*/
@Service
public class PostCommentsServiceImpl extends ServiceImpl<PostCommentsMapper, PostComments>
    implements PostCommentsService {

    @Autowired
    private CommunityPostsMapper communityPostsMapper;
    
    @Autowired
    private UsersMapper usersMapper;

    @Override
    @Transactional
    public Long createComment(CommentCreateDTO dto, Long userId) {
        // 1. 校验帖子是否存在
        CommunityPosts post = communityPostsMapper.selectById(dto.getPostId());
        if (post == null) {
            throw new BaseException("帖子不存在");
        }
        
        // 2. 校验父评论
        if (dto.getParentId() != null) {
            PostComments parentComment = this.getById(dto.getParentId());
            if (parentComment == null || !parentComment.getPostId().equals(dto.getPostId())) {
                throw new BaseException("父评论不存在");
            }
            
            // 确保只能回复一级评论
            if (parentComment.getParentId() != 0) {
                throw new BaseException("只能回复一级评论");
            }
        }
        
        // 3. 创建评论
        PostComments comment = new PostComments();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1); // 默认显示
        comment.setCreateTime(new Date());
        this.save(comment);
        
        return comment.getId();
    }

    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        // 1. 查询评论是否存在
        PostComments comment = this.getById(commentId);
        if (comment == null) {
            throw new BaseException("评论不存在");
        }
        
        // 2. 校验权限（只能删除自己的评论）
        if (!comment.getUserId().equals(userId)) {
            throw new BaseException("无权限删除他人评论");
        }
        
        // 3. 如果是一级评论，则同时删除其下所有的二级评论
        if (comment.getParentId() == 0) {
            LambdaQueryWrapper<PostComments> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PostComments::getParentId, commentId);
            this.remove(wrapper);
        }
        
        // 4. 删除当前评论
        return this.removeById(commentId);
    }

    @Override
    public PageResult<CommentVO> pageComments(CommentPageQuery query, Long userId) {
        // 1. 查询帖子是否存在
        CommunityPosts post = communityPostsMapper.selectById(query.getPostId());
        if (post == null) {
            throw new BaseException("帖子不存在");
        }
        
        // 2. 构建查询条件 - 只查询一级评论
        LambdaQueryWrapper<PostComments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostComments::getPostId, query.getPostId())
                .eq(PostComments::getParentId, 0) // 只查询一级评论
                .eq(PostComments::getStatus, 1); // 只查询显示状态的评论
        
        // 3. 按创建时间排序
        queryWrapper.orderByDesc(PostComments::getCreateTime);
        
        // 4. 分页查询
        Page<PostComments> page = new Page<>(query.getPage(), query.getPageSize());
        page = this.page(page, queryWrapper);
        
        // 5. 数据转换
        List<CommentVO> commentVOList = convertToCommentVOList(page.getRecords(), post, userId);
        
        // 6. 返回结果
        return new PageResult<>(page.getTotal(), commentVOList);
    }
    
    @Override
    public List<CommentVO> getCommentReplies(Long commentId, Long userId) {
        // 1. 查询评论是否存在
        PostComments parentComment = this.getById(commentId);
        if (parentComment == null) {
            throw new BaseException("评论不存在");
        }
        
        // 确保是一级评论
        if (parentComment.getParentId() != 0) {
            throw new BaseException("只能查询一级评论的回复");
        }
        
        // 2. 查询帖子信息
        CommunityPosts post = communityPostsMapper.selectById(parentComment.getPostId());
        if (post == null) {
            throw new BaseException("帖子不存在");
        }
        
        // 3. 查询二级评论
        LambdaQueryWrapper<PostComments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostComments::getParentId, commentId)
                .eq(PostComments::getStatus, 1)
                .orderByAsc(PostComments::getCreateTime); // 按时间正序排列
        
        List<PostComments> replies = this.list(queryWrapper);
        
        // 4. 数据转换
        return convertToCommentVOList(replies, post, userId);
    }

    @Override
    public Integer countCommentsByPostId(Long postId) {
        LambdaQueryWrapper<PostComments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostComments::getPostId, postId)
                .eq(PostComments::getStatus, 1);
        return Math.toIntExact(this.count(wrapper));
    }
    
    @Override
    public Map<Long, Integer> batchCountCommentsByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // 批量查询所有帖子的评论（包括一级和二级）
        LambdaQueryWrapper<PostComments> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PostComments::getPostId, postIds)
                .eq(PostComments::getStatus, 1);
        
        List<PostComments> comments = this.list(wrapper);
        
        // 按帖子ID分组统计评论数量
        Map<Long, Integer> commentCountMap = new HashMap<>();
        for (Long postId : postIds) {
            commentCountMap.put(postId, 0);
        }
        
        for (PostComments comment : comments) {
            Long postId = comment.getPostId();
            commentCountMap.put(postId, commentCountMap.get(postId) + 1);
        }
        
        return commentCountMap;
    }
    
    @Override
    public PageResult<AdminCommentVO> adminPageComments(AdminCommentPageQuery query) {
        // 1. 构建查询条件
        LambdaQueryWrapper<PostComments> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据帖子ID查询
        if (query.getPostId() != null) {
            queryWrapper.eq(PostComments::getPostId, query.getPostId());
        }
        
        // 根据用户ID查询
        if (query.getUserId() != null) {
            queryWrapper.eq(PostComments::getUserId, query.getUserId());
        }
        
        // 根据评论内容关键词查询
        if (StringUtils.hasText(query.getKeyword())) {
            queryWrapper.like(PostComments::getContent, query.getKeyword());
        }
        
        // 根据状态查询
        if (query.getStatus() != null) {
            queryWrapper.eq(PostComments::getStatus, query.getStatus());
        }
        
        // 2. 按创建时间排序
        String sortField = StringUtils.hasText(query.getSortField()) ? query.getSortField() : "createTime";
        boolean isAsc = "asc".equalsIgnoreCase(query.getSortOrder());
        
        if ("createTime".equals(sortField)) {
            queryWrapper.orderBy(true, isAsc, PostComments::getCreateTime);
        } else if ("likeCount".equals(sortField)) {
            queryWrapper.orderBy(true, isAsc, PostComments::getLikeCount);
        } else {
            queryWrapper.orderByDesc(PostComments::getCreateTime);
        }
        
        // 3. 分页查询
        Page<PostComments> page = new Page<>(query.getPage(), query.getPageSize());
        page = this.page(page, queryWrapper);
        
        // 4. 数据转换
        List<AdminCommentVO> commentVOList = new ArrayList<>();
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            // 4.1 获取所有评论用户ID和帖子ID
            List<Long> userIds = page.getRecords().stream()
                    .map(PostComments::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<Long> postIds = page.getRecords().stream()
                    .map(PostComments::getPostId)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 4.2 批量查询用户信息
            LambdaQueryWrapper<Users> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.in(Users::getId, userIds);
            List<Users> userList = usersMapper.selectList(userWrapper);
            Map<Long, Users> userMap = userList.stream()
                    .collect(Collectors.toMap(Users::getId, user -> user));
            
            // 4.3 批量查询帖子信息
            LambdaQueryWrapper<CommunityPosts> postWrapper = new LambdaQueryWrapper<>();
            postWrapper.in(CommunityPosts::getId, postIds);
            List<CommunityPosts> postList = communityPostsMapper.selectList(postWrapper);
            Map<Long, CommunityPosts> postMap = postList.stream()
                    .collect(Collectors.toMap(CommunityPosts::getId, post -> post));
            
            // 4.4 转换评论数据
            for (PostComments comment : page.getRecords()) {
                AdminCommentVO vo = new AdminCommentVO();
                BeanUtils.copyProperties(comment, vo);
                
                // 设置用户信息
                Users user = userMap.get(comment.getUserId());
                if (user != null) {
                    vo.setNickname(user.getNickname());
                    vo.setAvatar(user.getAvatarUrl());
                }
                
                // 设置帖子信息
                CommunityPosts post = postMap.get(comment.getPostId());
                if (post != null) {
                    vo.setPostTitle(post.getTitle());
                }
                
                // 如果是一级评论，统计回复数量
                if (comment.getParentId() == 0) {
                    LambdaQueryWrapper<PostComments> replyWrapper = new LambdaQueryWrapper<>();
                    replyWrapper.eq(PostComments::getParentId, comment.getId())
                            .eq(PostComments::getStatus, 1);
                    int replyCount = Math.toIntExact(this.count(replyWrapper));
                    vo.setReplyCount(replyCount);
                } else {
                    vo.setReplyCount(0);
                }
                
                commentVOList.add(vo);
            }
        }
        
        return new PageResult<>(page.getTotal(), commentVOList);
    }
    
    @Override
    @Transactional
    public boolean updateCommentStatus(CommentStatusDTO dto) {
        // 1. 查询评论是否存在
        PostComments comment = this.getById(dto.getCommentId());
        if (comment == null) {
            throw new BaseException("评论不存在");
        }
        
        // 2. 更新评论状态
        comment.setStatus(dto.getStatus());
        return this.updateById(comment);
    }
    
    @Override
    @Transactional
    public boolean batchDeleteComments(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        
        // 1. 查询一级评论
        List<Long> rootCommentIds = new ArrayList<>();
        for (Long id : ids) {
            PostComments comment = this.getById(id);
            if (comment != null && comment.getParentId() == 0) {
                rootCommentIds.add(id);
            }
        }
        
        // 2. 删除一级评论下的所有回复
        if (!rootCommentIds.isEmpty()) {
            LambdaQueryWrapper<PostComments> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(PostComments::getParentId, rootCommentIds);
            this.remove(wrapper);
        }
        
        // 3. 删除选中的评论
        return this.removeByIds(ids);
    }
    
    @Override
    public Map<String, Object> getCommentStatistics() {
        Map<String, Object> result = new HashMap<>(4);
        
        // 1. 总评论数
        long totalCount = this.count();
        result.put("totalCount", totalCount);
        
        // 2. 今日新增评论数
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date today = calendar.getTime();
        
        LambdaQueryWrapper<PostComments> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(PostComments::getCreateTime, today);
        long todayCount = this.count(todayWrapper);
        result.put("todayCount", todayCount);
        
        // 3. 隐藏评论数
        LambdaQueryWrapper<PostComments> hiddenWrapper = new LambdaQueryWrapper<>();
        hiddenWrapper.eq(PostComments::getStatus, 0);
        long hiddenCount = this.count(hiddenWrapper);
        result.put("hiddenCount", hiddenCount);
        
        // 4. 显示评论数
        LambdaQueryWrapper<PostComments> visibleWrapper = new LambdaQueryWrapper<>();
        visibleWrapper.eq(PostComments::getStatus, 1);
        long visibleCount = this.count(visibleWrapper);
        result.put("visibleCount", visibleCount);
        
        return result;
    }
    
    /**
     * 将评论实体列表转换为评论VO列表
     * 
     * @param commentList 评论实体列表
     * @param post 帖子信息
     * @param userId 当前用户ID
     * @return 评论VO列表
     */
    private List<CommentVO> convertToCommentVOList(List<PostComments> commentList, CommunityPosts post, Long userId) {
        List<CommentVO> commentVOList = new ArrayList<>();
        if (commentList == null || commentList.isEmpty()) {
            return commentVOList;
        }
        
        // 1. 获取所有评论用户ID
        List<Long> userIds = commentList.stream()
                .map(PostComments::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 2. 批量查询用户信息
        LambdaQueryWrapper<Users> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.in(Users::getId, userIds);
        List<Users> userList = usersMapper.selectList(userWrapper);
        Map<Long, Users> userMap = userList.stream()
                .collect(Collectors.toMap(Users::getId, user -> user));
        
        // 3. 转换评论数据
        for (PostComments comment : commentList) {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);
            
            // 设置用户信息
            Users user = userMap.get(comment.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatarUrl());
            }
            
            // 设置是否是楼主
            vo.setIsAuthor(comment.getUserId().equals(post.getUserId()));
            
            // 如果是一级评论，统计回复数量
            if (comment.getParentId() == 0) {
                LambdaQueryWrapper<PostComments> replyWrapper = new LambdaQueryWrapper<>();
                replyWrapper.eq(PostComments::getParentId, comment.getId())
                        .eq(PostComments::getStatus, 1);
                int replyCount = Math.toIntExact(this.count(replyWrapper));
                vo.setReplyCount(replyCount);
            } else {
                vo.setReplyCount(0);
            }
            
            commentVOList.add(vo);
        }
        
        return commentVOList;
    }
}




