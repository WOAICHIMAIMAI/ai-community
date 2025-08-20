package com.zheng.aicommunitybackend.domain.dto.analytics;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户活跃度DTO
 */
@Data
public class UserActivityDTO {

    /**
     * 日期
     */
    private Date date;

    /**
     * 日期字符串
     */
    private String dateStr;

    /**
     * 日活跃用户数
     */
    private Integer dailyActiveUsers;

    /**
     * 新增用户数
     */
    private Integer newUsers;

    /**
     * 登录用户数
     */
    private Integer loginUsers;

    /**
     * 提交工单用户数
     */
    private Integer orderUsers;

    /**
     * 发帖用户数
     */
    private Integer postUsers;

    /**
     * 用户留存率
     */
    private BigDecimal retentionRate;
}
