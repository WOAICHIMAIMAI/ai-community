package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.aicommunitybackend.domain.entity.PaymentBills;
import com.zheng.aicommunitybackend.domain.dto.PaymentBillQueryDTO;
import com.zheng.aicommunitybackend.domain.vo.PaymentBillVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 费用账单Mapper
 */
@Mapper
public interface PaymentBillsMapper extends BaseMapper<PaymentBills> {

    /**
     * 分页查询账单列表
     */
    IPage<PaymentBillVO> selectBillPage(Page<PaymentBillVO> page, 
                                       @Param("userId") Long userId,
                                       @Param("query") PaymentBillQueryDTO query);

    /**
     * 查询用户待缴费账单
     */
    List<PaymentBillVO> selectPendingBills(@Param("userId") Long userId);

    /**
     * 根据ID查询账单详情
     */
    PaymentBillVO selectBillDetail(@Param("billId") Long billId, @Param("userId") Long userId);

    /**
     * 计算用户待缴费总金额
     */
    BigDecimal selectPendingAmount(@Param("userId") Long userId);

    /**
     * 批量更新账单状态
     */
    int updateBillStatus(@Param("billIds") List<Long> billIds, @Param("status") Integer status);
}
