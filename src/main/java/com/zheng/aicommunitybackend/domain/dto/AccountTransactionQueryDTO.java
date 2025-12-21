package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 账户流水查询DTO
 */
@Data
@Schema(description = "账户流水查询条件")
public class AccountTransactionQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "交易类型：1-充值 2-消费 3-退款 4-冻结 5-解冻")
    private Integer transactionType;

    @Schema(description = "关联业务类型")
    private String relatedType;

    @Schema(description = "开始日期 yyyy-MM-dd")
    private String startDate;

    @Schema(description = "结束日期 yyyy-MM-dd")
    private String endDate;

    @Schema(description = "当前页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}

