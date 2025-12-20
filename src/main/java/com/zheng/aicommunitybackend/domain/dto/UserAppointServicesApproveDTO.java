package com.zheng.aicommunitybackend.domain.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserAppointServicesApproveDTO {
    /**
     * 服务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 服务类型标识(appliance, childcare, cleaning, cooking, eldercare, gardening, moving, pest, repair, tutoring)
     */
    @NotBlank(message = "服务类型不可以为空")
    private String serviceType;

    /**
     * 服务名称
     */
    @NotBlank(message = "服务名称不可以为空")
    private String serviceName;

    /**
     * 服务描述
     */
    private String description;

    /**
     * 服务图标
     */
    @NotNull(message = "服务图标不可以为空！")
    private String icon;

    /**
     * 基础价格
     */
    @NotNull(message = "请输入价格！")
    private BigDecimal basePrice;

    /**
     * 计价单位
     */
    @NotNull(message = "请输入价格！")
    private String unit;
}
