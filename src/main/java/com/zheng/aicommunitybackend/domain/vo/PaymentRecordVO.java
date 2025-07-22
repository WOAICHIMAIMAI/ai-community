package com.zheng.aicommunitybackend.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 缴费记录VO
 */
@Data
public class PaymentRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 缴费记录ID
     */
    private Long id;

    /**
     * 账单ID
     */
    private Long billId;

    /**
     * 账单标题
     */
    private String billTitle;

    /**
     * 账单类型
     */
    private Integer billType;

    /**
     * 账单类型名称
     */
    private String billTypeName;

    /**
     * 缴费流水号
     */
    private String paymentNo;

    /**
     * 实际缴费金额
     */
    private BigDecimal paymentAmount;

    /**
     * 支付方式：1-微信支付 2-支付宝支付 3-银行卡支付 4-余额支付
     */
    private Integer paymentMethod;

    /**
     * 支付方式名称
     */
    private String paymentMethodName;

    /**
     * 第三方交易号
     */
    private String transactionId;

    /**
     * 支付状态：0-待支付 1-支付成功 2-支付失败 3-已退款
     */
    private Integer paymentStatus;

    /**
     * 支付状态名称
     */
    private String paymentStatusName;

    /**
     * 支付完成时间
     */
    private LocalDateTime paymentTime;

    /**
     * 计费周期
     */
    private String billingPeriod;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
