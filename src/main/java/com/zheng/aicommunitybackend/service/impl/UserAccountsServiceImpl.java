package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.entity.UserAccounts;
import com.zheng.aicommunitybackend.domain.dto.AccountRechargeDTO;
import com.zheng.aicommunitybackend.domain.vo.UserAccountVO;
import com.zheng.aicommunitybackend.mapper.UserAccountsMapper;
import com.zheng.aicommunitybackend.service.UserAccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户账户服务实现类
 */
@Slf4j
@Service
public class UserAccountsServiceImpl extends ServiceImpl<UserAccountsMapper, UserAccounts> 
        implements UserAccountsService {

    @Override
    public UserAccountVO getAccountByUserId(Long userId) {
        UserAccountVO account = baseMapper.selectAccountByUserId(userId);
        if (account == null) {
            // 如果账户不存在，创建默认账户
            createAccount(userId);
            account = baseMapper.selectAccountByUserId(userId);
        }
        
        if (account != null) {
            // 计算可用余额
            BigDecimal availableBalance = account.getBalance().subtract(account.getFrozenAmount());
            account.setAvailableBalance(availableBalance);
            
            // 设置状态名称
            account.setStatusName(account.getStatus() == 1 ? "正常" : "冻结");
        }
        
        return account;
    }

    @Override
    @Transactional
    public boolean createAccount(Long userId) {
        // 检查账户是否已存在
        LambdaQueryWrapper<UserAccounts> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccounts::getUserId, userId);
        UserAccounts existAccount = getOne(wrapper);
        
        if (existAccount != null) {
            return true; // 账户已存在
        }
        
        UserAccounts account = new UserAccounts();
        account.setUserId(userId);
        account.setBalance(BigDecimal.ZERO);
        account.setFrozenAmount(BigDecimal.ZERO);
        account.setTotalRecharge(BigDecimal.ZERO);
        account.setTotalConsumption(BigDecimal.ZERO);
        account.setStatus(1); // 正常状态
        account.setCreatedTime(LocalDateTime.now());
        account.setUpdatedTime(LocalDateTime.now());
        
        return save(account);
    }

    @Override
    @Transactional
    public boolean recharge(Long userId, AccountRechargeDTO rechargeDTO) {
        // 这里应该调用第三方支付接口
        // 暂时模拟支付成功
        
        return baseMapper.updateBalance(userId, rechargeDTO.getAmount(), "recharge") > 0;
    }

    @Override
    @Transactional
    public boolean consume(Long userId, BigDecimal amount, String description) {
        // 检查余额是否充足
        if (!checkBalance(userId, amount)) {
            return false;
        }
        
        return baseMapper.updateBalance(userId, amount.negate(), "consume") > 0;
    }

    @Override
    @Transactional
    public boolean freezeAmount(Long userId, BigDecimal amount) {
        return baseMapper.updateFrozenAmount(userId, amount, "freeze") > 0;
    }

    @Override
    @Transactional
    public boolean unfreezeAmount(Long userId, BigDecimal amount) {
        return baseMapper.updateFrozenAmount(userId, amount, "unfreeze") > 0;
    }

    @Override
    public boolean checkBalance(Long userId, BigDecimal amount) {
        UserAccountVO account = getAccountByUserId(userId);
        if (account == null) {
            return false;
        }
        
        return account.getAvailableBalance().compareTo(amount) >= 0;
    }
}
