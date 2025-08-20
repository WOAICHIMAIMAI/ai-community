package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zheng.aicommunitybackend.domain.entity.ServiceStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 服务统计表 Mapper接口
 */
@Mapper
public interface ServiceStatisticsMapper extends BaseMapper<ServiceStatistics> {

}
