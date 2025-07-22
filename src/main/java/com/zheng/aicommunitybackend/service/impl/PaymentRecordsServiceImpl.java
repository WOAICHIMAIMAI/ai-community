package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.entity.PaymentRecords;
import com.zheng.aicommunitybackend.domain.dto.PaymentBillQueryDTO;
import com.zheng.aicommunitybackend.domain.dto.PaymentCreateDTO;
import com.zheng.aicommunitybackend.domain.vo.PaymentRecordVO;
import com.zheng.aicommunitybackend.domain.vo.PaymentStatisticsVO;
import com.zheng.aicommunitybackend.domain.vo.PaymentMethodVO;
import com.zheng.aicommunitybackend.domain.vo.UserAccountVO;
import com.zheng.aicommunitybackend.mapper.PaymentRecordsMapper;
import com.zheng.aicommunitybackend.service.PaymentBillsService;
import com.zheng.aicommunitybackend.service.PaymentRecordsService;
import com.zheng.aicommunitybackend.service.UserAccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 缴费记录服务实现类
 */
@Slf4j
@Service
public class PaymentRecordsServiceImpl extends ServiceImpl<PaymentRecordsMapper, PaymentRecords> 
        implements PaymentRecordsService {

    @Autowired
    private PaymentBillsService paymentBillsService;
    
    @Autowired
    private UserAccountsService userAccountsService;

    @Override
    @Transactional
    public PaymentRecordVO createPaymentOrder(Long userId, PaymentCreateDTO createDTO) {
        // 生成缴费流水号
        String paymentNo = generatePaymentNo();
        
        // 创建缴费记录
        PaymentRecords record = new PaymentRecords();
        record.setUserId(userId);
        record.setBillId(createDTO.getBillIds().get(0)); // 暂时支持单个账单
        record.setPaymentNo(paymentNo);
        record.setPaymentAmount(createDTO.getPaymentAmount());
        record.setPaymentMethod(createDTO.getPaymentMethod());
        record.setPaymentStatus(0); // 待支付
        record.setRemark(createDTO.getRemark());
        record.setCreatedTime(LocalDateTime.now());
        record.setUpdatedTime(LocalDateTime.now());
        
        save(record);
        
        // 如果是余额支付，直接处理
        if (createDTO.getPaymentMethod() == 4) {
            // 检查余额
            if (!userAccountsService.checkBalance(userId, createDTO.getPaymentAmount())) {
                throw new RuntimeException("账户余额不足");
            }
            
            // 扣除余额
            userAccountsService.consume(userId, createDTO.getPaymentAmount(), "缴费消费");
            
            // 更新支付状态
            record.setPaymentStatus(1);
            record.setPaymentTime(LocalDateTime.now());
            updateById(record);
            
            // 更新账单状态
            paymentBillsService.updateBillStatus(createDTO.getBillIds(), 1);
        }
        
        // 转换为VO返回
        PaymentRecordVO vo = new PaymentRecordVO();
        vo.setId(record.getId());
        vo.setPaymentNo(record.getPaymentNo());
        vo.setPaymentAmount(record.getPaymentAmount());
        vo.setPaymentMethod(record.getPaymentMethod());
        vo.setPaymentMethodName(getPaymentMethodName(record.getPaymentMethod()));
        vo.setPaymentStatus(record.getPaymentStatus());
        vo.setPaymentStatusName(getPaymentStatusName(record.getPaymentStatus()));
        vo.setCreatedTime(record.getCreatedTime());
        
        return vo;
    }

    @Override
    @Transactional
    public boolean confirmPayment(Long recordId, String transactionId) {
        PaymentRecords record = getById(recordId);
        if (record == null || record.getPaymentStatus() != 0) {
            return false;
        }
        
        record.setPaymentStatus(1);
        record.setTransactionId(transactionId);
        record.setPaymentTime(LocalDateTime.now());
        record.setUpdatedTime(LocalDateTime.now());
        
        boolean updated = updateById(record);
        
        if (updated) {
            // 更新账单状态
            paymentBillsService.updateBillStatus(List.of(record.getBillId()), 1);
        }
        
        return updated;
    }

    @Override
    public IPage<PaymentRecordVO> getPaymentRecordPage(Long userId, PaymentBillQueryDTO query) {
        Page<PaymentRecordVO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<PaymentRecordVO> result = baseMapper.selectPaymentRecordPage(page, userId, query);
        
        // 设置显示信息
        result.getRecords().forEach(this::setRecordDisplayInfo);
        
        return result;
    }

    @Override
    public PaymentStatisticsVO getPaymentStatistics(Long userId, Integer year) {
        return baseMapper.selectPaymentStatistics(userId, year);
    }

    @Override
    public PaymentRecordVO getByBillId(Long billId, Long userId) {
        PaymentRecordVO record = baseMapper.selectByBillId(billId, userId);
        if (record != null) {
            setRecordDisplayInfo(record);
        }
        return record;
    }

    /**
     * 生成缴费流水号
     */
    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + String.format("%04d", 
                (int)(Math.random() * 10000));
    }

    /**
     * 设置记录显示信息
     */
    private void setRecordDisplayInfo(PaymentRecordVO record) {
        record.setPaymentMethodName(getPaymentMethodName(record.getPaymentMethod()));
        record.setPaymentStatusName(getPaymentStatusName(record.getPaymentStatus()));
    }

    /**
     * 获取支付方式名称
     */
    private String getPaymentMethodName(Integer paymentMethod) {
        switch (paymentMethod) {
            case 1: return "微信支付";
            case 2: return "支付宝支付";
            case 3: return "银行卡支付";
            case 4: return "余额支付";
            default: return "未知方式";
        }
    }

    @Override
    public List<PaymentMethodVO> getPaymentMethods(Long userId) {
        List<PaymentMethodVO> methods = new ArrayList<>();

        // 获取用户账户信息
        UserAccountVO account = userAccountsService.getAccountByUserId(userId);

        // 1. 账户余额
        PaymentMethodVO balanceMethod = new PaymentMethodVO();
        balanceMethod.setId(1);
        balanceMethod.setName("账户余额");
        balanceMethod.setDescription("使用账户余额支付");
        balanceMethod.setIcon("balance-pay");
        balanceMethod.setType(4);
        balanceMethod.setEnabled(true);
        balanceMethod.setBalance(account.getBalance());
        balanceMethod.setIsDefault(true);
        balanceMethod.setSort(1);
        methods.add(balanceMethod);

        // 2. 微信支付
        PaymentMethodVO wechatMethod = new PaymentMethodVO();
        wechatMethod.setId(2);
        wechatMethod.setName("微信支付");
        wechatMethod.setDescription("使用微信余额支付");
        wechatMethod.setIcon("wechat-pay");
        wechatMethod.setType(1);
        wechatMethod.setEnabled(true);
        wechatMethod.setIsDefault(false);
        wechatMethod.setSort(2);
        methods.add(wechatMethod);

        // 3. 支付宝
        PaymentMethodVO alipayMethod = new PaymentMethodVO();
        alipayMethod.setId(3);
        alipayMethod.setName("支付宝");
        alipayMethod.setDescription("使用支付宝余额支付");
        alipayMethod.setIcon("alipay");
        alipayMethod.setType(2);
        alipayMethod.setEnabled(true);
        alipayMethod.setIsDefault(false);
        alipayMethod.setSort(3);
        methods.add(alipayMethod);

        // 4. 银行卡（示例）
        PaymentMethodVO bankCardMethod = new PaymentMethodVO();
        bankCardMethod.setId(4);
        bankCardMethod.setName("招商银行");
        bankCardMethod.setDescription("尾号1234储蓄卡");
        bankCardMethod.setIcon("credit-pay");
        bankCardMethod.setType(3);
        bankCardMethod.setEnabled(true);
        bankCardMethod.setCardNumber("****1234");
        bankCardMethod.setBankName("招商银行");
        bankCardMethod.setIsDefault(false);
        bankCardMethod.setSort(4);
        methods.add(bankCardMethod);

        return methods;
    }

    /**
     * 获取支付状态名称
     */
    private String getPaymentStatusName(Integer paymentStatus) {
        switch (paymentStatus) {
            case 0: return "待支付";
            case 1: return "支付成功";
            case 2: return "支付失败";
            case 3: return "已退款";
            default: return "未知状态";
        }
    }
}
