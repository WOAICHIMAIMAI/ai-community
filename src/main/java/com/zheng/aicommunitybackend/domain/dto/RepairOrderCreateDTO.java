package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 报修工单创建DTO
 */
@Data
@Schema(description = "报修工单创建DTO")
public class RepairOrderCreateDTO {
    
    /**
     * 报修地址
     */
    @NotBlank(message = "报修地址不能为空")
    @Schema(description = "报修地址", required = true)
    private String address;
    
    /**
     * 报修类型：水电/门窗/家电等
     */
    @NotBlank(message = "报修类型不能为空")
    @Schema(description = "报修类型：水电/门窗/家电等", required = true)
    private String repairType;
    
    /**
     * 报修标题
     */
    @NotBlank(message = "报修标题不能为空")
    @Schema(description = "报修标题", required = true)
    private String title;
    
    /**
     * 详细描述
     */
    @NotBlank(message = "报修描述不能为空")
    @Schema(description = "详细描述", required = true)
    private String description;
    
    /**
     * 现场照片URL，多个用逗号分隔
     */
    @Schema(description = "现场照片URL，多个用逗号分隔")
    private String images;
    
    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Schema(description = "联系电话", required = true)
    private String contactPhone;
    
    /**
     * 期望上门时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "期望上门时间")
    private Date expectedTime;
} 