package com.zheng.aicommunitybackend.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 缴费统计信息VO
 */
@Data
public class PaymentStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总缴费金额
     */
    private BigDecimal totalAmount;

    /**
     * 缴费次数
     */
    private Integer totalCount;

    /**
     * 平均缴费金额
     */
    private BigDecimal avgAmount;

    /**
     * 本月缴费金额
     */
    private BigDecimal monthAmount;

    /**
     * 本年缴费金额
     */
    private BigDecimal yearAmount;

    /**
     * 待缴费金额
     */
    private BigDecimal pendingAmount;

    /**
     * 待缴费数量
     */
    private Integer pendingCount;
}
