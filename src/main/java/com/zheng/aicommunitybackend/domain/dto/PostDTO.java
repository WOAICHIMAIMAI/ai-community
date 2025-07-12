package com.zheng.aicommunitybackend.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 帖子数据传输对象
 */
@Data
public class PostDTO {
    /**
     * 帖子ID（更新时需要）
     */
    private Long id;
    
    /**
     * 帖子标题
     */
    private String title;
    
    /**
     * 帖子内容
     */
    private String content;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 图片URL列表
     */
    private List<String> images;
    
    /**
     * 状态：0-草稿 1-已发布
     */
    private Integer status;
} 