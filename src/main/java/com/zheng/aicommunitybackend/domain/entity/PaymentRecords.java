package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 缴费记录实体类
 */
@Data
@TableName("payment_records")
public class PaymentRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 缴费记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账单ID
     */
    private Long billId;

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
     * 支付渠道标识
     */
    private String paymentChannel;

    /**
     * 第三方交易号
     */
    private String transactionId;

    /**
     * 支付状态：0-待支付 1-支付成功 2-支付失败 3-已退款
     */
    private Integer paymentStatus;

    /**
     * 支付完成时间
     */
    private LocalDateTime paymentTime;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
