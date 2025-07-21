package com.zheng.aicommunitybackend.domain.enums;

import lombok.Getter;

/**
 * 问题分类枚举
 */
@Getter
public enum ProblemCategoryEnum {
    
    ACCOUNT_RELATED(1, "账户相关"),
    REPAIR_SERVICE(2, "维修服务"),
    COMMUNITY_USAGE(3, "社区使用"),
    PAYMENT_BILLING(4, "支付账单"),
    TECHNICAL_SUPPORT(5, "技术支持"),
    OTHER(6, "其他问题");
    
    private final Integer code;
    private final String description;
    
    ProblemCategoryEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据code获取描述
     */
    public static String getDescriptionByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProblemCategoryEnum category : values()) {
            if (category.getCode().equals(code)) {
                return category.getDescription();
            }
        }
        return null;
    }
    
    /**
     * 根据code获取枚举
     */
    public static ProblemCategoryEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProblemCategoryEnum category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        return null;
    }
}
