package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zheng.aicommunitybackend.domain.entity.HotNews;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author ZhengJJ
* @description 针对表【hot_news(热点新闻表)】的数据库操作Mapper
* @createDate 2025-07-13 12:19:41
* @Entity generator.domain.HotNews
*/
public interface HotNewsMapper extends BaseMapper<HotNews> {

    /**
     * 查询最近的内容指纹
     * 限制查询数量，提高性能
     * @param limit 限制数量
     * @return 内容指纹列表
     */
    @Select("SELECT content_hash FROM hot_news WHERE content_hash IS NOT NULL ORDER BY create_time DESC LIMIT #{limit}")
    List<String> selectRecentContentHashes(@Param("limit") int limit);
}




