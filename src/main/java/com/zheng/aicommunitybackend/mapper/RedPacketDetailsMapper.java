package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zheng.aicommunitybackend.domain.entity.RedPacketDetails;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * @author ZhengJJ
 * @description 针对表【red_packet_details(红包详情表)】的数据库操作Mapper
 * @createDate 2025-07-23 10:00:00
 * @Entity com.zheng.aicommunitybackend.domain.entity.RedPacketDetails
 */
@Mapper
public interface RedPacketDetailsMapper extends BaseMapper<RedPacketDetails> {

    /**
     * 批量插入红包详情（预分配红包时使用）
     * @param details 红包详情列表
     * @return 插入行数
     */
    int batchInsert(@Param("details") List<RedPacketDetails> details);

    /**
     * 原子性抢红包（更新红包状态）
     * @param id 红包详情ID
     * @param userId 用户ID
     * @param grabTime 抢红包时间
     * @return 更新行数（1表示抢成功，0表示已被抢）
     */
    @Update("UPDATE red_packet_details SET " +
            "status = 1, " +
            "user_id = #{userId}, " +
            "grab_time = #{grabTime} " +
            "WHERE id = #{id} AND status = 0")
    int grabRedPacket(@Param("id") Long id, @Param("userId") Long userId, @Param("grabTime") Date grabTime);

    /**
     * 查询活动的可抢红包ID列表（用于Redis预加载）
     * @param activityId 活动ID
     * @return 可抢红包ID列表
     */
    List<Long> selectAvailablePacketIds(@Param("activityId") Long activityId);

    /**
     * 查询活动的红包统计信息
     * @param activityId 活动ID
     * @return 统计信息Map，包含total_count, grabbed_count, total_amount, grabbed_amount
     */
    java.util.Map<String, Object> selectActivityStats(@Param("activityId") Long activityId);

    /**
     * 查询用户在某活动中抢到的红包
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 红包详情
     */
    RedPacketDetails selectUserGrabbedPacket(@Param("activityId") Long activityId, @Param("userId") Long userId);

    /**
     * 查询活动的红包分配情况（管理端查看）
     * @param activityId 活动ID
     * @param status 红包状态（可选）
     * @return 红包详情列表
     */
    List<RedPacketDetails> selectActivityPackets(@Param("activityId") Long activityId, @Param("status") Integer status);
}
