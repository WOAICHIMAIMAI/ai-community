package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zheng.aicommunitybackend.domain.entity.DataAnalyticsSummary;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据分析汇总表 Mapper接口
 */
@Mapper
public interface DataAnalyticsSummaryMapper extends BaseMapper<DataAnalyticsSummary> {

}
