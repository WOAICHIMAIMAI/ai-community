package com.zheng.aicommunitybackend.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zheng.aicommunitybackend.common.Result;
import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.AccountRechargeDTO;
import com.zheng.aicommunitybackend.domain.dto.PaymentBillQueryDTO;
import com.zheng.aicommunitybackend.domain.dto.PaymentCreateDTO;
import com.zheng.aicommunitybackend.domain.vo.*;
import com.zheng.aicommunitybackend.service.PaymentBillsService;
import com.zheng.aicommunitybackend.service.PaymentRecordsService;
import com.zheng.aicommunitybackend.service.UserAccountsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 费用缴纳控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/payment")
@Tag(name = "费用缴纳", description = "费用缴纳相关接口")
public class PaymentController {

    @Autowired
    private PaymentBillsService paymentBillsService;

    @Autowired
    private PaymentRecordsService paymentRecordsService;

    @Autowired
    private UserAccountsService userAccountsService;

    /**
     * 获取用户账户信息
     */
    @GetMapping("/account")
    @Operation(summary = "获取用户账户信息", description = "获取当前用户的账户余额等信息")
    public Result<UserAccountVO> getUserAccount() {
        Long userId = UserContext.getUserId();
        log.info("获取用户账户信息 - userId: {}", userId);
        
        UserAccountVO account = userAccountsService.getAccountByUserId(userId);
        return Result.success(account);
    }

    /**
     * 账户充值
     */
    @PostMapping("/account/recharge")
    @Operation(summary = "账户充值", description = "用户账户余额充值")
    public Result<Boolean> recharge(@Valid @RequestBody AccountRechargeDTO rechargeDTO) {
        Long userId = UserContext.getUserId();
        log.info("账户充值 - userId: {}, amount: {}", userId, rechargeDTO.getAmount());
        
        boolean success = userAccountsService.recharge(userId, rechargeDTO);
        return Result.success(success);
    }

    /**
     * 获取待缴费账单列表
     */
    @GetMapping("/bills/pending")
    @Operation(summary = "获取待缴费账单", description = "获取当前用户的待缴费账单列表")
    public Result<List<PaymentBillVO>> getPendingBills() {
        Long userId = UserContext.getUserId();
        log.info("获取待缴费账单 - userId: {}", userId);
        
        List<PaymentBillVO> bills = paymentBillsService.getPendingBills(userId);
        return Result.success(bills);
    }

    /**
     * 获取待缴费总金额
     */
    @GetMapping("/bills/pending/amount")
    @Operation(summary = "获取待缴费总金额", description = "获取当前用户的待缴费总金额")
    public Result<BigDecimal> getPendingAmount() {
        Long userId = UserContext.getUserId();
        log.info("获取待缴费总金额 - userId: {}", userId);
        
        BigDecimal amount = paymentBillsService.getPendingAmount(userId);
        return Result.success(amount);
    }

    /**
     * 分页查询账单列表
     */
    @GetMapping("/bills")
    @Operation(summary = "分页查询账单列表", description = "根据条件分页查询账单列表")
    public Result<IPage<PaymentBillVO>> getBillPage(PaymentBillQueryDTO query) {
        Long userId = UserContext.getUserId();
        log.info("分页查询账单列表 - userId: {}, query: {}", userId, query);
        
        IPage<PaymentBillVO> page = paymentBillsService.getBillPage(userId, query);
        return Result.success(page);
    }

    /**
     * 获取账单详情
     */
    @GetMapping("/bills/{billId}")
    @Operation(summary = "获取账单详情", description = "根据账单ID获取账单详细信息")
    public Result<PaymentBillVO> getBillDetail(
            @Parameter(description = "账单ID") @PathVariable Long billId) {
        Long userId = UserContext.getUserId();
        log.info("获取账单详情 - userId: {}, billId: {}", userId, billId);
        
        PaymentBillVO bill = paymentBillsService.getBillDetail(billId, userId);
        return Result.success(bill);
    }

    /**
     * 创建缴费订单
     */
    @PostMapping("/orders")
    @Operation(summary = "创建缴费订单", description = "创建缴费订单，支持多种支付方式")
    public Result<PaymentRecordVO> createPaymentOrder(@Valid @RequestBody PaymentCreateDTO createDTO) {
        Long userId = UserContext.getUserId();
        log.info("创建缴费订单 - userId: {}, createDTO: {}", userId, createDTO);
        
        PaymentRecordVO record = paymentRecordsService.createPaymentOrder(userId, createDTO);
        return Result.success(record);
    }

    /**
     * 确认支付
     */
    @PostMapping("/orders/{recordId}/confirm")
    @Operation(summary = "确认支付", description = "确认第三方支付结果")
    public Result<Boolean> confirmPayment(
            @Parameter(description = "缴费记录ID") @PathVariable Long recordId,
            @Parameter(description = "第三方交易号") @RequestParam String transactionId) {
        log.info("确认支付 - recordId: {}, transactionId: {}", recordId, transactionId);
        
        boolean success = paymentRecordsService.confirmPayment(recordId, transactionId);
        return Result.success(success);
    }

    /**
     * 分页查询缴费记录
     */
    @GetMapping("/records")
    @Operation(summary = "分页查询缴费记录", description = "根据条件分页查询缴费历史记录")
    public Result<IPage<PaymentRecordVO>> getPaymentRecordPage(PaymentBillQueryDTO query) {
        Long userId = UserContext.getUserId();
        log.info("分页查询缴费记录 - userId: {}, query: {}", userId, query);
        
        IPage<PaymentRecordVO> page = paymentRecordsService.getPaymentRecordPage(userId, query);
        return Result.success(page);
    }

    /**
     * 获取缴费统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取缴费统计信息", description = "获取用户的缴费统计数据")
    public Result<PaymentStatisticsVO> getPaymentStatistics(
            @Parameter(description = "年份") @RequestParam(required = false) Integer year) {
        Long userId = UserContext.getUserId();
        log.info("获取缴费统计信息 - userId: {}, year: {}", userId, year);
        
        PaymentStatisticsVO statistics = paymentRecordsService.getPaymentStatistics(userId, year);
        return Result.success(statistics);
    }

    /**
     * 根据账单ID查询缴费记录
     */
    @GetMapping("/records/bill/{billId}")
    @Operation(summary = "根据账单ID查询缴费记录", description = "根据账单ID查询对应的缴费记录")
    public Result<PaymentRecordVO> getPaymentRecordByBillId(
            @Parameter(description = "账单ID") @PathVariable Long billId) {
        Long userId = UserContext.getUserId();
        log.info("根据账单ID查询缴费记录 - userId: {}, billId: {}", userId, billId);

        PaymentRecordVO record = paymentRecordsService.getByBillId(billId, userId);
        return Result.success(record);
    }

    /**
     * 获取支付方式列表
     */
    @GetMapping("/methods")
    @Operation(summary = "获取支付方式列表", description = "获取可用的支付方式列表")
    public Result<List<PaymentMethodVO>> getPaymentMethods() {
        Long userId = UserContext.getUserId();
        log.info("获取支付方式列表 - userId: {}", userId);

        List<PaymentMethodVO> methods = paymentRecordsService.getPaymentMethods(userId);
        return Result.success(methods);
    }
}
