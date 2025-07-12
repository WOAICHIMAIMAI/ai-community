package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 用户信息DTO
 */
@Schema(description = "用户信息")
public class UserInfoDTO {
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long id;
    
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
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickname;
    
    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatarUrl;
    
    /**
     * 性别：0-未知 1-男 2-女
     */
    @Schema(description = "性别：0-未知 1-男 2-女")
    private Integer gender;
    
    /**
     * 出生日期
     */
    @Schema(description = "出生日期")
    private Date birthday;
    
    /**
     * 注册时间
     */
    @Schema(description = "注册时间")
    private Date registerTime;
    
    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    private Date lastLoginTime;
    
    /**
     * 是否已实名认证
     */
    @Schema(description = "是否已实名认证")
    private Integer isVerified;
    
    /**
     * 用户角色
     */
    @Schema(description = "用户角色：admin-管理员 user-普通用户")
    private String role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Date getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Date registerTime) {
        this.registerTime = registerTime;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Integer getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Integer isVerified) {
        this.isVerified = isVerified;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
} 