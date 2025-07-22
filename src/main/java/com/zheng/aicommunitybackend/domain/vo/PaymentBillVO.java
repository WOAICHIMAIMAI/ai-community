package com.zheng.aicommunitybackend.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单信息VO
 */
@Data
public class PaymentBillVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账单ID
     */
    private Long id;

    /**
     * 账单类型：1-物业费 2-水费 3-电费 4-燃气费 5-停车费 6-其他
     */
    private Integer billType;

    /**
     * 账单类型名称
     */
    private String billTypeName;

    /**
     * 账单标题
     */
    private String billTitle;

    /**
     * 账单描述
     */
    private String billDescription;

    /**
     * 应缴金额
     */
    private BigDecimal amount;

    /**
     * 计费周期（如：2024-01）
     */
    private String billingPeriod;

    /**
     * 缴费截止日期
     */
    private LocalDateTime dueDate;

    /**
     * 账单状态：0-未缴费 1-已缴费 2-已逾期 3-部分缴费
     */
    private Integer status;

    /**
     * 账单状态名称
     */
    private String statusName;

    /**
     * 是否逾期
     */
    private Boolean isOverdue;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
