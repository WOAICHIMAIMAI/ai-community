package com.zheng.aicommunitybackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.dto.AccountTransactionQueryDTO;
import com.zheng.aicommunitybackend.domain.entity.AccountTransactions;
import com.zheng.aicommunitybackend.domain.vo.AccountTransactionVO;

/**
 * 账户流水服务接口
 */
public interface AccountTransactionsService extends IService<AccountTransactions> {

    /**
     * 分页查询账户流水
     * @param userId 用户ID
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<AccountTransactionVO> getTransactionPage(Long userId, AccountTransactionQueryDTO queryDTO);

    /**
     * 记录账户流水
     * @param userId 用户ID
     * @param accountId 账户ID
     * @param transactionType 交易类型
     * @param amount 交易金额
     * @param balanceBefore 交易前余额
     * @param balanceAfter 交易后余额
     * @param relatedId 关联业务ID
     * @param relatedType 关联业务类型
     * @param description 描述
     * @return 是否成功
     */
    boolean recordTransaction(Long userId, Long accountId, Integer transactionType, 
                            java.math.BigDecimal amount, java.math.BigDecimal balanceBefore, 
                            java.math.BigDecimal balanceAfter, Long relatedId, 
                            String relatedType, String description);
}

