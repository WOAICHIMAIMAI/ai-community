package com.zheng.aicommunitybackend.domain.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 帖子展示对象
 */
@Data
public class PostVO {
    /**
     * 帖子ID
     */
    private Long id;
    
    /**
     * 发帖用户ID
     */
    private Long userId;
    
    /**
     * 发帖用户昵称
     */
    private String nickname;

    /**
     * 发帖用户用户名
     */
    private String username;
    
    /**
     * 发帖用户头像
     */
    private String avatar;
    
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
     * 浏览数
     */
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 是否置顶
     */
    private Integer isTop;
    
    /**
     * 是否精华
     */
    private Integer isEssence;
    
    /**
     * 状态：0-草稿 1-已发布 2-已删除
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createdTime;
    
    /**
     * 更新时间
     */
    private Date updatedTime;
    
    /**
     * 当前登录用户是否已点赞
     */
    private Boolean hasLiked;
    
    /**
     * 当前登录用户是否已收藏
     */
    private Boolean hasFavorited;
} 