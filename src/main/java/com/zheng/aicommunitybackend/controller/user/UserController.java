package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.domain.dto.UserInfoDTO;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户相关接口
 */
@RestController
@RequestMapping("/user/user")
@Slf4j
public class UserController {

    private final UsersService usersService;

    public UserController(UsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoDTO> getCurrentUserInfo() {
        return Result.success(usersService.getCurrentUserInfo());
    }


    /**
     * 更新用户信息
     *
     * @param userInfoDTO 用户信息
     * @return 更新结果
     */
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody UserInfoDTO userInfoDTO) {
        usersService.updateUserInfo(userInfoDTO);
        return Result.success();
    }

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        usersService.changePassword(oldPassword, newPassword);
        return Result.success();
    }
} 