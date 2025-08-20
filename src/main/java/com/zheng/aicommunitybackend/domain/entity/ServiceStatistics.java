package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 服务统计表
 * @TableName service_statistics
 */
@TableName(value = "service_statistics")
@Data
public class ServiceStatistics implements Serializable {

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
     * 服务类型
     */
    private String serviceType;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 订单数量
     */
    private Integer orderCount;

    /**
     * 已完成数量
     */
    private Integer completedCount;

    /**
     * 完成率
     */
    private BigDecimal completionRate;

    /**
     * 总营收
     */
    private BigDecimal totalRevenue;

    /**
     * 平均满意度评分
     */
    private BigDecimal avgRating;

    /**
     * 同比增长率
     */
    private BigDecimal growthRate;

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
