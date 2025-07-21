package com.zheng.aicommunitybackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.LoginRequest;
import com.zheng.aicommunitybackend.domain.dto.RegisterRequest;
import com.zheng.aicommunitybackend.domain.dto.UserInfoDTO;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.LoginUserVO;
import com.zheng.aicommunitybackend.exception.BaseException;
import com.zheng.aicommunitybackend.mapper.UsersMapper;
import com.zheng.aicommunitybackend.service.UsersService;
import com.zheng.aicommunitybackend.domain.entity.Users;
import com.zheng.aicommunitybackend.util.DigestUtil;
import com.zheng.aicommunitybackend.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
* @author ZhengJJ
* @description 针对表【users(用户基础信息表)】的数据库操作Service实现
* @createDate 2025-07-10 11:03:49
*/
@Service
@AllArgsConstructor
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService {

    private final JwtUtil jwtUtil;

    @Override
    public LoginUserVO login(LoginRequest loginRequest) {
        if(loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()){
            throw new BaseException("用户名不能为空!");
        }
        if(loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()){
            throw new BaseException("密码不能为空!");
        }
        Users userDB = lambdaQuery().eq(Users::getUsername, loginRequest.getUsername()).one();
        if(userDB == null){
            throw new BaseException("请检查用户名是否有误！");
        }
        if(!userDB.getPassword().equals(DigestUtil.md5Hex(loginRequest.getPassword()))){
            throw new BaseException("密码错误，请重新输入！");
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setUsername(userDB.getUsername());
        loginUserVO.setRole(userDB.getRole());
        loginUserVO.setNickName(userDB.getNickname());
        loginUserVO.setAvatarUrl(userDB.getAvatarUrl());
        String token = jwtUtil.generateToken(userDB);
        loginUserVO.setToken(token);
        return loginUserVO;
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        // TODO 参数校验
        if(!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())){
            throw new BaseException("两次输入的密码不一致！");
        }
        Users users = BeanUtil.copyProperties(registerRequest, Users.class);
        users.setPassword(DigestUtil.md5Hex(registerRequest.getPassword()));
        users.setRole(0);
        save(users);
    }

    @Override
    public boolean isAdmin() {
        return UserContext.getUserRole() == 1;
    }

    @Override
    public UserInfoDTO getCurrentUserInfo() {
        Long userId = UserContext.getUserId();
        Users userDB = getById(userId);
        return BeanUtil.copyProperties(userDB, UserInfoDTO.class);
    }

    @Override
    public void updateUserInfo(UserInfoDTO userInfoDTO) {
        Users users = BeanUtil.copyProperties(userInfoDTO, Users.class);
        updateById(users);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        if(oldPassword == null || oldPassword.isEmpty()){
            throw new BaseException("请输入旧密码！");
        }
        if(newPassword == null || newPassword.isEmpty()){
            throw new BaseException("请输入新密码！");
        }
        if(newPassword.equals(oldPassword)){
            throw new BaseException("新密码不能与旧密码相同！");
        }
        Users userDB = getById(UserContext.getUserId());
        if(!userDB.getPassword().equals(DigestUtil.md5Hex(oldPassword))){
            throw new BaseException("旧密码错误！");
        }
        userDB.setPassword(DigestUtil.md5Hex(newPassword));
        updateById(userDB);
    }

    @Override
    public UserInfoDTO getUserInfoById(Long userId) {
        // 根据用户ID查询用户信息
        Users user = this.getById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        
        // 转换为DTO
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtil.copyProperties(user, userInfoDTO);
        
        return userInfoDTO;
    }
}
