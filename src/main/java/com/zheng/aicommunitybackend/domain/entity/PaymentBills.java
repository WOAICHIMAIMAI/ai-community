package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 费用账单实体类
 */
@Data
@TableName("payment_bills")
public class PaymentBills implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账单类型：1-物业费 2-水费 3-电费 4-燃气费 5-停车费 6-其他
     */
    private Integer billType;

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
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
