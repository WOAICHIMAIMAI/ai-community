package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付方式视图对象
 */
@Data
@Schema(description = "支付方式信息")
public class PaymentMethodVO {

    @Schema(description = "支付方式ID")
    private Integer id;

    @Schema(description = "支付方式名称")
    private String name;

    @Schema(description = "支付方式描述")
    private String description;

    @Schema(description = "支付方式图标")
    private String icon;

    @Schema(description = "支付方式类型：1-微信支付，2-支付宝，3-银行卡，4-账户余额")
    private Integer type;

    @Schema(description = "是否可用")
    private Boolean enabled;

    @Schema(description = "可用余额（仅账户余额类型）")
    private BigDecimal balance;

    @Schema(description = "银行卡号后四位（仅银行卡类型）")
    private String cardNumber;

    @Schema(description = "银行名称（仅银行卡类型）")
    private String bankName;

    @Schema(description = "是否为默认支付方式")
    private Boolean isDefault;

    @Schema(description = "排序")
    private Integer sort;
}
