package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.AppointmentCreateDTO;
import com.zheng.aicommunitybackend.domain.dto.AppointmentPageQuery;
import com.zheng.aicommunitybackend.domain.dto.AppointmentRateDTO;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.AppointmentOrderVO;
import com.zheng.aicommunitybackend.domain.vo.AppointmentServiceVO;
import com.zheng.aicommunitybackend.domain.vo.AppointmentRecommendVO;

import java.util.Date;
import java.util.List;

/**
 * 预约服务接口
 */
public interface AppointmentService {

    /**
     * 获取所有服务类型
     * @return 服务类型列表
     */
    List<AppointmentServiceVO> getAllServices();

    /**
     * 获取热门服务
     * @return 热门服务列表
     */
    List<AppointmentServiceVO> getHotServices();

    /**
     * 获取推荐服务
     * @param userId 用户ID
     * @return 推荐服务列表
     */
    List<AppointmentRecommendVO> getRecommendServices(Long userId);

    /**
     * 创建预约
     * @param dto 预约信息
     * @param userId 用户ID
     * @return 订单编号
     */
    String createAppointment(AppointmentCreateDTO dto, Long userId);

    /**
     * 分页查询用户预约记录
     * @param query 查询条件
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<AppointmentOrderVO> getUserAppointmentPage(AppointmentPageQuery query, Long userId);

    /**
     * 获取预约详情
     * @param id 预约ID
     * @param userId 用户ID
     * @return 预约详情
     */
    AppointmentOrderVO getAppointmentDetail(Long id, Long userId);

    /**
     * 取消预约
     * @param id 预约ID
     * @param userId 用户ID
     * @param reason 取消原因
     * @return 是否成功
     */
    Boolean cancelAppointment(Long id, Long userId, String reason);

    /**
     * 改期预约
     * @param id 预约ID
     * @param userId 用户ID
     * @param newAppointmentTime 新的预约时间
     * @return 是否成功
     */
    Boolean rescheduleAppointment(Long id, Long userId, Date newAppointmentTime);

    /**
     * 评价服务
     * @param dto 评价信息
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean rateAppointment(AppointmentRateDTO dto, Long userId);

    /**
     * 获取最近预约记录
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近预约记录
     */
    List<AppointmentOrderVO> getRecentAppointments(Long userId, Integer limit);

    /**
     * 获取可用时间段
     * @param serviceType 服务类型
     * @param date 日期
     * @return 可用时间段
     */
    List<String> getAvailableTimeSlots(String serviceType, String date);

    /**
     * 获取服务人员列表
     * @param serviceType 服务类型
     * @return 服务人员列表
     */
    List<Object> getServiceWorkers(String serviceType);

    /**
     * 获取预约统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    Object getAppointmentStats(Long userId);
}
