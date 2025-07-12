package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 实名认证分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "实名认证分页查询参数")
public class VerificationPageQuery extends PageQuery {
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;
    
    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名，支持模糊查询")
    private String realName;
    
    /**
     * 身份证号
     */
    @Schema(description = "身份证号，支持模糊查询")
    private String idCardNumber;
    
    /**
     * 认证状态：0-未认证 1-认证中 2-已认证 3-认证失败
     */
    @Schema(description = "认证状态：0-未认证 1-认证中 2-已认证 3-认证失败")
    private Integer verificationStatus;
    
    /**
     * 开始时间
     */
    @Schema(description = "开始时间，格式：yyyy-MM-dd HH:mm:ss")
    private String startTime;
    
    /**
     * 结束时间
     */
    @Schema(description = "结束时间，格式：yyyy-MM-dd HH:mm:ss")
    private String endTime;
} 