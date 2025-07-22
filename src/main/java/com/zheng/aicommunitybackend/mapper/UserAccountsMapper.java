package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zheng.aicommunitybackend.domain.entity.UserAccounts;
import com.zheng.aicommunitybackend.domain.vo.UserAccountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 用户账户Mapper
 */
@Mapper
public interface UserAccountsMapper extends BaseMapper<UserAccounts> {

    /**
     * 查询用户账户信息
     */
    UserAccountVO selectAccountByUserId(@Param("userId") Long userId);

    /**
     * 更新账户余额
     */
    int updateBalance(@Param("userId") Long userId, 
                     @Param("amount") BigDecimal amount,
                     @Param("type") String type);

    /**
     * 冻结/解冻金额
     */
    int updateFrozenAmount(@Param("userId") Long userId,
                          @Param("amount") BigDecimal amount,
                          @Param("operation") String operation);
}
