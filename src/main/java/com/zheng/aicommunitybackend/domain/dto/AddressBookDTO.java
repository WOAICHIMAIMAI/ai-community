package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 地址簿数据传输对象
 */
@Data
@Schema(description = "地址簿数据传输对象")
public class AddressBookDTO {

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
    @Schema(description = "收货人姓名", required = true)
    private String name;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "联系电话", required = true)
    private String phone;

    /**
     * 省份
     */
    @NotBlank(message = "省份不能为空")
    @Schema(description = "省份", required = true)
    private String province;

    /**
     * 城市
     */
    @NotBlank(message = "城市不能为空")
    @Schema(description = "城市", required = true)
    private String city;

    /**
     * 区县
     */
    @NotBlank(message = "区县不能为空")
    @Schema(description = "区县", required = true)
    private String district;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    @Schema(description = "详细地址", required = true)
    private String detail;

    /**
     * 邮政编码
     */
    @Schema(description = "邮政编码")
    private String postalCode;

    /**
     * 是否默认地址(0-否 1-是)
     */
    @Schema(description = "是否默认地址(0-否 1-是)")
    private Integer isDefault;

    /**
     * 地址标签(家/公司/学校等)
     */
    @Schema(description = "地址标签(家/公司/学校等)")
    private String label;
} 