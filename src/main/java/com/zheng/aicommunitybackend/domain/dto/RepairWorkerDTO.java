package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 维修工DTO
 */
@Data
@Schema(description = "维修工DTO")
public class RepairWorkerDTO {

    /**
     * 维修工ID，新增时不需要传
     */
    @Schema(description = "维修工ID，新增时不需要传")
    private Long id;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 20, message = "姓名长度不能超过20个字符")
    @Schema(description = "姓名", required = true)
    private String name;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "联系电话", required = true)
    private String phone;

    /**
     * 服务类型，多个用逗号分隔
     */
    @NotBlank(message = "服务类型不能为空")
    @Schema(description = "服务类型，多个用逗号分隔", required = true)
    private String serviceType;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)", message = "身份证号格式不正确")
    @Schema(description = "身份证号", required = true)
    private String idCardNumber;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatarUrl;

    /**
     * 工作状态：0-休息 1-可接单 2-忙碌
     */
    @Schema(description = "工作状态：0-休息 1-可接单 2-忙碌")
    private Integer workStatus;

    /**
     * 个人介绍
     */
    @Schema(description = "个人介绍")
    @Size(max = 500, message = "个人介绍不能超过500个字符")
    private String introduction;
} 