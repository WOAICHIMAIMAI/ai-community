package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 创建预约DTO
 */
@Data
@Schema(description = "创建预约DTO")
public class AppointmentCreateDTO {

    /**
     * 服务类型
     */
    @NotBlank(message = "服务类型不能为空")
    @Schema(description = "服务类型", example = "cleaning")
    private String serviceType;

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
    @Schema(description = "服务地址", example = "阳光小区1号楼1单元101室")
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
    @Schema(description = "联系电话", example = "138****8888")
    private String contactPhone;

    /**
     * 特殊要求
     */
    @Schema(description = "特殊要求", example = "需要深度清洁厨房和卫生间")
    private String requirements;

    /**
     * 指定服务人员ID（可选）
     */
    @Schema(description = "指定服务人员ID")
    private Long workerId;
}
