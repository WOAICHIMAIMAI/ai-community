package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 管理员查看的用户认证信息VO
 */
@Data
@Schema(description = "管理员查看的用户认证信息")
public class AdminVerificationVO {
    
    /**
     * 认证记录ID
     */
    @Schema(description = "认证记录ID")
    private Long id;
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;
    
    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;
    
    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;
    
    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    private String realName;
    
    /**
     * 身份证号
     */
    @Schema(description = "身份证号")
    private String idCardNumber;
    
    /**
     * 身份证正面照片URL
     */
    @Schema(description = "身份证正面照片URL")
    private String idCardFrontUrl;
    
    /**
     * 身份证反面照片URL
     */
    @Schema(description = "身份证反面照片URL")
    private String idCardBackUrl;
    
    /**
     * 认证状态：0-未认证 1-认证中 2-已认证 3-认证失败
     */
    @Schema(description = "认证状态：0-未认证 1-认证中 2-已认证 3-认证失败")
    private Integer verificationStatus;
    
    /**
     * 认证失败原因
     */
    @Schema(description = "认证失败原因")
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