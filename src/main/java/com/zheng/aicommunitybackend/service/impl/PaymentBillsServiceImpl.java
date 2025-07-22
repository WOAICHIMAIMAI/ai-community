package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.entity.PaymentBills;
import com.zheng.aicommunitybackend.domain.dto.PaymentBillQueryDTO;
import com.zheng.aicommunitybackend.domain.vo.PaymentBillVO;
import com.zheng.aicommunitybackend.mapper.PaymentBillsMapper;
import com.zheng.aicommunitybackend.service.PaymentBillsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 费用账单服务实现类
 */
@Slf4j
@Service
public class PaymentBillsServiceImpl extends ServiceImpl<PaymentBillsMapper, PaymentBills> 
        implements PaymentBillsService {

    @Override
    public IPage<PaymentBillVO> getBillPage(Long userId, PaymentBillQueryDTO query) {
        Page<PaymentBillVO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<PaymentBillVO> result = baseMapper.selectBillPage(page, userId, query);

        // 设置显示信息
        result.getRecords().forEach(this::setBillDisplayInfo);

        return result;
    }

    @Override
    public List<PaymentBillVO> getPendingBills(Long userId) {
        List<PaymentBillVO> bills = baseMapper.selectPendingBills(userId);
        
        // 设置账单类型名称和状态名称
        bills.forEach(this::setBillDisplayInfo);
        
        return bills;
    }

    @Override
    public PaymentBillVO getBillDetail(Long billId, Long userId) {
        PaymentBillVO bill = baseMapper.selectBillDetail(billId, userId);
        if (bill != null) {
            setBillDisplayInfo(bill);
        }
        return bill;
    }

    @Override
    public BigDecimal getPendingAmount(Long userId) {
        BigDecimal amount = baseMapper.selectPendingAmount(userId);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    @Override
    public boolean updateBillStatus(List<Long> billIds, Integer status) {
        return baseMapper.updateBillStatus(billIds, status) > 0;
    }

    /**
     * 设置账单显示信息
     */
    private void setBillDisplayInfo(PaymentBillVO bill) {
        // 设置账单类型名称
        bill.setBillTypeName(getBillTypeName(bill.getBillType()));
        
        // 设置状态名称
        bill.setStatusName(getBillStatusName(bill.getStatus()));
        
        // 判断是否逾期
        if (bill.getDueDate() != null && bill.getStatus() == 0) {
            bill.setIsOverdue(LocalDateTime.now().isAfter(bill.getDueDate()));
        } else {
            bill.setIsOverdue(false);
        }
    }

    /**
     * 获取账单类型名称
     */
    private String getBillTypeName(Integer billType) {
        switch (billType) {
            case 1: return "物业费";
            case 2: return "水费";
            case 3: return "电费";
            case 4: return "燃气费";
            case 5: return "停车费";
            case 6: return "其他费用";
            default: return "未知类型";
        }
    }

    /**
     * 获取账单状态名称
     */
    private String getBillStatusName(Integer status) {
        switch (status) {
            case 0: return "未缴费";
            case 1: return "已缴费";
            case 2: return "已逾期";
            case 3: return "部分缴费";
            default: return "未知状态";
        }
    }
}
