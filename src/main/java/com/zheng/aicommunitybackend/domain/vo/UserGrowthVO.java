package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户增长趋势VO
 */
@Data
@Schema(description = "用户增长趋势数据")
public class UserGrowthVO {
    
    /**
     * 日期列表
     */
    @Schema(description = "日期列表")
    private List<String> dates;
    
    /**
     * 用户数量列表
     */
    @Schema(description = "用户数量列表")
    private List<Long> counts;
} 