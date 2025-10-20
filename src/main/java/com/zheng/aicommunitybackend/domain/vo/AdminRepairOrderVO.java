package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 管理端报修工单VO
 */
@Data
@Schema(description = "管理端报修工单VO")
public class AdminRepairOrderVO {
    
    /**
     * 报修单ID
     */
    @Schema(description = "报修单ID")
    private Long id;
    
    /**
     * 报修单编号
     */
    @Schema(description = "报修单编号")
    private String orderNumber;
    
    /**
     * 报修用户ID
     */
    @Schema(description = "报修用户ID")
    private Long userId;
    
    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名")
    private String userName;
    
    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String userNickname;
    
    /**
     * 报修地址ID
     */
    @Schema(description = "报修地址ID")
    private Long addressId;
    
    /**
     * 报修地址详情
     */
    @Schema(description = "报修地址详情")
    private String addressDetail;
    
    /**
     * 报修类型：水电/门窗/家电等
     */
    @Schema(description = "报修类型：水电/门窗/家电等")
    private String repairType;
    
    /**
     * 报修标题
     */
    @Schema(description = "报修标题")
    private String title;
    
    /**
     * 详细描述
     */
    @Schema(description = "详细描述")
    private String description;
    
    /**
     * 现场照片URL，多个用逗号分隔
     */
    @Schema(description = "现场照片URL，多个用逗号分隔")
    private String images;
    
    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String contactPhone;
    
    /**
     * 期望上门时间
     */
    @Schema(description = "期望上门时间")
    private Date expectedTime;
    
    /**
     * 紧急程度：1-一般 2-紧急 3-非常紧急
     */
    @Schema(description = "紧急程度：1-一般 2-紧急 3-非常紧急")
    private Integer urgencyLevel;
    
    /**
     * 状态：0-待受理 1-已分配 2-处理中 3-已完成 4-已取消
     */
    @Schema(description = "状态：0-待受理 1-已分配 2-处理中 3-已完成 4-已取消")
    private Integer status;
    
    /**
     * 状态描述
     */
    @Schema(description = "状态描述")
    private String statusDesc;
    
    /**
     * 维修工ID
     */
    @Schema(description = "维修工ID")
    private Long workerId;
    
    /**
     * 维修工姓名
     */
    @Schema(description = "维修工姓名")
    private String workerName;
    
    /**
     * 维修工电话
     */
    @Schema(description = "维修工电话")
    private String workerPhone;
    
    /**
     * 维修工头像URL
     */
    @Schema(description = "维修工头像URL")
    private String workerAvatar;
    
    /**
     * 预约上门时间
     */
    @Schema(description = "预约上门时间")
    private Date appointmentTime;
    
    /**
     * 完成时间
     */
    @Schema(description = "完成时间")
    private Date completionTime;
    
    /**
     * 满意度评价：1-5星
     */
    @Schema(description = "满意度评价：1-5星")
    private Integer satisfactionLevel;
    
    /**
     * 用户反馈
     */
    @Schema(description = "用户反馈")
    private String feedback;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;
} 