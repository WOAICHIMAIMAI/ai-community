package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 管理端维修进度分页查询DTO
 */
@Data
@Schema(description = "管理端维修进度分页查询DTO")
public class AdminProgressPageQuery extends PageQuery {
    
    /**
     * 关联工单ID
     */
    @Schema(description = "关联工单ID")
    private Long orderId;
    
    /**
     * 操作人类型：1-用户 2-维修工 3-系统 4-管理员
     */
    @Schema(description = "操作人类型：1-用户 2-维修工 3-系统 4-管理员")
    private Integer operatorType;
    
    /**
     * 操作人ID
     */
    @Schema(description = "操作人ID")
    private Long operatorId;
    
    /**
     * 操作类型
     */
    @Schema(description = "操作类型")
    private String action;
    
    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private Date startTime;
    
    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private Date endTime;
    
    /**
     * 关键词（操作描述）
     */
    @Schema(description = "关键词（操作描述）")
    private String keyword;
} 