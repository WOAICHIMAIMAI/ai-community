package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.aicommunitybackend.domain.entity.PaymentRecords;
import com.zheng.aicommunitybackend.domain.dto.PaymentBillQueryDTO;
import com.zheng.aicommunitybackend.domain.vo.PaymentRecordVO;
import com.zheng.aicommunitybackend.domain.vo.PaymentStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 缴费记录Mapper
 */
@Mapper
public interface PaymentRecordsMapper extends BaseMapper<PaymentRecords> {

    /**
     * 分页查询缴费记录
     */
    IPage<PaymentRecordVO> selectPaymentRecordPage(Page<PaymentRecordVO> page,
                                                  @Param("userId") Long userId,
                                                  @Param("query") PaymentBillQueryDTO query);

    /**
     * 查询用户缴费统计信息
     */
    PaymentStatisticsVO selectPaymentStatistics(@Param("userId") Long userId,
                                               @Param("year") Integer year);

    /**
     * 根据账单ID查询缴费记录
     */
    PaymentRecordVO selectByBillId(@Param("billId") Long billId, @Param("userId") Long userId);
}
