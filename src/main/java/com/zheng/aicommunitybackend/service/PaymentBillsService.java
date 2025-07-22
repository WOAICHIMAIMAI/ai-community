package com.zheng.aicommunitybackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.entity.PaymentBills;
import com.zheng.aicommunitybackend.domain.dto.PaymentBillQueryDTO;
import com.zheng.aicommunitybackend.domain.vo.PaymentBillVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 费用账单服务接口
 */
public interface PaymentBillsService extends IService<PaymentBills> {

    /**
     * 分页查询账单列表
     */
    IPage<PaymentBillVO> getBillPage(Long userId, PaymentBillQueryDTO query);

    /**
     * 查询用户待缴费账单
     */
    List<PaymentBillVO> getPendingBills(Long userId);

    /**
     * 根据ID查询账单详情
     */
    PaymentBillVO getBillDetail(Long billId, Long userId);

    /**
     * 计算用户待缴费总金额
     */
    BigDecimal getPendingAmount(Long userId);

    /**
     * 批量更新账单状态
     */
    boolean updateBillStatus(List<Long> billIds, Integer status);
}
