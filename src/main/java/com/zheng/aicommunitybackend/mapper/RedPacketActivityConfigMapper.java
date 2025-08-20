package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zheng.aicommunitybackend.domain.entity.RedPacketActivityConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author ZhengJJ
 * @description 针对表【red_packet_activity_config(红包活动配置表)】的数据库操作Mapper
 * @createDate 2025-07-23 10:00:00
 * @Entity com.zheng.aicommunitybackend.domain.entity.RedPacketActivityConfig
 */
@Mapper
public interface RedPacketActivityConfigMapper extends BaseMapper<RedPacketActivityConfig> {

    /**
     * 查询活动的所有配置
     * @param activityId 活动ID
     * @return 配置列表
     */
    List<RedPacketActivityConfig> selectByActivityId(@Param("activityId") Long activityId);

    /**
     * 查询活动的配置Map（key-value形式）
     * @param activityId 活动ID
     * @return 配置Map
     */
    Map<String, String> selectConfigMap(@Param("activityId") Long activityId);

    /**
     * 查询活动的特定配置值
     * @param activityId 活动ID
     * @param configKey 配置键
     * @return 配置值
     */
    String selectConfigValue(@Param("activityId") Long activityId, @Param("configKey") String configKey);

    /**
     * 批量插入配置
     * @param configs 配置列表
     * @return 插入行数
     */
    int batchInsert(@Param("configs") List<RedPacketActivityConfig> configs);

    /**
     * 删除活动的所有配置
     * @param activityId 活动ID
     * @return 删除行数
     */
    int deleteByActivityId(@Param("activityId") Long activityId);
}
