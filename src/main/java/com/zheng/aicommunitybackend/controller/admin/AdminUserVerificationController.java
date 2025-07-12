package com.zheng.aicommunitybackend.controller.admin;

import com.zheng.aicommunitybackend.domain.dto.VerificationAuditDTO;
import com.zheng.aicommunitybackend.domain.dto.VerificationPageQuery;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.AdminVerificationVO;
import com.zheng.aicommunitybackend.service.UserVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户实名认证控制器
 */
@RestController
@RequestMapping("/admin/verification")
@Tag(name = "管理员实名认证接口")
@Slf4j
public class AdminUserVerificationController {

    private final UserVerificationService userVerificationService;

    @Autowired
    public AdminUserVerificationController(UserVerificationService userVerificationService) {
        this.userVerificationService = userVerificationService;
    }

    /**
     * 分页查询实名认证列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询实名认证列表")
    @PostMapping("/list")
    public Result<PageResult> listVerifications(@RequestBody VerificationPageQuery query) {
        log.info("管理员查询实名认证列表，参数：{}", query);
        PageResult result = userVerificationService.adminListVerifications(query);
        return Result.success(result);
    }

    /**
     * 获取实名认证详情
     *
     * @param id 认证记录ID
     * @return 认证详情
     */
    @Operation(summary = "获取实名认证详情")
    @GetMapping("/detail/{id}")
    public Result<AdminVerificationVO> getVerificationDetail(
            @Parameter(description = "认证记录ID", required = true)
            @PathVariable("id") Long id) {
        log.info("管理员获取实名认证详情，ID：{}", id);
        AdminVerificationVO vo = userVerificationService.adminGetVerificationDetail(id);
        return Result.success(vo);
    }

    /**
     * 审核实名认证
     *
     * @param auditDTO 审核信息
     * @return 审核结果
     */
    @Operation(summary = "审核实名认证")
    @PostMapping("/admin/audit")
    public Result<Boolean> auditVerification(@Valid @RequestBody VerificationAuditDTO auditDTO) {
        log.info("管理员审核实名认证，参数：{}", auditDTO);
        boolean result = userVerificationService.adminAuditVerification(auditDTO);
        return Result.success(result);
    }

    /**
     * 统计各状态的认证数量
     *
     * @return 状态统计结果：[待审核数, 已通过数, 已拒绝数, 总数]
     */
    @Operation(summary = "统计各状态的认证数量")
    @GetMapping("/count")
    public Result<Integer[]> countVerificationStatus() {
        log.info("管理员统计实名认证状态数量");
        Integer[] counts = userVerificationService.adminCountVerificationStatus();
        return Result.success(counts);
    }
} 