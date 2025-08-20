package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.aicommunitybackend.domain.entity.RedPacketRecords;
import com.zheng.aicommunitybackend.domain.vo.RedPacketRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author ZhengJJ
 * @description 针对表【red_packet_records(抢红包记录表)】的数据库操作Mapper
 * @createDate 2025-07-23 10:00:00
 * @Entity com.zheng.aicommunitybackend.domain.entity.RedPacketRecords
 */
@Mapper
public interface RedPacketRecordsMapper extends BaseMapper<RedPacketRecords> {

    /**
     * 分页查询用户抢红包记录
     * @param page 分页参数
     * @param userId 用户ID
     * @param activityId 活动ID（可选）
     * @return 抢红包记录列表
     */
    IPage<RedPacketRecordVO> selectUserRecordPage(
            Page<RedPacketRecordVO> page,
            @Param("userId") Long userId,
            @Param("activityId") Long activityId
    );

    /**
     * 分页查询活动的抢红包记录（管理端）
     * @param page 分页参数
     * @param activityId 活动ID
     * @return 抢红包记录列表
     */
    IPage<RedPacketRecordVO> selectActivityRecordPage(
            Page<RedPacketRecordVO> page,
            @Param("activityId") Long activityId
    );

    /**
     * 查询用户在某活动中的抢红包记录
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 抢红包记录
     */
    RedPacketRecordVO selectUserActivityRecord(@Param("userId") Long userId, @Param("activityId") Long activityId);

    /**
     * 更新账户更新状态
     * @param id 记录ID
     * @param accountUpdated 账户更新状态
     * @return 更新行数
     */
    @Update("UPDATE red_packet_records SET account_updated = #{accountUpdated} WHERE id = #{id}")
    int updateAccountStatus(@Param("id") Long id, @Param("accountUpdated") Integer accountUpdated);

    /**
     * 查询未更新账户的记录（用于补偿处理）
     * @param limit 查询数量限制
     * @return 未更新账户的记录列表
     */
    List<RedPacketRecords> selectUnprocessedRecords(@Param("limit") Integer limit);

    /**
     * 查询活动的抢红包统计
     * @param activityId 活动ID
     * @return 统计信息
     */
    java.util.Map<String, Object> selectActivityRecordStats(@Param("activityId") Long activityId);

    /**
     * 查询用户的抢红包统计
     * @param userId 用户ID
     * @return 统计信息
     */
    java.util.Map<String, Object> selectUserRecordStats(@Param("userId") Long userId);
}
