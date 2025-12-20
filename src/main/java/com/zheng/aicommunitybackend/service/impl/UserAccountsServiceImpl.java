package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.entity.AccountTransactions;
import com.zheng.aicommunitybackend.domain.entity.UserAccounts;
import com.zheng.aicommunitybackend.domain.dto.AccountRechargeDTO;
import com.zheng.aicommunitybackend.domain.vo.UserAccountVO;
import com.zheng.aicommunitybackend.mapper.AccountTransactionsMapper;
import com.zheng.aicommunitybackend.mapper.UserAccountsMapper;
import com.zheng.aicommunitybackend.service.UserAccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 用户账户服务实现类
 */
@Slf4j
@Service
public class UserAccountsServiceImpl extends ServiceImpl<UserAccountsMapper, UserAccounts> 
        implements UserAccountsService {
    
    @Autowired
    private AccountTransactionsMapper accountTransactionsMapper;

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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payForAppointment(Long userId, Long orderId, String orderNo, BigDecimal amount) {
        log.info("开始处理预约订单支付，用户ID：{}，订单ID：{}，订单号：{}，金额：{}", userId, orderId, orderNo, amount);
        
        // 1. 查询用户账户
        LambdaQueryWrapper<UserAccounts> accountWrapper = new LambdaQueryWrapper<>();
        accountWrapper.eq(UserAccounts::getUserId, userId);
        UserAccounts account = getOne(accountWrapper);
        
        if (account == null) {
            log.error("用户账户不存在，用户ID：{}", userId);
            throw new RuntimeException("用户账户不存在");
        }
        
        // 2. 检查账户状态
        if (account.getStatus() != 1) {
            log.error("用户账户已被冻结，用户ID：{}", userId);
            throw new RuntimeException("账户已被冻结，无法支付");
        }
        
        // 3. 计算可用余额
        BigDecimal availableBalance = account.getBalance().subtract(account.getFrozenAmount());
        log.info("用户可用余额：{}，需要支付：{}", availableBalance, amount);
        
        // 4. 检查余额是否充足
        if (availableBalance.compareTo(amount) < 0) {
            log.error("账户余额不足，可用余额：{}，需要支付：{}", availableBalance, amount);
            throw new RuntimeException("账户余额不足");
        }
        
        // 5. 记录交易前余额
        BigDecimal balanceBefore = account.getBalance();
        
        // 6. 扣除余额
        account.setBalance(account.getBalance().subtract(amount));
        
        // 7. 更新累计消费金额
        BigDecimal totalConsumption = account.getTotalConsumption();
        if (totalConsumption == null) {
            totalConsumption = BigDecimal.ZERO;
        }
        account.setTotalConsumption(totalConsumption.add(amount));
        
        // 8. 更新账户
        account.setUpdatedTime(LocalDateTime.now());
        boolean updateResult = updateById(account);
        
        if (!updateResult) {
            log.error("更新账户余额失败，用户ID：{}", userId);
            throw new RuntimeException("支付失败，请重试");
        }
        
        // 9. 生成流水号
        String transactionNo = generateTransactionNo();
        
        // 10. 记录账户流水
        AccountTransactions transaction = new AccountTransactions();
        transaction.setUserId(userId);
        transaction.setAccountId(account.getId());
        transaction.setTransactionNo(transactionNo);
        transaction.setTransactionType(2); // 2-消费
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setRelatedId(orderId);
        transaction.setRelatedType("appointment_order");
        transaction.setDescription("预约服务支付 - 订单号：" + orderNo);
        transaction.setCreatedTime(LocalDateTime.now());
        
        int insertResult = accountTransactionsMapper.insert(transaction);
        
        if (insertResult <= 0) {
            log.error("记录账户流水失败，用户ID：{}", userId);
            throw new RuntimeException("支付失败，请重试");
        }
        
        log.info("预约订单支付成功，用户ID：{}，订单ID：{}，流水号：{}", userId, orderId, transactionNo);
        return true;
    }
    
    /**
     * 生成交易流水号
     * 格式：TXN + 年月日时分秒毫秒 + 4位随机数
     */
    private String generateTransactionNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = (int) (Math.random() * 10000);
        return "TXN" + timestamp + String.format("%04d", random);
    }
}
