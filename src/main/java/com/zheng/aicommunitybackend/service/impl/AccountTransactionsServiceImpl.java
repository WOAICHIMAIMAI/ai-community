package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.dto.AccountTransactionQueryDTO;
import com.zheng.aicommunitybackend.domain.entity.AccountTransactions;
import com.zheng.aicommunitybackend.domain.vo.AccountTransactionVO;
import com.zheng.aicommunitybackend.mapper.AccountTransactionsMapper;
import com.zheng.aicommunitybackend.service.AccountTransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 账户流水服务实现类
 */
@Slf4j
@Service
public class AccountTransactionsServiceImpl extends ServiceImpl<AccountTransactionsMapper, AccountTransactions>
        implements AccountTransactionsService {

    private static final Map<Integer, String> TRANSACTION_TYPE_MAP = new HashMap<>();

    static {
        TRANSACTION_TYPE_MAP.put(1, "充值");
        TRANSACTION_TYPE_MAP.put(2, "消费");
        TRANSACTION_TYPE_MAP.put(3, "退款");
        TRANSACTION_TYPE_MAP.put(4, "冻结");
        TRANSACTION_TYPE_MAP.put(5, "解冻");
    }

    @Override
    public IPage<AccountTransactionVO> getTransactionPage(Long userId, AccountTransactionQueryDTO queryDTO) {
        // 创建分页对象
        Page<AccountTransactions> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        
        // 构建查询条件
        LambdaQueryWrapper<AccountTransactions> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountTransactions::getUserId, userId);
        
        // 交易类型
        if (queryDTO.getTransactionType() != null) {
            wrapper.eq(AccountTransactions::getTransactionType, queryDTO.getTransactionType());
        }
        
        // 关联业务类型
        if (StringUtils.hasText(queryDTO.getRelatedType())) {
            wrapper.eq(AccountTransactions::getRelatedType, queryDTO.getRelatedType());
        }
        
        // 日期范围
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (StringUtils.hasText(queryDTO.getStartDate())) {
            LocalDateTime startDateTime = LocalDate.parse(queryDTO.getStartDate(), formatter).atStartOfDay();
            wrapper.ge(AccountTransactions::getCreatedTime, startDateTime);
        }
        if (StringUtils.hasText(queryDTO.getEndDate())) {
            LocalDateTime endDateTime = LocalDate.parse(queryDTO.getEndDate(), formatter).atTime(LocalTime.MAX);
            wrapper.le(AccountTransactions::getCreatedTime, endDateTime);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(AccountTransactions::getCreatedTime);
        
        // 查询
        IPage<AccountTransactions> transactionPage = baseMapper.selectPage(page, wrapper);
        
        // 转换为VO
        return transactionPage.convert(this::convertToVO);
    }

    @Override
    public boolean recordTransaction(Long userId, Long accountId, Integer transactionType, 
                                    BigDecimal amount, BigDecimal balanceBefore, 
                                    BigDecimal balanceAfter, Long relatedId, 
                                    String relatedType, String description) {
        AccountTransactions transaction = new AccountTransactions();
        transaction.setUserId(userId);
        transaction.setAccountId(accountId);
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRelatedId(relatedId);
        transaction.setRelatedType(relatedType);
        transaction.setDescription(description);
        transaction.setCreatedTime(LocalDateTime.now());
        
        return save(transaction);
    }

    /**
     * 转换为VO
     */
    private AccountTransactionVO convertToVO(AccountTransactions entity) {
        AccountTransactionVO vo = new AccountTransactionVO();
        BeanUtils.copyProperties(entity, vo);
        
        // 设置交易类型名称
        vo.setTransactionTypeName(TRANSACTION_TYPE_MAP.getOrDefault(entity.getTransactionType(), "未知"));
        
        return vo;
    }

    /**
     * 生成交易流水号
     */
    private String generateTransactionNo() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

