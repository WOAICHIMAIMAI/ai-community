package com.zheng.aicommunitybackend.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户流水VO
 */
@Data
@Schema(description = "账户流水信息")
public class AccountTransactionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "流水ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "账户ID")
    private Long accountId;

    @Schema(description = "交易流水号")
    private String transactionNo;

    @Schema(description = "交易类型：1-充值 2-消费 3-退款 4-冻结 5-解冻")
    private Integer transactionType;

    @Schema(description = "交易类型名称")
    private String transactionTypeName;

    @Schema(description = "交易金额")
    private BigDecimal amount;

    @Schema(description = "交易前余额")
    private BigDecimal balanceBefore;

    @Schema(description = "交易后余额")
    private BigDecimal balanceAfter;

    @Schema(description = "关联业务ID")
    private Long relatedId;

    @Schema(description = "关联业务类型")
    private String relatedType;

    @Schema(description = "交易描述")
    private String description;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}

