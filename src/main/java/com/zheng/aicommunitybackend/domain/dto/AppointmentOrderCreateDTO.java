package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 创建预约订单DTO（支持多服务）
 */
@Data
@Schema(description = "创建预约订单DTO")
public class AppointmentOrderCreateDTO {

    /**
     * 服务项列表
     */
    @NotEmpty(message = "服务项不能为空")
    @Schema(description = "服务项列表")
    private List<ServiceItem> serviceItems;

    /**
     * 预约服务时间
     */
    @NotNull(message = "预约时间不能为空")
    @Schema(description = "预约服务时间")
    private Date appointmentTime;

    /**
     * 服务地址
     */
    @NotBlank(message = "服务地址不能为空")
    @Schema(description = "服务地址", example = "北京市朝阳区东城区复地连城2栋3单元")
    private String address;

    /**
     * 联系人姓名
     */
    @NotBlank(message = "联系人姓名不能为空")
    @Schema(description = "联系人姓名", example = "张先生")
    private String contactName;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Schema(description = "联系电话", example = "15824456449")
    private String contactPhone;

    /**
     * 服务需求说明
     */
    @Schema(description = "服务需求说明")
    private String requirements;

    /**
     * 特殊要求（可选）
     */
    @Schema(description = "特殊要求列表", example = "[\"tools\", \"urgent\"]")
    private List<String> specialRequests;

    /**
     * 服务项
     */
    @Data
    @Schema(description = "服务项")
    public static class ServiceItem {
        /**
         * 服务ID
         */
        @NotNull(message = "服务ID不能为空")
        @Schema(description = "服务ID")
        private Long serviceId;

        /**
         * 服务数量
         */
        @NotNull(message = "服务数量不能为空")
        @Schema(description = "服务数量")
        private Integer quantity;
    }
}

