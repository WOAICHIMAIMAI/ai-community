package com.zheng.aicommunitybackend.constant;

/**
 * 缓存常量类
 */
public class CacheConstants {

    /**
     * 帖子相关缓存前缀
     */
    public static final String POST_PREFIX = "post:";
    
    /**
     * 帖子列表缓存前缀
     */
    public static final String POST_LIST_PREFIX = "post:list:";
    
    /**
     * 帖子详情缓存前缀
     */
    public static final String POST_DETAIL_PREFIX = "post:detail:";
    
    /**
     * 帖子分页缓存前缀
     */
    public static final String POST_PAGE_PREFIX = "post:page:";

    /**
     * 新闻相关缓存前缀
     */
    public static final String NEWS_PREFIX = "news:";
    
    /**
     * 新闻列表缓存前缀
     */
    public static final String NEWS_LIST_PREFIX = "news:list:";
    
    /**
     * 新闻详情缓存前缀
     */
    public static final String NEWS_DETAIL_PREFIX = "news:detail:";
    
    /**
     * 新闻分页缓存前缀
     */
    public static final String NEWS_PAGE_PREFIX = "news:page:";
    
    /**
     * 热点新闻缓存前缀
     */
    public static final String HOT_NEWS_PREFIX = "news:hot:";

    /**
     * 默认缓存过期时间（秒）- 30分钟
     */
    public static final long DEFAULT_EXPIRE_TIME = 30 * 60;
    
    /**
     * 热点数据缓存过期时间（秒）- 10分钟
     */
    public static final long HOT_DATA_EXPIRE_TIME = 10 * 60;
    
    /**
     * 详情数据缓存过期时间（秒）- 1小时
     */
    public static final long DETAIL_EXPIRE_TIME = 60 * 60;

    /**
     * 构建帖子详情缓存key
     */
    public static String buildPostDetailKey(Long postId) {
        return POST_DETAIL_PREFIX + postId;
    }

    /**
     * 构建帖子分页缓存key
     */
    public static String buildPostPageKey(int page, int size, String category) {
        return POST_PAGE_PREFIX + page + ":" + size + ":" + (category != null ? category : "all");
    }

    /**
     * 构建新闻详情缓存key
     */
    public static String buildNewsDetailKey(Long newsId) {
        return NEWS_DETAIL_PREFIX + newsId;
    }

    /**
     * 构建新闻分页缓存key
     */
    public static String buildNewsPageKey(int page, int size, String category) {
        return NEWS_PAGE_PREFIX + page + ":" + size + ":" + (category != null ? category : "all");
    }

    /**
     * 构建热点新闻缓存key
     */
    public static String buildHotNewsKey(int limit) {
        return HOT_NEWS_PREFIX + limit;
    }
}
