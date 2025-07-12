package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.domain.dto.VerificationSubmitDTO;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.VerificationVO;
import com.zheng.aicommunitybackend.service.UserVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
     * 上传身份证照片
     *
     * @param file 图片文件
     * @param type 图片类型：front-正面，back-反面
     * @return 图片URL
     * @throws IOException 如果上传失败
     */
    @PostMapping("/upload")
    @Operation(summary = "上传身份证照片", description = "上传身份证正面或反面照片")
    public Result<String> uploadIdCardImage(
            @RequestParam("file") @Parameter(description = "身份证照片文件") MultipartFile file,
            @RequestParam("type") @Parameter(description = "照片类型：front-正面，back-反面") String type) throws IOException {
        log.info("上传身份证照片：type={}", type);
        String imageUrl = userVerificationService.uploadIdCardImage(file, type);
        return Result.success(imageUrl);
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