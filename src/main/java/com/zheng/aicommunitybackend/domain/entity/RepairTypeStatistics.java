package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 保修类型统计表
 * @TableName repair_type_statistics
 */
@TableName(value = "repair_type_statistics")
@Data
public class RepairTypeStatistics implements Serializable {

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
     * 保修类型
     */
    private String repairType;

    /**
     * 工单数量
     */
    private Integer orderCount;

    /**
     * 已完成数量
     */
    private Integer completedCount;

    /**
     * 平均处理时长(小时)
     */
    private BigDecimal avgProcessTime;

    /**
     * 平均满意度评分
     */
    private BigDecimal avgSatisfactionScore;

    /**
     * 占比百分数
     */
    private BigDecimal percentage;

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
