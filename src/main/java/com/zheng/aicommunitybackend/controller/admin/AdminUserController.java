package com.zheng.aicommunitybackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.aicommunitybackend.domain.dto.UserInfoDTO;
import com.zheng.aicommunitybackend.domain.entity.Users;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.exception.BaseException;
import com.zheng.aicommunitybackend.service.UsersService;
import com.zheng.aicommunitybackend.util.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员-用户管理接口
 */
@RestController
@RequestMapping("/admin/user")
@Slf4j
public class AdminUserController {

    private final UsersService usersService;

    public AdminUserController(UsersService usersService) {
        this.usersService = usersService;
    }
    
    /**
     * 检查是否具有管理员权限
     *
     * @return 权限检查结果
     */
    private boolean checkAdminPermission() {
        return usersService.isAdmin();
    }

    /**
     * 分页查询用户列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param keyword  搜索关键字（用户名、手机号、昵称）
     * @param status   状态过滤
     * @return 用户列表
     */
    @GetMapping("/list")
    public Result<Page<UserInfoDTO>> listUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        
        // 权限检查
        if (!checkAdminPermission()) {
            throw new BaseException("你没有权限进行此操作！");
        }

        Page<Users> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();

        // 添加搜索条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Users::getUsername, keyword)
                    .or().like(Users::getPhone, keyword)
                    .or().like(Users::getNickname, keyword);
        }

        // 添加状态过滤
        if (status != null) {
            queryWrapper.eq(Users::getStatus, status);
        }

        // 按注册时间倒序排序
        queryWrapper.orderByDesc(Users::getRegisterTime);

        // 执行查询
        Page<Users> userPage = usersService.page(page, queryWrapper);

        // 转换为UserInfoDTO
        Page<UserInfoDTO> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserInfoDTO> records = new ArrayList<>();

        for (Users user : userPage.getRecords()) {
            UserInfoDTO dto = new UserInfoDTO();
            BeanUtils.copyProperties(user, dto);
            // 设置角色信息
            dto.setRole(user.getRole() == 1 ? "admin" : "user");
            records.add(dto);
        }

        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态（0-禁用 1-正常 2-未激活）
     * @return 更新结果
     */
    @PutMapping("/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        // 权限检查
        if (!checkAdminPermission()) {
            throw new BaseException("你没有权限进行此操作！");
        }
        
        if (status != 0 && status != 1 && status != 2) {
            return Result.error("无效的状态值");
        }

        Users user = usersService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setStatus(status);
        boolean success = usersService.updateById(user);

        if (success) {
            return Result.success();
        } else {
            return Result.error("更新状态失败");
        }
    }

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @return 重置结果
     */
    @PostMapping("/{userId}/reset-password")
    public Result<String> resetPassword(@PathVariable Long userId) {
        // 权限检查
        if (!checkAdminPermission()) {
            throw new BaseException("你没有权限进行此操作！");
        }
        
        Users user = usersService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 重置密码为123456
        String newPassword = "123456";
        
        // 更新用户密码（加密存储）
        user.setPassword(DigestUtil.md5Hex(newPassword));
        boolean success = usersService.updateById(user);

        if (success) {
            return Result.success();
        } else {
            return Result.error("密码重置失败");
        }
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        // 权限检查
        if (!checkAdminPermission()) {
            throw new BaseException("你没有权限进行此操作！");
        }
        
        boolean success = usersService.removeById(userId);
        
        if (success) {
            return Result.success();
        } else {
            return Result.error("用户删除失败");
        }
    }
} 