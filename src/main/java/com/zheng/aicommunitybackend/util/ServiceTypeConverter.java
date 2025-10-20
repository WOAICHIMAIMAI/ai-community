package com.zheng.aicommunitybackend.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务类型转换工具类
 * 用于将前端传入的中文服务类型转换为数据库中的英文服务类型
 */
public class ServiceTypeConverter {
    
    /**
     * 中文到英文的映射
     */
    private static final Map<String, String> CN_TO_EN_MAP = new HashMap<>();
    
    /**
     * 英文到中文的映射
     */
    private static final Map<String, String> EN_TO_CN_MAP = new HashMap<>();
    
    static {
        // 清洁服务
        CN_TO_EN_MAP.put("清洁", "cleaning");
        CN_TO_EN_MAP.put("保洁", "cleaning");
        
        // 维修服务
        CN_TO_EN_MAP.put("水电维修", "repair");
        CN_TO_EN_MAP.put("门窗维修", "repair");
        CN_TO_EN_MAP.put("维修", "repair");
        
        // 家电维修
        CN_TO_EN_MAP.put("家电维修", "appliance");
        CN_TO_EN_MAP.put("家电", "appliance");
        
        // 搬家服务
        CN_TO_EN_MAP.put("搬家", "moving");
        CN_TO_EN_MAP.put("搬运", "moving");
        
        // 园艺服务
        CN_TO_EN_MAP.put("园艺", "gardening");
        CN_TO_EN_MAP.put("绿化", "gardening");
        
        // 除虫服务
        CN_TO_EN_MAP.put("除虫", "pest");
        CN_TO_EN_MAP.put("杀虫", "pest");
        
        // 教育辅导
        CN_TO_EN_MAP.put("辅导", "tutoring");
        CN_TO_EN_MAP.put("家教", "tutoring");
        
        // 老年护理
        CN_TO_EN_MAP.put("老年护理", "eldercare");
        CN_TO_EN_MAP.put("养老", "eldercare");
        
        // 儿童看护
        CN_TO_EN_MAP.put("儿童看护", "childcare");
        CN_TO_EN_MAP.put("育儿", "childcare");
        
        // 烹饪服务
        CN_TO_EN_MAP.put("烹饪", "cooking");
        CN_TO_EN_MAP.put("厨师", "cooking");
        
        // 反向映射
        EN_TO_CN_MAP.put("cleaning", "清洁");
        EN_TO_CN_MAP.put("repair", "维修");
        EN_TO_CN_MAP.put("appliance", "家电维修");
        EN_TO_CN_MAP.put("moving", "搬家");
        EN_TO_CN_MAP.put("gardening", "园艺");
        EN_TO_CN_MAP.put("pest", "除虫");
        EN_TO_CN_MAP.put("tutoring", "辅导");
        EN_TO_CN_MAP.put("eldercare", "老年护理");
        EN_TO_CN_MAP.put("childcare", "儿童看护");
        EN_TO_CN_MAP.put("cooking", "烹饪");
    }
    
    /**
     * 将中文服务类型转换为英文
     * 
     * @param chineseType 中文服务类型
     * @return 英文服务类型，如果没有找到映射则返回原值
     */
    public static String convertToEnglish(String chineseType) {
        if (chineseType == null || chineseType.trim().isEmpty()) {
            return chineseType;
        }
        
        // 去除空格
        String trimmed = chineseType.trim();
        
        // 如果是"全部"或为空，返回null表示不需要筛选
        if ("全部".equals(trimmed) || "all".equalsIgnoreCase(trimmed)) {
            return null;
        }
        
        // 查找映射
        String englishType = CN_TO_EN_MAP.get(trimmed);
        
        // 如果找到了映射，返回英文类型；否则返回原值（可能已经是英文）
        return englishType != null ? englishType : chineseType;
    }
    
    /**
     * 将英文服务类型转换为中文
     * 
     * @param englishType 英文服务类型
     * @return 中文服务类型，如果没有找到映射则返回原值
     */
    public static String convertToChinese(String englishType) {
        if (englishType == null || englishType.trim().isEmpty()) {
            return englishType;
        }
        
        String trimmed = englishType.trim();
        String chineseType = EN_TO_CN_MAP.get(trimmed);
        
        return chineseType != null ? chineseType : englishType;
    }
    
    /**
     * 判断是否为中文服务类型
     * 
     * @param type 服务类型
     * @return true表示是中文，false表示不是
     */
    public static boolean isChinese(String type) {
        if (type == null || type.trim().isEmpty()) {
            return false;
        }
        
        return CN_TO_EN_MAP.containsKey(type.trim());
    }
    
    /**
     * 判断是否为英文服务类型
     * 
     * @param type 服务类型
     * @return true表示是英文，false表示不是
     */
    public static boolean isEnglish(String type) {
        if (type == null || type.trim().isEmpty()) {
            return false;
        }
        
        return EN_TO_CN_MAP.containsKey(type.trim());
    }
}

