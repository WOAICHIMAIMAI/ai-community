package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 维修工评价VO
 */
@Data
@Schema(description = "维修工评价VO")
public class WorkerReviewVO {
    
    /**
     * 工单ID
     */
    @Schema(description = "工单ID")
    private Long id;
    
    /**
     * 用户ID（脱敏显示）
     */
    @Schema(description = "用户ID（脱敏显示）")
    private String userId;
    
    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String userName;
    
    /**
     * 评分（1-5星）
     */
    @Schema(description = "评分（1-5星）")
    private Integer rating;
    
    /**
     * 评价内容
     */
    @Schema(description = "评价内容")
    private String content;
    
    /**
     * 评价时间
     */
    @Schema(description = "评价时间")
    private Date createTime;
    
    /**
     * 工单标题
     */
    @Schema(description = "工单标题")
    private String orderTitle;
    
    /**
     * 报修类型
     */
    @Schema(description = "报修类型")
    private String repairType;
}

