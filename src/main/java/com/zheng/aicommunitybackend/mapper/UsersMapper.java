package com.zheng.aicommunitybackend.mapper;

import com.zheng.aicommunitybackend.domain.entity.Users;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

/**
* @author ZhengJJ
* @description 针对表【users(用户基础信息表)】的数据库操作Mapper
* @createDate 2025-07-10 11:03:49
* @Entity com.zheng.aicommunitybackend.domain.entity.Users
*/
public interface UsersMapper extends BaseMapper<Users> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    Users selectByUsername(@Param("username") String username);

    /**
     * 根据用户名模式删除用户（用于清理测试数据）
     */
    @Delete("DELETE FROM users WHERE username LIKE #{usernamePattern}")
    int deleteByUsername(@Param("usernamePattern") String usernamePattern);
}




