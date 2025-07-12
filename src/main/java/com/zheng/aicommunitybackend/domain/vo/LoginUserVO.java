package com.zheng.aicommunitybackend.domain.vo;

import lombok.Data;

@Data
public class LoginUserVO {
    private String username;
    private String nickName;
    private Integer role;
    private String token;
    private String avatarUrl;
}
