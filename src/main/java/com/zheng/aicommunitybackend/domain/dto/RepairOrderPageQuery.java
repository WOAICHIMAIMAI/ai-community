package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 报修工单分页查询DTO
 */
@Data
@Schema(description = "报修工单分页查询DTO")
public class RepairOrderPageQuery extends PageQuery {
    
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
     * 关键词（标题或描述）
     */
    @Schema(description = "关键词（标题或描述）")
    private String keyword;
} 