package com.zheng.aicommunitybackend.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户账户信息VO
 */
@Data
public class UserAccountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账户ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;

    /**
     * 累计充值金额
     */
    private BigDecimal totalRecharge;

    /**
     * 累计消费金额
     */
    private BigDecimal totalConsumption;

    /**
     * 账户状态：0-冻结 1-正常
     */
    private Integer status;

    /**
     * 账户状态名称
     */
    private String statusName;
}
