package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.dto.AdminLikePageQuery;
import com.zheng.aicommunitybackend.domain.dto.LikeRecordDTO;
import com.zheng.aicommunitybackend.domain.entity.CommunityPosts;
import com.zheng.aicommunitybackend.domain.entity.LikeRecords;
import com.zheng.aicommunitybackend.domain.entity.PostComments;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.AdminLikeVO;
import com.zheng.aicommunitybackend.domain.vo.LikeStatusVO;
import com.zheng.aicommunitybackend.exception.BaseException;
import com.zheng.aicommunitybackend.mapper.CommunityPostsMapper;
import com.zheng.aicommunitybackend.mapper.LikeRecordsMapper;
import com.zheng.aicommunitybackend.mapper.PostCommentsMapper;
import com.zheng.aicommunitybackend.service.LikeRecordsService;
import com.zheng.aicommunitybackend.service.UsersService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

// 导入MQ相关
import com.zheng.aicommunitybackend.domain.dto.LikeActionMessage;
import com.zheng.aicommunitybackend.service.MQProducerService;

/**
* @author ZhengJJ
* @description 针对表【like_records(点赞记录表)】的数据库操作Service实现
* @createDate 2025-07-10 11:03:49
*/
@Service
public class LikeRecordsServiceImpl extends ServiceImpl<LikeRecordsMapper, LikeRecords>
    implements LikeRecordsService {

    @Autowired
    private CommunityPostsMapper communityPostsMapper;
    
    @Autowired
    private PostCommentsMapper postCommentsMapper;

    @Autowired
    private UsersService usersService;

    @Resource
    private RedisTemplate redisTemplate;
    
    @Autowired
    private MQProducerService mqProducerService;
    
    /**
     * 点赞类型：帖子
     */
    private static final int TYPE_POST = 1;
    
    /**
     * 点赞类型：评论
     */
    private static final int TYPE_COMMENT = 2;

    @Override
    @Transactional
    public LikeStatusVO likeOrUnlike(LikeRecordDTO dto, Long userId) {
        // 1. 校验目标是否存在
        checkTargetExists(dto.getType(), dto.getTargetId());
        
        // 2. 查询是否已点赞
        LambdaQueryWrapper<LikeRecords> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LikeRecords::getUserId, userId)
                .eq(LikeRecords::getTargetType, dto.getType())
                .eq(LikeRecords::getTargetId, dto.getTargetId());
        
        LikeRecords record = this.getOne(queryWrapper);
        
        // 3. 处理点赞/取消点赞
        boolean isLiked;
        if (record == null) {
            // 未点赞，添加点赞记录
            record = new LikeRecords();
            record.setUserId(userId);
            record.setTargetType(dto.getType());
            record.setTargetId(dto.getTargetId());
            record.setCreateTime(new Date());
            this.save(record);
            
            // 更新目标点赞数 +1
            updateLikeCount(dto.getType(), dto.getTargetId(), 1);
            isLiked = true;
        } else {
            // 已点赞，取消点赞
            this.removeById(record.getId());
            
            // 更新目标点赞数 -1
            updateLikeCount(dto.getType(), dto.getTargetId(), -1);
            isLiked = false;
        }
        
        // 4. 返回最新点赞状态
        int likeCount = getLikeCount(dto.getType(), dto.getTargetId());
        return new LikeStatusVO(isLiked, likeCount);
    }

    /**
     * 点赞 / 取消点赞 (Redis + RocketMQ) 优化
     * @param dto 点赞记录DTO
     * @param userId 用户ID
     * @return
     */
    @Override
    @Transactional
    public LikeStatusVO likeOrUnlikeRedis(LikeRecordDTO dto, Long userId) {
        // 1. 校验目标是否存在
        checkTargetExists(dto.getType(), dto.getTargetId());

        // 2. 使用Redis Lua脚本处理点赞/取消点赞
        String key = "community:like:" + userId;
        String field = dto.getType() + ":" + dto.getTargetId();
        
        // 编写Lua脚本 - 修复脚本语法，确保正确闭合
        String script = 
                "local key = KEYS[1];\n" +
                "local field = ARGV[1];\n" +
                "local exists = redis.call('HEXISTS', key, field);\n" +
                "if exists == 1 then\n" +
                "   redis.call('HDEL', key, field);\n" +
                "   return 0;\n" +
                "else\n" +
                "   redis.call('HSET', key, field, 1);\n" +
                "   return 1;\n" +
                "end";
        
        // 执行Lua脚本
        Long result = (Long) redisTemplate.execute(
                (RedisCallback<Object>) connection -> 
                        connection.scriptingCommands().eval(
                                script.getBytes(),
                                ReturnType.INTEGER,
                                1,
                                key.getBytes(),
                                field.getBytes()
                        )
        );
        
        // 3. 根据结果处理业务逻辑
        boolean isLiked = (result != null && result == 1);
        
        // 4. 发送消息到MQ处理数据库操作
        LikeActionMessage message = new LikeActionMessage(
                userId,
                dto.getType(),
                dto.getTargetId(),
                isLiked ? 1 : 0  // 1表示点赞，0表示取消点赞
        );
        mqProducerService.sendLikeMessage(message);
        
        // 5. 返回最新点赞状态
        int likeCount = getLikeCount(dto.getType(), dto.getTargetId());
        return new LikeStatusVO(isLiked, likeCount);
    }

    @Override
    public LikeStatusVO getLikeStatus(Integer type, Long targetId, Long userId) {
        // 1. 校验目标是否存在
        checkTargetExists(type, targetId);
        
        // 2. 查询是否已点赞
        LambdaQueryWrapper<LikeRecords> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LikeRecords::getUserId, userId)
                .eq(LikeRecords::getTargetType, type)
                .eq(LikeRecords::getTargetId, targetId);
        
        boolean isLiked = this.count(queryWrapper) > 0;
        
        // 3. 获取点赞数
        int likeCount = getLikeCount(type, targetId);
        
        return new LikeStatusVO(isLiked, likeCount);
    }

    @Override
    public List<Long> getUserLikedList(Long userId, Integer type) {
        LambdaQueryWrapper<LikeRecords> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LikeRecords::getUserId, userId)
                .eq(LikeRecords::getTargetType, type)
                .select(LikeRecords::getTargetId);
        
        List<LikeRecords> records = this.list(queryWrapper);
        return records.stream().map(LikeRecords::getTargetId).collect(Collectors.toList());
    }

    @Override
    public List<LikeStatusVO> batchGetLikeStatus(Integer type, List<Long> targetIds, Long userId) {
        if (targetIds == null || targetIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 1. 批量查询用户点赞状态
        LambdaQueryWrapper<LikeRecords> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LikeRecords::getUserId, userId)
                .eq(LikeRecords::getTargetType, type)
                .in(LikeRecords::getTargetId, targetIds);
        
        List<LikeRecords> userLikeRecords = this.list(queryWrapper);
        List<Long> userLikedIds = userLikeRecords.stream()
                .map(LikeRecords::getTargetId)
                .collect(Collectors.toList());
        
        // 2. 获取每个目标的点赞数
        List<LikeStatusVO> result = new ArrayList<>();
        for (Long targetId : targetIds) {
            boolean isLiked = userLikedIds.contains(targetId);
            int likeCount = getLikeCount(type, targetId);
            result.add(new LikeStatusVO(isLiked, likeCount));
        }
        
        return result;
    }

    /**
     * 分页查询点赞列表
     * @param adminLikePageQuery
     * @return
     */
    @Override
    public PageResult<AdminLikeVO> getLikesPage(AdminLikePageQuery adminLikePageQuery) {
        // 拼接查询条件
        LambdaQueryWrapper<LikeRecords> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(adminLikePageQuery.getId() != null){
            lambdaQueryWrapper.eq(LikeRecords::getId, adminLikePageQuery.getId());
        }
        if(adminLikePageQuery.getUserId() != null){
            lambdaQueryWrapper.eq(LikeRecords::getUserId, adminLikePageQuery.getUserId());
        }
        if(adminLikePageQuery.getTargetType() != null){
            lambdaQueryWrapper.eq(LikeRecords::getTargetType, adminLikePageQuery.getTargetType());
        }
        if(adminLikePageQuery.getTargetId() != null){
            lambdaQueryWrapper.eq(LikeRecords::getTargetId, adminLikePageQuery.getTargetId());
        }
        if(adminLikePageQuery.getStartTime() != null && adminLikePageQuery.getEndTime() != null){
            lambdaQueryWrapper.between(LikeRecords::getCreateTime, adminLikePageQuery.getStartTime(), adminLikePageQuery.getEndTime());
        }else if(adminLikePageQuery.getStartTime() != null){
            lambdaQueryWrapper.ge(LikeRecords::getCreateTime, adminLikePageQuery.getStartTime());
        }else if(adminLikePageQuery.getEndTime() != null){
            lambdaQueryWrapper.le(LikeRecords::getCreateTime, adminLikePageQuery.getEndTime());
        }

        // 拼接分页条件
        String sortField = adminLikePageQuery.getSortField();
        String sortOrder = adminLikePageQuery.getSortOrder();
        if(sortField != null){
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortField){
                case "id":
                    lambdaQueryWrapper.orderBy(true, isAsc, LikeRecords::getId);
                    break;
                case "userId":
                    lambdaQueryWrapper.orderBy(true, isAsc, LikeRecords::getUserId);
                    break;
                case "targetType":
                    lambdaQueryWrapper.orderBy(true, isAsc, LikeRecords::getTargetType);
                    break;
                case "targetId":
                    lambdaQueryWrapper.orderBy(true, isAsc, LikeRecords::getTargetId);
                    break;
                case "createTime":
                    lambdaQueryWrapper.orderBy(true, isAsc, LikeRecords::getCreateTime);
                    break;
                default:
                    lambdaQueryWrapper.orderByDesc(LikeRecords::getCreateTime);
            }
        } else {
            // 默认按创建时间倒序
            lambdaQueryWrapper.orderByDesc(LikeRecords::getCreateTime);
        }

        // 开始查询数据
        Page<LikeRecords> page = page(new Page<>(adminLikePageQuery.getPage(), adminLikePageQuery.getPageSize()), lambdaQueryWrapper);
        List<LikeRecords> records = page.getRecords();

        // 转换VO
        List<AdminLikeVO> voList = convertToAdminLikeVOList(records);
        
        // 封装返回结果
        return new PageResult<AdminLikeVO>(page.getTotal(), voList);
    }

    /**
     * 转换成 VO 对象集合
     * @param recordsList 点赞记录列表
     * @return VO列表
     */
    private List<AdminLikeVO> convertToAdminLikeVOList(List<LikeRecords> recordsList){
        if(recordsList == null || recordsList.isEmpty()){
            return new ArrayList<>();
        }
        
        List<AdminLikeVO> voList = new ArrayList<>(recordsList.size());
        
        for(LikeRecords record : recordsList){
            AdminLikeVO vo = new AdminLikeVO();
            vo.setId(record.getId());
            vo.setUserId(record.getUserId());
            vo.setTargetType(record.getTargetType());
            vo.setTargetId(record.getTargetId());
            // 将Date转换为LocalDateTime
            vo.setCreateTime(record.getCreateTime().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
            
            // 填充用户信息
            com.zheng.aicommunitybackend.domain.entity.Users user = usersService.getById(record.getUserId());
            if(user != null){
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
            }
            
            // 根据目标类型获取目标标题/内容预览
            if(TYPE_POST == record.getTargetType()){
                CommunityPosts post = communityPostsMapper.selectById(record.getTargetId());
                if(post != null){
                    vo.setTargetTitle(post.getTitle());
                }
            } else if(TYPE_COMMENT == record.getTargetType()){
                PostComments comment = postCommentsMapper.selectById(record.getTargetId());
                if(comment != null){
                    // 取评论内容前20个字符作为预览
                    String content = comment.getContent();
                    if(content != null && content.length() > 20){
                        content = content.substring(0, 20) + "...";
                    }
                    vo.setTargetTitle(content);
                }
            }
            
            voList.add(vo);
        }
        
        return voList;
    }

    /**
     * 校验目标是否存在
     * @param type 目标类型：1-帖子 2-评论
     * @param targetId 目标ID
     */
    private void checkTargetExists(Integer type, Long targetId) {
        if (type == null || targetId == null) {
            throw new BaseException("目标类型或ID不能为空");
        }
        
        if (TYPE_POST == type) {
            CommunityPosts post = communityPostsMapper.selectById(targetId);
            if (post == null) {
                throw new BaseException("帖子不存在");
            }
        } else if (TYPE_COMMENT == type) {
            PostComments comment = postCommentsMapper.selectById(targetId);
            if (comment == null) {
                throw new BaseException("评论不存在");
            }
        } else {
            throw new BaseException("不支持的目标类型");
        }
    }
    
    /**
     * 获取目标的点赞数
     * @param type 目标类型：1-帖子 2-评论
     * @param targetId 目标ID
     * @return 点赞数
     */
    private int getLikeCount(Integer type, Long targetId) {
        if (type == null || targetId == null) {
            return 0;
        }
        
        if (TYPE_POST == type) {
            // 获取帖子点赞数
            CommunityPosts post = communityPostsMapper.selectById(targetId);
            return post != null ? post.getLikeCount() : 0;
        } else if (TYPE_COMMENT == type) {
            // 获取评论点赞数
            PostComments comment = postCommentsMapper.selectById(targetId);
            return comment != null ? comment.getLikeCount() : 0;
        }
        
        return 0;
    }
    
    /**
     * 更新目标的点赞数
     * @param type 目标类型：1-帖子 2-评论
     * @param targetId 目标ID
     * @param count 更新的数量，正数为增加，负数为减少
     */
    @Override
    public void updateLikeCount(Integer type, Long targetId, int count) {
        if (type == null || targetId == null) {
            return;
        }
        
        if (TYPE_POST == type) {
            // 更新帖子点赞数
            CommunityPosts post = communityPostsMapper.selectById(targetId);
            if (post != null) {
                // 防止点赞数为负数
                int newLikeCount = Math.max(0, post.getLikeCount() + count);
                post.setLikeCount(newLikeCount);
                communityPostsMapper.updateById(post);
            }
        } else if (TYPE_COMMENT == type) {
            // 更新评论点赞数
            PostComments comment = postCommentsMapper.selectById(targetId);
            if (comment != null) {
                // 防止点赞数为负数
                int newLikeCount = Math.max(0, comment.getLikeCount() + count);
                comment.setLikeCount(newLikeCount);
                postCommentsMapper.updateById(comment);
            }
        }
    }
}




