package com.zheng.aicommunitybackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zheng.aicommunitybackend.domain.dto.RedPacketActivityCreateDTO;
import com.zheng.aicommunitybackend.domain.dto.RedPacketActivityQueryDTO;
import com.zheng.aicommunitybackend.domain.vo.RedPacketActivityListVO;
import com.zheng.aicommunitybackend.domain.vo.RedPacketActivityVO;
import com.zheng.aicommunitybackend.domain.vo.RedPacketGrabVO;
import com.zheng.aicommunitybackend.domain.vo.RedPacketRecordVO;

import java.util.List;

/**
 * 红包服务接口
 */
public interface RedPacketService {

    /**
     * 创建红包活动
     * @param createDTO 创建参数
     * @param creatorId 创建者ID
     * @return 活动ID
     */
    Long createActivity(RedPacketActivityCreateDTO createDTO, Long creatorId);

    /**
     * 分页查询红包活动列表（管理端）
     * @param queryDTO 查询参数
     * @return 活动列表
     */
    IPage<RedPacketActivityVO> getActivityPage(RedPacketActivityQueryDTO queryDTO);

    /**
     * 查询进行中的活动列表（用户端）
     * @param userId 用户ID，用于判断用户是否已抢过
     * @return 进行中的活动列表
     */
    List<RedPacketActivityListVO> getActiveActivitiesForUser(Long userId);

    /**
     * 根据ID查询活动详情
     * @param id 活动ID
     * @param userId 用户ID（可选，用于查询用户是否已抢过）
     * @return 活动详情
     */
    RedPacketActivityVO getActivityDetail(Long id, Long userId);

    /**
     * 抢红包
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 抢红包结果
     */
    RedPacketGrabVO grabRedPacket(Long userId, Long activityId);

    /**
     * 分页查询用户抢红包记录
     * @param userId 用户ID
     * @param activityId 活动ID（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 抢红包记录列表
     */
    IPage<RedPacketRecordVO> getUserRecordPage(Long userId, Long activityId, Integer page, Integer size);

    /**
     * 分页查询活动的抢红包记录（管理端）
     * @param activityId 活动ID
     * @param page 页码
     * @param size 每页大小
     * @return 抢红包记录列表
     */
    IPage<RedPacketRecordVO> getActivityRecordPage(Long activityId, Integer page, Integer size);

    /**
     * 取消活动
     * @param activityId 活动ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean cancelActivity(Long activityId, Long operatorId);

    /**
     * 手动开始活动
     * @param activityId 活动ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean startActivity(Long activityId, Long operatorId);

    /**
     * 手动结束活动
     * @param activityId 活动ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean endActivity(Long activityId, Long operatorId);

    /**
     * 预加载活动到Redis（活动开始前调用）
     * @param activityId 活动ID
     * @return 是否成功
     */
    boolean preloadActivityToRedis(Long activityId);

    /**
     * 清理活动Redis数据（活动结束后调用）
     * @param activityId 活动ID
     * @return 是否成功
     */
    boolean clearActivityFromRedis(Long activityId);

    /**
     * 处理未更新账户的记录（补偿机制）
     * @param limit 处理数量限制
     * @return 处理的记录数
     */
    int processUnprocessedRecords(Integer limit);

    /**
     * 获取活动统计信息
     * @param activityId 活动ID
     * @return 统计信息
     */
    java.util.Map<String, Object> getActivityStats(Long activityId);

    /**
     * 获取用户抢红包统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    java.util.Map<String, Object> getUserStats(Long userId);
}
