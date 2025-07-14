package com.zheng.aicommunitybackend.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zheng.aicommunitybackend.domain.entity.CommunityPosts;
import com.zheng.aicommunitybackend.domain.entity.LikeRecords;
import com.zheng.aicommunitybackend.domain.entity.PostComments;
import com.zheng.aicommunitybackend.mapper.CommunityPostsMapper;
import com.zheng.aicommunitybackend.mapper.LikeRecordsMapper;
import com.zheng.aicommunitybackend.mapper.PostCommentsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 点赞数据对账补偿任务
 */
@Slf4j
@Component
public class LikeReconciliationTask {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private LikeRecordsMapper likeRecordsMapper;
    
    @Autowired
    private CommunityPostsMapper communityPostsMapper;
    
    @Autowired
    private PostCommentsMapper postCommentsMapper;
    
    // Redis中用户点赞记录的hash结构key前缀
    private static final String LIKE_KEY_PREFIX = "community:like:";
    
    /**
     * 每天凌晨3点执行点赞数据对账补偿
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void reconcileLikeData() {
        log.info("开始执行点赞数据对账补偿任务...");
        
        try {
            // 1. 从Redis中获取所有用户点赞数据并统计点赞数
            Map<String, Integer> postLikeCountMap = new HashMap<>();  // 文章点赞数统计
            Map<String, Integer> commentLikeCountMap = new HashMap<>();  // 评论点赞数统计
            Map<String, Set<Long>> postLikeUsersMap = new HashMap<>();  // 文章点赞用户集合
            Map<String, Set<Long>> commentLikeUsersMap = new HashMap<>();  // 评论点赞用户集合
            
            // 收集所有点赞记录
            collectLikeDataFromRedis(postLikeCountMap, commentLikeCountMap, 
                    postLikeUsersMap, commentLikeUsersMap);
            
            // 2. 同步文章点赞数和记录
            reconcilePostsLikeData(postLikeCountMap, postLikeUsersMap);
            
            // 3. 同步评论点赞数和记录
            reconcileCommentsLikeData(commentLikeCountMap, commentLikeUsersMap);
            
            log.info("点赞数据对账补偿任务执行完成");
        } catch (Exception e) {
            log.error("点赞数据对账补偿任务执行失败", e);
        }
    }
    
    /**
     * 从Redis中收集所有用户的点赞数据
     */
    private void collectLikeDataFromRedis(
            Map<String, Integer> postLikeCountMap, 
            Map<String, Integer> commentLikeCountMap,
            Map<String, Set<Long>> postLikeUsersMap,
            Map<String, Set<Long>> commentLikeUsersMap) {
        
        log.info("开始从Redis收集点赞数据...");
        // 获取所有用户的点赞key
        Set<String> userLikeKeys = stringRedisTemplate.keys(LIKE_KEY_PREFIX + "*");
        if (userLikeKeys == null || userLikeKeys.isEmpty()) {
            log.info("Redis中未找到点赞记录数据");
            return;
        }
        
        log.info("找到{}个用户点赞记录", userLikeKeys.size());
        
        // 遍历每个用户的点赞记录
        for (String userLikeKey : userLikeKeys) {
            // 提取用户ID
            String userIdStr = userLikeKey.substring(LIKE_KEY_PREFIX.length());
            Long userId;
            try {
                userId = Long.valueOf(userIdStr);
            } catch (NumberFormatException e) {
                log.warn("无效的用户ID: {}", userIdStr);
                continue;
            }
            
            // 获取该用户的所有点赞目标
            Map<Object, Object> userLikes = stringRedisTemplate.opsForHash().entries(userLikeKey);
            if (userLikes == null || userLikes.isEmpty()) {
                continue;
            }
            
            // 处理每个点赞目标
            for (Map.Entry<Object, Object> entry : userLikes.entrySet()) {
                String targetKey = entry.getKey().toString();
                // 解析目标类型和ID
                String[] parts = targetKey.split(":");
                if (parts.length != 2) {
                    log.warn("无效的点赞目标键: {}", targetKey);
                    continue;
                }
                
                int type;
                Long targetId;
                try {
                    type = Integer.parseInt(parts[0]);
                    targetId = Long.parseLong(parts[1]);
                } catch (NumberFormatException e) {
                    log.warn("无效的点赞目标类型或ID: {}", targetKey);
                    continue;
                }
                
                // 根据类型统计点赞数和记录点赞用户
                if (type == 1) {  // 文章点赞
                    String key = String.valueOf(targetId);
                    postLikeCountMap.put(key, postLikeCountMap.getOrDefault(key, 0) + 1);
                    
                    // 记录点赞用户
                    postLikeUsersMap.computeIfAbsent(key, k -> new HashSet<>()).add(userId);
                } else if (type == 2) {  // 评论点赞
                    String key = String.valueOf(targetId);
                    commentLikeCountMap.put(key, commentLikeCountMap.getOrDefault(key, 0) + 1);
                    
                    // 记录点赞用户
                    commentLikeUsersMap.computeIfAbsent(key, k -> new HashSet<>()).add(userId);
                }
            }
        }
        
        log.info("Redis点赞数据收集完成，文章点赞数: {}, 评论点赞数: {}", 
                postLikeCountMap.size(), commentLikeCountMap.size());
    }
    
    /**
     * 同步文章点赞数和记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void reconcilePostsLikeData(
            Map<String, Integer> postLikeCountMap, 
            Map<String, Set<Long>> postLikeUsersMap) {
        
        log.info("开始同步文章点赞数据...");
        if (postLikeCountMap.isEmpty()) {
            return;
        }
        
        int updatedPostCount = 0;
        int addedLikeRecords = 0;
        int removedLikeRecords = 0;
        
        // 获取所有需要更新的文章ID
        List<Long> postIds = postLikeCountMap.keySet().stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
        
        // 批量查询文章
        List<CommunityPosts> posts = communityPostsMapper.selectBatchIds(postIds);
        Map<Long, CommunityPosts> postsMap = posts.stream()
                .collect(Collectors.toMap(CommunityPosts::getId, Function.identity()));
        
        // 更新文章点赞数
        for (Map.Entry<String, Integer> entry : postLikeCountMap.entrySet()) {
            Long postId = Long.parseLong(entry.getKey());
            Integer likeCount = entry.getValue();
            
            CommunityPosts post = postsMap.get(postId);
            if (post == null) {
                log.warn("文章不存在，ID: {}", postId);
                continue;
            }
            
            // 如果点赞数不一致，更新数据库
            if (!likeCount.equals(post.getLikeCount())) {
                log.info("更新文章点赞数: ID={}, Redis点赞数={}, DB点赞数={}", 
                        postId, likeCount, post.getLikeCount());
                post.setLikeCount(likeCount);
                communityPostsMapper.updateById(post);
                updatedPostCount++;
            }
        }
        
        // 同步点赞记录表
        for (Map.Entry<String, Set<Long>> entry : postLikeUsersMap.entrySet()) {
            Long postId = Long.parseLong(entry.getKey());
            Set<Long> userIds = entry.getValue();
            
            // 查询数据库中该文章的所有点赞记录
            LambdaQueryWrapper<LikeRecords> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LikeRecords::getTargetType, 1)
                    .eq(LikeRecords::getTargetId, postId);
            List<LikeRecords> dbLikeRecords = likeRecordsMapper.selectList(queryWrapper);
            
            // 将数据库记录转为用户ID集合
            Set<Long> dbUserIds = dbLikeRecords.stream()
                    .map(LikeRecords::getUserId)
                    .collect(Collectors.toSet());
            
            // 需要添加的记录：Redis中有但数据库中没有
            Set<Long> toAddUserIds = new HashSet<>(userIds);
            toAddUserIds.removeAll(dbUserIds);
            
            // 需要删除的记录：数据库中有但Redis中没有
            Set<Long> toRemoveUserIds = new HashSet<>(dbUserIds);
            toRemoveUserIds.removeAll(userIds);
            
            // 批量添加点赞记录
            if (!toAddUserIds.isEmpty()) {
                List<LikeRecords> toAddRecords = new ArrayList<>();
                for (Long userId : toAddUserIds) {
                    LikeRecords record = new LikeRecords();
                    record.setUserId(userId);
                    record.setTargetType(1);  // 1表示文章
                    record.setTargetId(postId);
                    record.setCreateTime(new Date());
                    toAddRecords.add(record);
                }
                
                for (LikeRecords record : toAddRecords) {
                    likeRecordsMapper.insert(record);
                    addedLikeRecords++;
                }
                
                log.info("为文章ID={}添加了{}条点赞记录", postId, toAddRecords.size());
            }
            
            // 批量删除点赞记录
            if (!toRemoveUserIds.isEmpty()) {
                LambdaQueryWrapper<LikeRecords> removeWrapper = new LambdaQueryWrapper<>();
                removeWrapper.eq(LikeRecords::getTargetType, 1)
                        .eq(LikeRecords::getTargetId, postId)
                        .in(LikeRecords::getUserId, toRemoveUserIds);
                int removedCount = likeRecordsMapper.delete(removeWrapper);
                removedLikeRecords += removedCount;
                
                log.info("从文章ID={}删除了{}条点赞记录", postId, removedCount);
            }
        }
        
        log.info("文章点赞数据同步完成: 更新{}篇文章点赞数, 新增{}条点赞记录, 删除{}条点赞记录",
                updatedPostCount, addedLikeRecords, removedLikeRecords);
    }
    
    /**
     * 同步评论点赞数和记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void reconcileCommentsLikeData(
            Map<String, Integer> commentLikeCountMap, 
            Map<String, Set<Long>> commentLikeUsersMap) {
        
        log.info("开始同步评论点赞数据...");
        if (commentLikeCountMap.isEmpty()) {
            return;
        }
        
        int updatedCommentCount = 0;
        int addedLikeRecords = 0;
        int removedLikeRecords = 0;
        
        // 获取所有需要更新的评论ID
        List<Long> commentIds = commentLikeCountMap.keySet().stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
        
        // 批量查询评论
        List<PostComments> comments = postCommentsMapper.selectBatchIds(commentIds);
        Map<Long, PostComments> commentsMap = comments.stream()
                .collect(Collectors.toMap(PostComments::getId, Function.identity()));
        
        // 更新评论点赞数
        for (Map.Entry<String, Integer> entry : commentLikeCountMap.entrySet()) {
            Long commentId = Long.parseLong(entry.getKey());
            Integer likeCount = entry.getValue();
            
            PostComments comment = commentsMap.get(commentId);
            if (comment == null) {
                log.warn("评论不存在，ID: {}", commentId);
                continue;
            }
            
            // 如果点赞数不一致，更新数据库
            if (!likeCount.equals(comment.getLikeCount())) {
                log.info("更新评论点赞数: ID={}, Redis点赞数={}, DB点赞数={}", 
                        commentId, likeCount, comment.getLikeCount());
                comment.setLikeCount(likeCount);
                postCommentsMapper.updateById(comment);
                updatedCommentCount++;
            }
        }
        
        // 同步点赞记录表
        for (Map.Entry<String, Set<Long>> entry : commentLikeUsersMap.entrySet()) {
            Long commentId = Long.parseLong(entry.getKey());
            Set<Long> userIds = entry.getValue();
            
            // 查询数据库中该评论的所有点赞记录
            LambdaQueryWrapper<LikeRecords> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LikeRecords::getTargetType, 2)  // 2表示评论
                    .eq(LikeRecords::getTargetId, commentId);
            List<LikeRecords> dbLikeRecords = likeRecordsMapper.selectList(queryWrapper);
            
            // 将数据库记录转为用户ID集合
            Set<Long> dbUserIds = dbLikeRecords.stream()
                    .map(LikeRecords::getUserId)
                    .collect(Collectors.toSet());
            
            // 需要添加的记录：Redis中有但数据库中没有
            Set<Long> toAddUserIds = new HashSet<>(userIds);
            toAddUserIds.removeAll(dbUserIds);
            
            // 需要删除的记录：数据库中有但Redis中没有
            Set<Long> toRemoveUserIds = new HashSet<>(dbUserIds);
            toRemoveUserIds.removeAll(userIds);
            
            // 批量添加点赞记录
            if (!toAddUserIds.isEmpty()) {
                List<LikeRecords> toAddRecords = new ArrayList<>();
                for (Long userId : toAddUserIds) {
                    LikeRecords record = new LikeRecords();
                    record.setUserId(userId);
                    record.setTargetType(2);  // 2表示评论
                    record.setTargetId(commentId);
                    record.setCreateTime(new Date());
                    toAddRecords.add(record);
                }
                
                for (LikeRecords record : toAddRecords) {
                    likeRecordsMapper.insert(record);
                    addedLikeRecords++;
                }
                
                log.info("为评论ID={}添加了{}条点赞记录", commentId, toAddRecords.size());
            }
            
            // 批量删除点赞记录
            if (!toRemoveUserIds.isEmpty()) {
                LambdaQueryWrapper<LikeRecords> removeWrapper = new LambdaQueryWrapper<>();
                removeWrapper.eq(LikeRecords::getTargetType, 2)  // 2表示评论
                        .eq(LikeRecords::getTargetId, commentId)
                        .in(LikeRecords::getUserId, toRemoveUserIds);
                int removedCount = likeRecordsMapper.delete(removeWrapper);
                removedLikeRecords += removedCount;
                
                log.info("从评论ID={}删除了{}条点赞记录", commentId, removedCount);
            }
        }
        
        log.info("评论点赞数据同步完成: 更新{}条评论点赞数, 新增{}条点赞记录, 删除{}条点赞记录",
                updatedCommentCount, addedLikeRecords, removedLikeRecords);
    }
    
    /**
     * 手动触发执行点赞数据对账
     * @return 处理结果信息
     */
    public Map<String, Object> manualReconcile() {
        Map<String, Object> result = new HashMap<>();
        try {
            reconcileLikeData();
            result.put("success", true);
            result.put("message", "点赞数据对账补偿任务执行成功");
        } catch (Exception e) {
            log.error("手动触发点赞数据对账失败", e);
            result.put("success", false);
            result.put("message", "点赞数据对账补偿任务执行失败: " + e.getMessage());
        }
        return result;
    }
} 