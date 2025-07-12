package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 报修进度VO
 */
@Data
@Schema(description = "报修进度VO")
public class RepairProgressVO {

    /**
     * 主键ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 关联工单ID
     */
    @Schema(description = "工单ID")
    private Long orderId;

    /**
     * 操作人ID
     */
    @Schema(description = "操作人ID")
    private Long operatorId;

    /**
     * 操作人名称
     */
    @Schema(description = "操作人名称")
    private String operatorName;

    /**
     * 操作人类型：1-用户 2-维修工 3-系统 4-管理员
     */
    @Schema(description = "操作人类型：1-用户 2-维修工 3-系统 4-管理员")
    private Integer operatorType;
    
    /**
     * 操作人类型描述
     */
    @Schema(description = "操作人类型描述")
    private String operatorTypeDesc;

    /**
     * 操作类型
     */
    @Schema(description = "操作类型")
    private String action;

    /**
     * 操作描述
     */
    @Schema(description = "操作描述")
    private String description;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
} 