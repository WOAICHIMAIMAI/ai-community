package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 管理端报修工单分页查询DTO
 */
@Data
@Schema(description = "管理端报修工单分页查询DTO")
public class AdminRepairOrderPageQuery extends PageQuery {
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;
    
    /**
     * 报修单编号
     */
    @Schema(description = "报修单编号")
    private String orderNumber;
    
    /**
     * 报修类型：水电/门窗/家电等
     */
    @Schema(description = "报修类型")
    private String repairType;
    
    /**
     * 工单状态：0-待受理 1-已分配 2-处理中 3-已完成 4-已取消
     */
    @Schema(description = "工单状态：0-待受理 1-已分配 2-处理中 3-已完成 4-已取消")
    private Integer status;
    
    /**
     * 维修工ID
     */
    @Schema(description = "维修工ID")
    private Long workerId;
    
    /**
     * 创建开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建开始时间")
    private Date startTime;
    
    /**
     * 创建结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建结束时间")
    private Date endTime;
    
    /**
     * 关键词（标题或描述或用户联系方式）
     */
    @Schema(description = "关键词（标题或描述或用户联系方式）")
    private String keyword;
    
    /**
     * 紧急程度：1-一般 2-紧急 3-非常紧急
     */
    @Schema(description = "紧急程度：1-一般 2-紧急 3-非常紧急")
    private Integer urgencyLevel;
} 