package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zheng.aicommunitybackend.domain.entity.UserActivityStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户活跃度统计表 Mapper接口
 */
@Mapper
public interface UserActivityStatisticsMapper extends BaseMapper<UserActivityStatistics> {

}
