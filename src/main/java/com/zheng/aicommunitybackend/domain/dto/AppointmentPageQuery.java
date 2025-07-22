package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预约订单分页查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "预约订单分页查询DTO")
public class AppointmentPageQuery extends PageQuery {

    /**
     * 服务类型
     */
    @Schema(description = "服务类型", example = "cleaning")
    private String serviceType;

    /**
     * 订单状态：0-待确认 1-已确认 2-服务中 3-已完成 4-已取消
     */
    @Schema(description = "订单状态：0-待确认 1-已确认 2-服务中 3-已完成 4-已取消")
    private Integer status;

    /**
     * 关键词搜索（订单编号、服务名称、联系人）
     */
    @Schema(description = "关键词搜索")
    private String keyword;
}
