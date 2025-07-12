package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 用户认证信息VO
 */
@Data
@Schema(description = "用户认证信息")
public class VerificationVO {
    
    /**
     * 认证记录ID
     */
    @Schema(description = "认证记录ID")
    private Long id;
    
    /**
     * 真实姓名（脱敏）
     */
    @Schema(description = "真实姓名（脱敏）")
    private String realName;
    
    /**
     * 身份证号（脱敏）
     */
    @Schema(description = "身份证号（脱敏）")
    private String idCardNumber;
    
    /**
     * 认证状态：0-未认证 1-认证中 2-已认证 3-认证失败
     */
    @Schema(description = "认证状态：0-未认证 1-认证中 2-已认证 3-认证失败")
    private Integer verificationStatus;
    
    /**
     * 认证失败原因
     */
    @Schema(description = "认证失败原因，仅当状态为认证失败时有值")
    private String failureReason;
    
    /**
     * 提交认证时间
     */
    @Schema(description = "提交认证时间")
    private Date submitTime;
    
    /**
     * 认证完成时间
     */
    @Schema(description = "认证完成时间")
    private Date completeTime;
} 