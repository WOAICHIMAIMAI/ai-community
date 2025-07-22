package com.zheng.aicommunitybackend.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 账户充值DTO
 */
@Data
public class AccountRechargeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 充值金额
     */
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    private BigDecimal amount;

    /**
     * 支付方式：1-微信支付 2-支付宝支付 3-银行卡支付
     */
    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;

    /**
     * 备注信息
     */
    private String remark;
}
