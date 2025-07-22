package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 预约推荐服务VO
 */
@Data
@Schema(description = "预约推荐服务VO")
public class AppointmentRecommendVO {

    /**
     * 服务类型标识
     */
    @Schema(description = "服务类型标识", example = "cleaning")
    private String type;

    /**
     * 服务名称
     */
    @Schema(description = "服务名称", example = "家政保洁")
    private String name;

    /**
     * 服务图标
     */
    @Schema(description = "服务图标", example = "brush-o")
    private String icon;

    /**
     * 推荐理由
     */
    @Schema(description = "推荐理由", example = "您上次预约的服务，体验很棒")
    private String reason;
}
