package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.aicommunitybackend.domain.dto.AppointmentPageQuery;
import com.zheng.aicommunitybackend.domain.entity.AppointmentOrders;
import com.zheng.aicommunitybackend.domain.vo.AppointmentOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预约订单Mapper
 * @author ZhengJJ
 * @description 针对表【appointment_orders(预约订单表)】的数据库操作Mapper
 * @createDate 2025-01-22 10:00:00
 */
@Mapper
public interface AppointmentOrdersMapper extends BaseMapper<AppointmentOrders> {

    /**
     * 分页查询用户预约订单
     * @param page 分页参数
     * @param query 查询条件
     * @param userId 用户ID
     * @return 预约订单列表
     */
    IPage<AppointmentOrderVO> selectUserAppointmentPage(
            Page<AppointmentOrderVO> page,
            @Param("query") AppointmentPageQuery query,
            @Param("userId") Long userId
    );

    /**
     * 根据ID查询预约订单详情
     * @param id 订单ID
     * @param userId 用户ID
     * @return 预约订单详情
     */
    AppointmentOrderVO selectAppointmentDetail(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    /**
     * 查询用户最近预约记录
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近预约记录
     */
    List<AppointmentOrderVO> selectRecentAppointments(
            @Param("userId") Long userId,
            @Param("limit") Integer limit
    );
}
