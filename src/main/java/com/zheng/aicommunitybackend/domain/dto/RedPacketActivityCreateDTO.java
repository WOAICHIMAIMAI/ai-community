package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 创建红包活动DTO
 */
@Data
@Schema(description = "创建红包活动DTO")
public class RedPacketActivityCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 活动名称
     */
    @NotBlank(message = "活动名称不能为空")
    @Size(max = 100, message = "活动名称不能超过100个字符")
    @Schema(description = "活动名称", required = true)
    private String activityName;

    /**
     * 活动描述
     */
    @Size(max = 500, message = "活动描述不能超过500个字符")
    @Schema(description = "活动描述")
    private String activityDesc;

    /**
     * 红包总金额（元）
     */
    @NotNull(message = "红包总金额不能为空")
    @DecimalMin(value = "0.01", message = "红包总金额必须大于0")
    @DecimalMax(value = "100000.00", message = "红包总金额不能超过10万元")
    @Schema(description = "红包总金额（元）", required = true)
    private Double totalAmount;

    /**
     * 红包总数量
     */
    @NotNull(message = "红包总数量不能为空")
    @Min(value = 1, message = "红包数量至少为1个")
    @Max(value = 10000, message = "红包数量不能超过10000个")
    @Schema(description = "红包总数量", required = true)
    private Integer totalCount;

    /**
     * 活动开始时间
     */
    @NotNull(message = "活动开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "活动开始时间，格式：yyyy-MM-dd HH:mm:ss", required = true, example = "2025-07-24 10:00:00")
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    @NotNull(message = "活动结束时间不能为空")
    @Future(message = "活动结束时间必须是未来时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "活动结束时间，格式：yyyy-MM-dd HH:mm:ss", required = true, example = "2025-07-24 18:00:00")
    private LocalDateTime endTime;

    /**
     * 红包分配算法
     */
    @Schema(description = "红包分配算法：DOUBLE_AVERAGE-二倍均值法, RANDOM-随机分配")
    private String algorithm = "DOUBLE_AVERAGE";

    /**
     * 最小红包金额（分）
     */
    @Min(value = 1, message = "最小红包金额不能小于1分")
    @Schema(description = "最小红包金额（分）")
    private Long minAmount = 1L;

    /**
     * 最大红包金额（分）
     */
    @Schema(description = "最大红包金额（分）")
    private Long maxAmount;

    /**
     * 是否立即开始
     */
    @Schema(description = "是否立即开始")
    private Boolean startImmediately = false;

    // 自定义校验方法
    @AssertTrue(message = "活动结束时间必须晚于开始时间")
    public boolean isEndTimeAfterStartTime() {
        if (startTime == null || endTime == null) {
            return true; // 让其他校验处理null值
        }
        // 如果立即开始，跳过时间校验
        if (startImmediately != null && startImmediately) {
            return true;
        }
        return endTime.isAfter(startTime);
    }

    @AssertTrue(message = "活动持续时间不能超过24小时")
    public boolean isValidDuration() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return endTime.isBefore(startTime.plusDays(1));
    }

    @AssertTrue(message = "平均红包金额不能小于1分")
    public boolean isValidAverageAmount() {
        if (totalAmount == null || totalCount == null) {
            return true;
        }
        long totalAmountCents = Math.round(totalAmount * 100);
        return totalAmountCents >= totalCount; // 平均至少1分
    }

    @AssertTrue(message = "最大红包金额不能小于最小红包金额")
    public boolean isMaxAmountValid() {
        if (minAmount == null || maxAmount == null) {
            return true;
        }
        return maxAmount >= minAmount;
    }

    // 获取总金额（分）
    public Long getTotalAmountCents() {
        return totalAmount != null ? Math.round(totalAmount * 100) : null;
    }
}
