package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 注册请求DTO
 */
@Schema(description = "注册请求")
public class RegisterRequest {
    /**
     * 用户名
     */
    @Schema(description = "用户名", required = true)
    private String username;
    
    /**
     * 密码
     */
    @Schema(description = "密码", required = true)
    private String password;
    
    /**
     * 确认密码
     */
    @Schema(description = "确认密码", required = true)
    private String confirmPassword;
    
    /**
     * 手机号
     */
    @Schema(description = "手机号", required = true)
    private String phone;
    
    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickname;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
} 