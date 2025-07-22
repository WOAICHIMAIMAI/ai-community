package com.zheng.aicommunitybackend.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 账单查询DTO
 */
@Data
public class PaymentBillQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账单类型：1-物业费 2-水费 3-电费 4-燃气费 5-停车费 6-其他
     */
    private Integer billType;

    /**
     * 账单状态：0-未缴费 1-已缴费 2-已逾期 3-部分缴费
     */
    private Integer status;

    /**
     * 计费周期（如：2024-01）
     */
    private String billingPeriod;

    /**
     * 年份筛选
     */
    private Integer year;

    /**
     * 月份筛选
     */
    private Integer month;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
