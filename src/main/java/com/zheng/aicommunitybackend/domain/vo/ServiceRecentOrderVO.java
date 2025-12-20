package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 服务最近预约VO
 */
@Data
@Schema(description = "服务最近预约")
public class ServiceRecentOrderVO implements Serializable {
    
    @Schema(description = "预约ID")
    private Long id;
    
    @Schema(description = "用户昵称")
    private String username;
    
    @Schema(description = "预约时间")
    private Date appointmentTime;
    
    @Schema(description = "订单状态：0-待处理 1-已确认 2-进行中 3-已完成 4-已取消")
    private Integer status;
    
    @Schema(description = "服务地址")
    private String address;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "创建时间")
    private Date createTime;
}

