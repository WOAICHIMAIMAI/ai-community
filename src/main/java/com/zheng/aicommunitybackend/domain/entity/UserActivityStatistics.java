package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户活跃度统计表
 * @TableName user_activity_statistics
 */
@TableName(value = "user_activity_statistics")
@Data
public class UserActivityStatistics implements Serializable {

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
     * 日活跃用户数(DAU)
     */
    private Integer dailyActiveUsers;

    /**
     * 周活跃用户数(WAU)
     */
    private Integer weeklyActiveUsers;

    /**
     * 月活跃用户数(MAU)
     */
    private Integer monthlyActiveUsers;

    /**
     * 新增用户数
     */
    private Integer newUsers;

    /**
     * 登录用户数
     */
    private Integer loginUsers;

    /**
     * 提交保修工单用户数
     */
    private Integer repairOrderUsers;

    /**
     * 预约服务用户数
     */
    private Integer appointmentUsers;

    /**
     * 发帖用户数
     */
    private Integer postUsers;

    /**
     * 付费用户数
     */
    private Integer payingUsers;

    /**
     * 用户留存率
     */
    private BigDecimal retentionRate;

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
