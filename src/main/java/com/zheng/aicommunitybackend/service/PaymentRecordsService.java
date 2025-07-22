package com.zheng.aicommunitybackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.entity.PaymentRecords;
import com.zheng.aicommunitybackend.domain.dto.PaymentBillQueryDTO;
import com.zheng.aicommunitybackend.domain.dto.PaymentCreateDTO;
import com.zheng.aicommunitybackend.domain.vo.PaymentRecordVO;
import com.zheng.aicommunitybackend.domain.vo.PaymentStatisticsVO;
import com.zheng.aicommunitybackend.domain.vo.PaymentMethodVO;

import java.util.List;

/**
 * 缴费记录服务接口
 */
public interface PaymentRecordsService extends IService<PaymentRecords> {

    /**
     * 创建缴费订单
     */
    PaymentRecordVO createPaymentOrder(Long userId, PaymentCreateDTO createDTO);

    /**
     * 确认支付
     */
    boolean confirmPayment(Long recordId, String transactionId);

    /**
     * 分页查询缴费记录
     */
    IPage<PaymentRecordVO> getPaymentRecordPage(Long userId, PaymentBillQueryDTO query);

    /**
     * 查询用户缴费统计信息
     */
    PaymentStatisticsVO getPaymentStatistics(Long userId, Integer year);

    /**
     * 根据账单ID查询缴费记录
     */
    PaymentRecordVO getByBillId(Long billId, Long userId);

    /**
     * 获取支付方式列表
     */
    List<PaymentMethodVO> getPaymentMethods(Long userId);
}
