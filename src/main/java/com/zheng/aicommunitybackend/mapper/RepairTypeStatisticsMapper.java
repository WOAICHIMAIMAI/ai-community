package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zheng.aicommunitybackend.domain.entity.RepairTypeStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 保修类型统计表 Mapper接口
 */
@Mapper
public interface RepairTypeStatisticsMapper extends BaseMapper<RepairTypeStatistics> {

}
