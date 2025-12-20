package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.domain.dto.VerificationSubmitDTO;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.VerificationVO;
import com.zheng.aicommunitybackend.service.UserVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户实名认证相关接口
 */
@RestController
@RequestMapping("/user/verification")
@Slf4j
@Tag(name = "用户实名认证", description = "用户实名认证相关接口")
public class UserVerificationController {

    private final UserVerificationService userVerificationService;

    public UserVerificationController(UserVerificationService userVerificationService) {
        this.userVerificationService = userVerificationService;
    }

    /**
     * 提交实名认证申请
     *
     * @param verificationSubmitDTO 实名认证信息
     * @return 提交结果
     */
    @PostMapping("/submit")
    @Operation(summary = "提交实名认证申请", description = "提交用户的实名认证信息")
    public Result<Void> submitVerification(@RequestBody @Valid VerificationSubmitDTO verificationSubmitDTO) {
        log.info("用户提交实名认证申请");
        boolean success = userVerificationService.submitVerification(verificationSubmitDTO);
        return success ? Result.success() : Result.error("提交失败");
    }

    /**
     * 获取当前用户的认证信息
     *
     * @return 认证信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取认证信息", description = "获取当前用户的实名认证信息")
    public Result<VerificationVO> getVerificationInfo() {
        log.info("获取用户认证信息");
        VerificationVO verificationVO = userVerificationService.getCurrentUserVerification();
        return Result.success(verificationVO);
    }

    /**
     * 检查当前用户是否已实名认证
     *
     * @return 是否已认证
     */
    @GetMapping("/status")
    @Operation(summary = "检查认证状态", description = "检查当前用户是否已通过实名认证")
    public Result<Boolean> checkVerificationStatus() {
        log.info("检查用户认证状态");
        boolean isVerified = userVerificationService.checkUserVerificationStatus();
        return Result.success(isVerified);
    }

    /**
     * 取消认证申请
     *
     * @return 取消结果
     */
    @PostMapping("/cancel")
    @Operation(summary = "取消认证申请", description = "取消进行中的实名认证申请")
    public Result<Void> cancelVerification() {
        log.info("用户取消认证申请");
        boolean success = userVerificationService.cancelVerification();
        return success ? Result.success() : Result.error("取消失败");
    }
} 