package com.zheng.aicommunitybackend.domain.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体类
 */
public class Users implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户唯一标识ID
     */
    private Long id;
    
    /**
     * 用户名，用于登录
     */
    private String username;
    
    /**
     * 加密后的密码
     */
    private String password;
    
    /**
     * 手机号，用于登录和联系
     */
    private String phone;
    
    /**
     * 用户昵称，显示用
     */
    private String nickname;
    
    /**
     * 头像图片URL
     */
    private String avatarUrl;
    
    /**
     * 性别：0-未知 1-男 2-女
     */
    private Integer gender;
    
    /**
     * 出生日期
     */
    private Date birthday;
    
    /**
     * 注册时间
     */
    private Date registerTime;
    
    /**
     * 最后登录时间
     */
    private Date lastLoginTime;
    
    /**
     * 账号状态：0-禁用 1-正常 2-未激活
     */
    private Integer status;
    
    /**
     * 是否完成实名认证
     */
    private Integer isVerified;
    
    /**
     * 用户角色：0-普通用户 1-管理员
     */
    private Integer role;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Integer isVerified) {
        this.isVerified = isVerified;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Users{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", phone='" + phone + '\'' +
                ", nickname='" + nickname + '\'' +
                ", gender=" + gender +
                ", status=" + status +
                ", isVerified=" + isVerified +
                ", role=" + role +
                '}';
    }
}