package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.AdminPostPageQuery;
import com.zheng.aicommunitybackend.domain.dto.PostDTO;
import com.zheng.aicommunitybackend.domain.dto.PostPageQuery;
import com.zheng.aicommunitybackend.domain.dto.PostStatusDTO;
import com.zheng.aicommunitybackend.domain.entity.CommunityPosts;
import com.zheng.aicommunitybackend.domain.entity.FavoriteRecords;
import com.zheng.aicommunitybackend.domain.entity.LikeRecords;
import com.zheng.aicommunitybackend.domain.entity.Users;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.PostVO;
import com.zheng.aicommunitybackend.exception.BaseException;
import com.zheng.aicommunitybackend.mapper.CommunityPostsMapper;
import com.zheng.aicommunitybackend.service.CommunityPostsService;
import com.zheng.aicommunitybackend.service.FavoriteRecordsService;
import com.zheng.aicommunitybackend.service.LikeRecordsService;
import com.zheng.aicommunitybackend.service.UsersService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author ZhengJJ
* @description 针对表【community_posts(社区帖子表)】的数据库操作Service实现
* @createDate 2025-07-10 11:03:49
*/
@Service
public class CommunityPostsServiceImpl extends ServiceImpl<CommunityPostsMapper, CommunityPosts>
    implements CommunityPostsService{

    private final UsersService usersService;
    private final LikeRecordsService likeRecordsService;
    private final FavoriteRecordsService favoriteRecordsService;

    public CommunityPostsServiceImpl(UsersService usersService, 
                                   LikeRecordsService likeRecordsService, 
                                   FavoriteRecordsService favoriteRecordsService) {
        this.usersService = usersService;
        this.likeRecordsService = likeRecordsService;
        this.favoriteRecordsService = favoriteRecordsService;
    }

    @Override
    @Transactional
    public Long createPost(PostDTO postDTO) {
        // 获取当前登录用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BaseException("请先登录");
        }
        
        // 检查参数
        if (postDTO == null) {
            throw new BaseException("参数不能为空");
        }
        if (!StringUtils.hasText(postDTO.getTitle())) {
            throw new BaseException("标题不能为空");
        }
        if (!StringUtils.hasText(postDTO.getContent())) {
            throw new BaseException("内容不能为空");
        }
        
        CommunityPosts post = new CommunityPosts();
        post.setUserId(userId);
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setCategory(postDTO.getCategory());
        
        // 处理图片
        if (!CollectionUtils.isEmpty(postDTO.getImages())) {
            post.setImages(String.join(",", postDTO.getImages()));
        }
        
        // 设置状态
        post.setStatus(postDTO.getStatus() != null ? postDTO.getStatus() : 1); // 默认为已发布
        
        // 设置初始值
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setCreateTime(new Date());
        post.setUpdateTime(new Date());
        
        // 保存到数据库
        this.save(post);
        
        return post.getId();
    }

    @Override
    @Transactional
    public boolean updatePost(PostDTO postDTO) {
        // 检查参数
        if (postDTO == null || postDTO.getId() == null) {
            throw new BaseException("参数不能为空");
        }
        
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BaseException("请先登录");
        }
        
        // 查询原帖子
        CommunityPosts post = this.getById(postDTO.getId());
        if (post == null) {
            throw new BaseException("帖子不存在");
        }
        
        // 检查是否是帖子作者
        if (!post.getUserId().equals(userId)) {
            throw new BaseException("只能修改自己的帖子");
        }
        
        // 更新帖子信息
        if (StringUtils.hasText(postDTO.getTitle())) {
            post.setTitle(postDTO.getTitle());
        }
        if (StringUtils.hasText(postDTO.getContent())) {
            post.setContent(postDTO.getContent());
        }
        if (StringUtils.hasText(postDTO.getCategory())) {
            post.setCategory(postDTO.getCategory());
        }
        
        // 处理图片
        if (!CollectionUtils.isEmpty(postDTO.getImages())) {
            post.setImages(String.join(",", postDTO.getImages()));
        }
        
        // 更新状态
        if (postDTO.getStatus() != null) {
            post.setStatus(postDTO.getStatus());
        }
        
        post.setUpdateTime(new Date());
        
        // 更新到数据库
        return this.updateById(post);
    }

    @Override
    @Transactional
    public boolean deletePost(Long postId) {
        if (postId == null) {
            throw new BaseException("帖子ID不能为空");
        }
        
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BaseException("请先登录");
        }
        
        // 查询原帖子
        CommunityPosts post = this.getById(postId);
        if (post == null) {
            throw new BaseException("帖子不存在");
        }
        
        // 检查是否是帖子作者
        if (!post.getUserId().equals(userId)) {
            throw new BaseException("只能删除自己的帖子");
        }
        
        // 逻辑删除
        post.setStatus(2); // 已删除状态
        post.setUpdateTime(new Date());
        
        return this.updateById(post);
    }

    @Override
    public PostVO getPostDetail(Long postId) {
        if (postId == null) {
            throw new BaseException("帖子ID不能为空");
        }
        
        // 查询帖子
        CommunityPosts post = this.getById(postId);
        if (post == null || post.getStatus() == 2) { // 帖子不存在或已删除
            throw new BaseException("帖子不存在或已删除");
        }
        
        // 更新浏览量
        post.setViewCount(post.getViewCount() + 1);
        this.updateById(post);
        
        // 转换为VO
        PostVO postVO = convertToPostVO(post);
        
        // 获取当前登录用户ID
        Long userId = UserContext.getUserId();
        if (userId != null) {
            // 查询当前用户是否点赞
            LambdaQueryWrapper<LikeRecords> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(LikeRecords::getUserId, userId)
                    .eq(LikeRecords::getTargetId, postId)
                    .eq(LikeRecords::getTargetType, 1); // 假设1表示帖子点赞
            boolean hasLiked = likeRecordsService.count(likeWrapper) > 0;
            postVO.setHasLiked(hasLiked);
            
            // 查询当前用户是否收藏
            LambdaQueryWrapper<FavoriteRecords> favoriteWrapper = new LambdaQueryWrapper<>();
            favoriteWrapper.eq(FavoriteRecords::getUserId, userId)
                    .eq(FavoriteRecords::getTargetId, postId)
                    .eq(FavoriteRecords::getTargetType, 1); // 假设1表示帖子收藏
            boolean hasFavorited = favoriteRecordsService.count(favoriteWrapper) > 0;
            postVO.setHasFavorited(hasFavorited);
        } else {
            postVO.setHasLiked(false);
            postVO.setHasFavorited(false);
        }
        
        return postVO;
    }

    @Override
    public PageResult listPosts(PostPageQuery query) {
        // 构建查询条件
        LambdaQueryWrapper<CommunityPosts> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(query.getStatus() != null, CommunityPosts::getStatus, query.getStatus()); // 只查询已发布的帖子
        
        // 分类条件
        if (StringUtils.hasText(query.getCategory())) {
            queryWrapper.eq(CommunityPosts::getCategory, query.getCategory());
        }
        
        // 关键字条件
        if (StringUtils.hasText(query.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper.like(CommunityPosts::getTitle, query.getKeyword())
                    .or()
                    .like(CommunityPosts::getContent, query.getKeyword()));
        }
        
        // 排序
        if (StringUtils.hasText(query.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(query.getSortOrder());
            switch (query.getSortField()) {
                case "createTime":
                    queryWrapper.orderByDesc(CommunityPosts::getIsTop) // 置顶帖子优先
                            .orderBy(true, isAsc, CommunityPosts::getCreateTime);
                    break;
                case "viewCount":
                    queryWrapper.orderByDesc(CommunityPosts::getIsTop) // 置顶帖子优先
                            .orderBy(true, isAsc, CommunityPosts::getViewCount);
                    break;
                case "likeCount":
                    queryWrapper.orderByDesc(CommunityPosts::getIsTop) // 置顶帖子优先
                            .orderBy(true, isAsc, CommunityPosts::getLikeCount);
                    break;
                case "commentCount":
                    queryWrapper.orderByDesc(CommunityPosts::getIsTop) // 置顶帖子优先
                            .orderBy(true, isAsc, CommunityPosts::getCommentCount);
                    break;
                default:
                    queryWrapper.orderByDesc(CommunityPosts::getIsTop) // 置顶帖子优先
                            .orderByDesc(CommunityPosts::getCreateTime); // 默认按创建时间倒序
            }
        } else {
            queryWrapper.orderByDesc(CommunityPosts::getIsTop) // 置顶帖子优先
                    .orderByDesc(CommunityPosts::getCreateTime); // 默认按创建时间倒序
        }
        
        // 执行分页查询
        Page<CommunityPosts> pageResult = this.page(new Page<>(query.getPage(), query.getPageSize()), queryWrapper);
        
        // 转换为VO列表
        List<PostVO> postVOList = convertToPostVOList(pageResult.getRecords());
        
        // 返回结果
        return new PageResult(pageResult.getTotal(), postVOList);
    }

    @Override
    public PageResult listUserPosts(PostPageQuery query) {
        // 获取当前登录用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BaseException("请先登录");
        }
        
        // 构建查询条件
        LambdaQueryWrapper<CommunityPosts> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityPosts::getUserId, userId);
        
        // 根据状态过滤
        if (query.getStatus() != null) {
            queryWrapper.eq(CommunityPosts::getStatus, query.getStatus());
        } else {
            queryWrapper.ne(CommunityPosts::getStatus, 2); // 默认不显示已删除
        }
        
        // 关键字条件
        if (StringUtils.hasText(query.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper.like(CommunityPosts::getTitle, query.getKeyword())
                    .or()
                    .like(CommunityPosts::getContent, query.getKeyword()));
        }
        
        // 排序
        if (StringUtils.hasText(query.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(query.getSortOrder());
            switch (query.getSortField()) {
                case "createTime":
                    queryWrapper.orderBy(true, isAsc, CommunityPosts::getCreateTime);
                    break;
                case "updateTime":
                    queryWrapper.orderBy(true, isAsc, CommunityPosts::getUpdateTime);
                    break;
                case "status":
                    queryWrapper.orderBy(true, isAsc, CommunityPosts::getStatus);
                    break;
                default:
                    queryWrapper.orderByDesc(CommunityPosts::getCreateTime); // 默认按创建时间倒序
            }
        } else {
            queryWrapper.orderByDesc(CommunityPosts::getCreateTime); // 默认按创建时间倒序
        }
        
        // 执行分页查询
        Page<CommunityPosts> pageResult = this.page(new Page<>(query.getPage(), query.getPageSize()), queryWrapper);
        
        // 转换为VO列表
        List<PostVO> postVOList = convertToPostVOList(pageResult.getRecords());
        
        // 返回结果
        return new PageResult(pageResult.getTotal(), postVOList);
    }
    
    /**
     * 将帖子实体转换为VO
     */
    private PostVO convertToPostVO(CommunityPosts post) {
        if (post == null) {
            return null;
        }
        
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        
        // 手动设置时间字段，解决字段名不匹配问题
        postVO.setCreatedTime(post.getCreateTime());
        postVO.setUpdatedTime(post.getUpdateTime());
        
        // 处理图片列表
        if (StringUtils.hasText(post.getImages())) {
            postVO.setImages(Arrays.asList(post.getImages().split(",")));
        } else {
            postVO.setImages(new ArrayList<>());
        }
        
        // 获取用户信息
        Users user = usersService.getById(post.getUserId());
        if (user != null) {
            postVO.setUsername(user.getUsername());
            postVO.setAvatar(user.getAvatarUrl());
        }
        
        return postVO;
    }
    
    /**
     * 将帖子实体列表转换为VO列表
     */
    private List<PostVO> convertToPostVOList(List<CommunityPosts> postList) {
        if (CollectionUtils.isEmpty(postList)) {
            return new ArrayList<>();
        }
        
        // 获取所有用户ID
        Set<Long> userIds = postList.stream()
                .map(CommunityPosts::getUserId)
                .collect(Collectors.toSet());
        
        // 批量查询用户信息
        Map<Long, Users> userMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(userIds)) {
            List<Users> users = usersService.listByIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(Users::getId, user -> user));
        }
        
        // 当前登录用户ID
        Long currentUserId = UserContext.getUserId();
        
        // 如果用户已登录，批量查询点赞和收藏状态
        Map<Long, Boolean> likeMap = new HashMap<>();
        Map<Long, Boolean> favoriteMap = new HashMap<>();
        
        if (currentUserId != null && !CollectionUtils.isEmpty(postList)) {
            Set<Long> postIds = postList.stream()
                    .map(CommunityPosts::getId)
                    .collect(Collectors.toSet());
            
            // 批量查询点赞记录
            LambdaQueryWrapper<LikeRecords> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(LikeRecords::getUserId, currentUserId)
                    .eq(LikeRecords::getTargetType, 1) // 假设1表示帖子点赞
                    .in(LikeRecords::getTargetId, postIds);
            
            List<LikeRecords> likeRecords = likeRecordsService.list(likeWrapper);
            if (!CollectionUtils.isEmpty(likeRecords)) {
                for (LikeRecords record : likeRecords) {
                    likeMap.put(record.getTargetId(), true);
                }
            }
            
            // 批量查询收藏记录
            LambdaQueryWrapper<FavoriteRecords> favoriteWrapper = new LambdaQueryWrapper<>();
            favoriteWrapper.eq(FavoriteRecords::getUserId, currentUserId)
                    .eq(FavoriteRecords::getTargetType, 1) // 假设1表示帖子收藏
                    .in(FavoriteRecords::getTargetId, postIds);
            
            List<FavoriteRecords> favoriteRecords = favoriteRecordsService.list(favoriteWrapper);
            if (!CollectionUtils.isEmpty(favoriteRecords)) {
                for (FavoriteRecords record : favoriteRecords) {
                    favoriteMap.put(record.getTargetId(), true);
                }
            }
        }
        
        // 转换为VO列表
        Map<Long, Users> finalUserMap = userMap;
        Map<Long, Boolean> finalLikeMap = likeMap;
        Map<Long, Boolean> finalFavoriteMap = favoriteMap;
        
        return postList.stream().map(post -> {
            PostVO postVO = new PostVO();
            BeanUtils.copyProperties(post, postVO);
            
            // 手动设置时间字段，解决字段名不匹配问题
            postVO.setCreatedTime(post.getCreateTime());
            postVO.setUpdatedTime(post.getUpdateTime());
            
            // 处理图片列表
            if (StringUtils.hasText(post.getImages())) {
                postVO.setImages(Arrays.asList(post.getImages().split(",")));
            } else {
                postVO.setImages(new ArrayList<>());
            }
            
            // 设置用户信息
            Users user = finalUserMap.get(post.getUserId());
            if (user != null) {
                postVO.setUsername(user.getUsername());
                postVO.setAvatar(user.getAvatarUrl());
            }
            
            // 设置点赞和收藏状态
            postVO.setHasLiked(finalLikeMap.getOrDefault(post.getId(), false));
            postVO.setHasFavorited(finalFavoriteMap.getOrDefault(post.getId(), false));
            
            return postVO;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult adminListPosts(AdminPostPageQuery query) {
        // 检查当前用户是否是管理员
        Integer userRole = UserContext.getUserRole();
        if (userRole == null || userRole != 1) {
            throw new BaseException("无权限操作，请联系管理员");
        }
        
        // 构建查询条件
        LambdaQueryWrapper<CommunityPosts> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按用户ID筛选
        if (query.getUserId() != null) {
            queryWrapper.eq(CommunityPosts::getUserId, query.getUserId());
        }
        
        // 按状态筛选
        if (query.getStatus() != null) {
            queryWrapper.eq(CommunityPosts::getStatus, query.getStatus());
        }
        
        // 按分类筛选
        if (StringUtils.hasText(query.getCategory())) {
            queryWrapper.eq(CommunityPosts::getCategory, query.getCategory());
        }
        
        // 按关键字筛选
        if (StringUtils.hasText(query.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper.like(CommunityPosts::getTitle, query.getKeyword())
                    .or()
                    .like(CommunityPosts::getContent, query.getKeyword()));
        }
        
        // 按是否置顶筛选
        if (query.getIsTop() != null) {
            queryWrapper.eq(CommunityPosts::getIsTop, query.getIsTop());
        }
        
        // 按是否精华筛选
        if (query.getIsEssence() != null) {
            queryWrapper.eq(CommunityPosts::getIsEssence, query.getIsEssence());
        }
        
        // 按时间范围筛选
        if (StringUtils.hasText(query.getStartTime()) && StringUtils.hasText(query.getEndTime())) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startDate = sdf.parse(query.getStartTime());
                Date endDate = sdf.parse(query.getEndTime());
                queryWrapper.between(CommunityPosts::getCreateTime, startDate, endDate);
            } catch (ParseException e) {
                throw new BaseException("日期格式错误，正确格式：yyyy-MM-dd HH:mm:ss");
            }
        } else if (StringUtils.hasText(query.getStartTime())) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startDate = sdf.parse(query.getStartTime());
                queryWrapper.ge(CommunityPosts::getCreateTime, startDate);
            } catch (ParseException e) {
                throw new BaseException("开始日期格式错误，正确格式：yyyy-MM-dd HH:mm:ss");
            }
        } else if (StringUtils.hasText(query.getEndTime())) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date endDate = sdf.parse(query.getEndTime());
                queryWrapper.le(CommunityPosts::getCreateTime, endDate);
            } catch (ParseException e) {
                throw new BaseException("结束日期格式错误，正确格式：yyyy-MM-dd HH:mm:ss");
            }
        }
        
        // 排序
        if (StringUtils.hasText(query.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(query.getSortOrder());
            switch (query.getSortField()) {
                case "createTime":
                    queryWrapper.orderBy(true, isAsc, CommunityPosts::getCreateTime);
                    break;
                case "updateTime":
                    queryWrapper.orderBy(true, isAsc, CommunityPosts::getUpdateTime);
                    break;
                case "viewCount":
                    queryWrapper.orderBy(true, isAsc, CommunityPosts::getViewCount);
                    break;
                case "likeCount":
                    queryWrapper.orderBy(true, isAsc, CommunityPosts::getLikeCount);
                    break;
                case "commentCount":
                    queryWrapper.orderBy(true, isAsc, CommunityPosts::getCommentCount);
                    break;
                default:
                    queryWrapper.orderByDesc(CommunityPosts::getCreateTime); // 默认按创建时间倒序
            }
        } else {
            queryWrapper.orderByDesc(CommunityPosts::getCreateTime);
        }
        
        // 执行分页查询
        Page<CommunityPosts> pageResult = this.page(new Page<>(query.getPage(), query.getPageSize()), queryWrapper);
        
        // 转换为VO列表
        List<PostVO> postVOList = convertToPostVOList(pageResult.getRecords());
        
        // 返回结果
        return new PageResult(pageResult.getTotal(), postVOList);
    }

    @Override
    @Transactional
    public boolean updatePostStatus(PostStatusDTO postStatusDTO) {
        // 检查当前用户是否是管理员
        Integer userRole = UserContext.getUserRole();
        if (userRole == null || userRole != 1) {
            throw new BaseException("无权限操作，请联系管理员");
        }
        
        // 检查参数
        if (postStatusDTO == null || postStatusDTO.getPostId() == null) {
            throw new BaseException("参数不能为空");
        }
        
        // 查询帖子是否存在
        CommunityPosts post = this.getById(postStatusDTO.getPostId());
        if (post == null) {
            throw new BaseException("帖子不存在");
        }
        
        // 更新帖子状态
        boolean updated = false;
        
        if (postStatusDTO.getStatus() != null) {
            post.setStatus(postStatusDTO.getStatus());
            updated = true;
        }
        
        if (postStatusDTO.getIsTop() != null) {
            post.setIsTop(postStatusDTO.getIsTop());
            updated = true;
        }
        
        if (postStatusDTO.getIsEssence() != null) {
            post.setIsEssence(postStatusDTO.getIsEssence());
            updated = true;
        }
        
        if (updated) {
            post.setUpdateTime(new Date());
            return this.updateById(post);
        }
        
        return false;
    }

    @Override
    @Transactional
    public boolean adminDeletePost(Long postId) {
        // 检查当前用户是否是管理员
        Integer userRole = UserContext.getUserRole();
        if (userRole == null || userRole != 1) {
            throw new BaseException("无权限操作，请联系管理员");
        }
        
        if (postId == null) {
            throw new BaseException("帖子ID不能为空");
        }
        
        // 查询帖子是否存在
        CommunityPosts post = this.getById(postId);
        if (post == null) {
            throw new BaseException("帖子不存在");
        }
        
        // 物理删除帖子
        return this.removeById(postId);
    }
}




