package com.zheng.aicommunitybackend.dto;

import lombok.Data;

/**
 * 用户活跃度DTO
 */
@Data
public class UserActivityDTO {

    /**
     * 活动日期
     */
    private String activityDate;

    /**
     * 活跃用户数
     */
    private Integer activeUsers;

    /**
     * 总订单数
     */
    private Integer totalOrders;
}
