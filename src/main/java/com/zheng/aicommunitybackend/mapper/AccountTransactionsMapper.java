package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zheng.aicommunitybackend.domain.entity.AccountTransactions;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账户流水Mapper
 */
@Mapper
public interface AccountTransactionsMapper extends BaseMapper<AccountTransactions> {

}
