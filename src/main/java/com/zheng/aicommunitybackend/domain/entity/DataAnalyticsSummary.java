package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 数据分析汇总表
 * @TableName data_analytics_summary
 */
@TableName(value = "data_analytics_summary")
@Data
public class DataAnalyticsSummary implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 统计日期
     */
    private Date statisticsDate;

    /**
     * 统计类型：1-日统计 2-周统计 3-月统计 4-季度统计 5-年统计
     */
    private Integer statisticsType;

    /**
     * 保修工单总数
     */
    private Integer totalRepairOrders;

    /**
     * 已完成保修工单数
     */
    private Integer completedRepairOrders;

    /**
     * 预约服务总数
     */
    private Integer totalAppointmentOrders;

    /**
     * 已完成预约服务数
     */
    private Integer completedAppointmentOrders;

    /**
     * 活跃用户数
     */
    private Integer activeUsers;

    /**
     * 新增用户数
     */
    private Integer newUsers;

    /**
     * 平均响应时长(分钟)
     */
    private BigDecimal avgResponseTime;

    /**
     * 平均满意度评分
     */
    private BigDecimal avgSatisfactionScore;

    /**
     * 总营收
     */
    private BigDecimal totalRevenue;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
