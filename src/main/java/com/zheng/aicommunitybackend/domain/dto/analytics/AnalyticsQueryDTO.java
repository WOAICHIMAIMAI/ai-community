package com.zheng.aicommunitybackend.domain.dto.analytics;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 数据分析查询DTO
 */
@Data
public class AnalyticsQueryDTO {

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private Date startDate;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    private Date endDate;

    /**
     * 统计类型：1-日统计 2-周统计 3-月统计 4-季度统计 5-年统计
     */
    private Integer statisticsType;

    /**
     * 服务类型（可选）
     */
    private String serviceType;

    /**
     * 保修类型（可选）
     */
    private String repairType;
}
