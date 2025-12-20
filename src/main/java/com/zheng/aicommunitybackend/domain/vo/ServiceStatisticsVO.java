package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务统计数据VO
 */
@Data
@Schema(description = "服务统计数据")
public class ServiceStatisticsVO implements Serializable {
    
    @Schema(description = "总预约数")
    private Integer totalOrders;
    
    @Schema(description = "已完成订单数")
    private Integer completedOrders;
    
    @Schema(description = "平均评分")
    private BigDecimal averageRating;
    
    @Schema(description = "总收入")
    private BigDecimal totalRevenue;
    
    @Schema(description = "待处理订单数")
    private Integer pendingOrders;
    
    @Schema(description = "进行中订单数")
    private Integer inProgressOrders;
    
    @Schema(description = "已取消订单数")
    private Integer cancelledOrders;
}

