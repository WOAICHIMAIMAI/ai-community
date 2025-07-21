package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.LoginRequest;
import com.zheng.aicommunitybackend.domain.dto.RegisterRequest;
import com.zheng.aicommunitybackend.domain.dto.UserInfoDTO;
import com.zheng.aicommunitybackend.domain.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.LoginUserVO;

/**
* @author ZhengJJ
* @description 针对表【users(用户基础信息表)】的数据库操作Service
* @createDate 2025-07-10 11:03:49
*/
public interface UsersService extends IService<Users> {

    LoginUserVO login(LoginRequest loginRequest);

    void register(RegisterRequest registerRequest);

    boolean isAdmin();

    UserInfoDTO getCurrentUserInfo();

    void updateUserInfo(UserInfoDTO userInfoDTO);

    void changePassword(String oldPassword, String newPassword);

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoDTO getUserInfoById(Long userId);
}
