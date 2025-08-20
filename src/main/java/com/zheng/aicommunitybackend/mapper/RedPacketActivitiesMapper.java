package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.aicommunitybackend.domain.entity.RedPacketActivities;
import com.zheng.aicommunitybackend.domain.vo.RedPacketActivityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author ZhengJJ
 * @description 针对表【red_packet_activities(红包活动表)】的数据库操作Mapper
 * @createDate 2025-07-23 10:00:00
 * @Entity com.zheng.aicommunitybackend.domain.entity.RedPacketActivities
 */
@Mapper
public interface RedPacketActivitiesMapper extends BaseMapper<RedPacketActivities> {

    /**
     * 分页查询红包活动列表（管理端）
     * @param page 分页参数
     * @param status 活动状态
     * @param activityName 活动名称
     * @return 活动列表
     */
    IPage<RedPacketActivityVO> selectActivityPage(
            Page<RedPacketActivityVO> page,
            @Param("status") Integer status,
            @Param("activityName") String activityName
    );

    /**
     * 查询进行中的活动列表（用户端）
     * @return 进行中的活动列表
     */
    List<RedPacketActivityVO> selectActiveActivities();

    /**
     * 根据ID查询活动详情
     * @param id 活动ID
     * @return 活动详情
     */
    RedPacketActivityVO selectActivityDetail(@Param("id") Long id);

    /**
     * 更新活动统计信息（抢红包时调用）
     * @param activityId 活动ID
     * @param amount 红包金额
     * @return 更新行数
     */
    @Update("UPDATE red_packet_activities SET " +
            "grabbed_count = grabbed_count + 1, " +
            "grabbed_amount = grabbed_amount + #{amount}, " +
            "updated_time = NOW() " +
            "WHERE id = #{activityId}")
    int updateActivityStats(@Param("activityId") Long activityId, @Param("amount") Long amount);

    /**
     * 批量更新活动状态（定时任务使用）
     * @param status 新状态
     * @param ids 活动ID列表
     * @return 更新行数
     */
    int batchUpdateStatus(@Param("status") Integer status, @Param("ids") List<Long> ids);

    /**
     * 查询需要开始的活动
     * @return 需要开始的活动ID列表
     */
    List<Long> selectActivitiesToStart();

    /**
     * 查询需要结束的活动
     * @return 需要结束的活动ID列表
     */
    List<Long> selectActivitiesToEnd();
}
