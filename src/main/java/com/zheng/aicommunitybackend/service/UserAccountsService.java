package com.zheng.aicommunitybackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.entity.UserAccounts;
import com.zheng.aicommunitybackend.domain.dto.AccountRechargeDTO;
import com.zheng.aicommunitybackend.domain.vo.UserAccountVO;

import java.math.BigDecimal;

/**
 * 用户账户服务接口
 */
public interface UserAccountsService extends IService<UserAccounts> {

    /**
     * 查询用户账户信息
     */
    UserAccountVO getAccountByUserId(Long userId);

    /**
     * 创建用户账户
     */
    boolean createAccount(Long userId);

    /**
     * 账户充值
     */
    boolean recharge(Long userId, AccountRechargeDTO rechargeDTO);

    /**
     * 账户消费
     */
    boolean consume(Long userId, BigDecimal amount, String description);

    /**
     * 冻结金额
     */
    boolean freezeAmount(Long userId, BigDecimal amount);

    /**
     * 解冻金额
     */
    boolean unfreezeAmount(Long userId, BigDecimal amount);

    /**
     * 检查余额是否充足
     */
    boolean checkBalance(Long userId, BigDecimal amount);
}
