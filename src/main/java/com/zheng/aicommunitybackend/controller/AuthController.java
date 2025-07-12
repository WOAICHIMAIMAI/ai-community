package com.zheng.aicommunitybackend.controller;

import com.zheng.aicommunitybackend.common.Result;
import com.zheng.aicommunitybackend.domain.dto.LoginRequest;
import com.zheng.aicommunitybackend.domain.dto.RegisterRequest;
import com.zheng.aicommunitybackend.domain.vo.LoginUserVO;
import com.zheng.aicommunitybackend.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 认证相关接口
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证接口", description = "用户登录注册相关接口")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final UsersService usersService;

    public AuthController(UsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录结果（JWT令牌）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录并获取JWT令牌")
    public Result<LoginUserVO> login(@RequestBody LoginRequest loginRequest) {
        log.info("用户登录: {}", loginRequest.getUsername());
        LoginUserVO loginUserVO = usersService.login(loginRequest);
        return Result.success(loginUserVO);
    }

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户")
    public Result<Void> register(@RequestBody RegisterRequest registerRequest) {
        log.info("用户注册: {}", registerRequest.getUsername());
        usersService.register(registerRequest);
        return Result.success();
    }
} 