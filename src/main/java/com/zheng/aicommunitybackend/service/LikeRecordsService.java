package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.AdminLikePageQuery;
import com.zheng.aicommunitybackend.domain.dto.LikeRecordDTO;
import com.zheng.aicommunitybackend.domain.entity.LikeRecords;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.AdminLikeVO;
import com.zheng.aicommunitybackend.domain.vo.LikeStatusVO;

import java.util.List;

/**
* @author ZhengJJ
* @description 针对表【like_records(点赞记录表)】的数据库操作Service
* @createDate 2025-07-10 11:03:49
*/
public interface LikeRecordsService extends IService<LikeRecords> {

    /**
     * 点赞或取消点赞
     * @param dto 点赞记录DTO
     * @param userId 用户ID
     * @return 点赞状态
     */
    LikeStatusVO likeOrUnlike(LikeRecordDTO dto, Long userId);
    
    /**
     * 获取点赞状态
     * @param type 点赞类型：1-帖子 2-评论
     * @param targetId 目标ID
     * @param userId 用户ID
     * @return 点赞状态
     */
    LikeStatusVO getLikeStatus(Integer type, Long targetId, Long userId);
    
    /**
     * 获取用户点赞列表
     * @param userId 用户ID
     * @param type 点赞类型：1-帖子 2-评论
     * @return 目标ID列表
     */
    List<Long> getUserLikedList(Long userId, Integer type);
    
    /**
     * 批量获取点赞状态
     * @param type 点赞类型：1-帖子 2-评论
     * @param targetIds 目标ID列表
     * @param userId 用户ID
     * @return 目标ID到点赞状态的映射
     */
    List<LikeStatusVO> batchGetLikeStatus(Integer type, List<Long> targetIds, Long userId);

    /**
     * 分页查询点赞列表
     * @param adminLikePageQuery
     * @return
     */
    PageResult<AdminLikeVO> getLikesPage(AdminLikePageQuery adminLikePageQuery);
}
