package com.zheng.aicommunitybackend.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建缴费订单DTO
 */
@Data
public class PaymentCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账单ID列表
     */
    @NotEmpty(message = "账单ID不能为空")
    private List<Long> billIds;

    /**
     * 支付方式：1-微信支付 2-支付宝支付 3-银行卡支付 4-余额支付
     */
    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    private BigDecimal paymentAmount;

    /**
     * 备注信息
     */
    private String remark;
}
