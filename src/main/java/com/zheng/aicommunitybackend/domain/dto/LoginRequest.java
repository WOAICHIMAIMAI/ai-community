package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Schema(description = "登录请求")
@Data
public class LoginRequest {
    /**
     * 用户名或手机号
     */
    @Schema(description = "用户名或手机号", required = true)
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码", required = true)
    private String password;



} 