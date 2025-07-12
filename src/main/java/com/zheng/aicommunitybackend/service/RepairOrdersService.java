package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.*;
import com.zheng.aicommunitybackend.domain.entity.RepairOrders;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.AdminRepairOrderVO;
import com.zheng.aicommunitybackend.domain.vo.RepairOrderVO;
import com.zheng.aicommunitybackend.domain.vo.RepairStatsVO;

/**
* @author ZhengJJ
* @description 针对表【repair_orders(物业报修工单表)】的数据库操作Service
* @createDate 2025-07-10 11:03:49
*/
public interface RepairOrdersService extends IService<RepairOrders> {

    /**
     * 创建报修工单
     * @param dto 报修工单创建DTO
     * @param userId 用户ID
     * @return 工单ID
     */
    Long createRepairOrder(RepairOrderCreateDTO dto, Long userId);
    
    /**
     * 取消报修工单
     * @param orderId 工单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean cancelRepairOrder(Long orderId, Long userId);
    
    /**
     * 分页查询用户的报修工单
     * @param query 查询条件
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<RepairOrderVO> pageUserRepairOrders(RepairOrderPageQuery query, Long userId);
    
    /**
     * 根据ID获取报修工单详情
     * @param orderId 工单ID
     * @param userId 用户ID
     * @return 工单详情
     */
    RepairOrderVO getRepairOrderDetail(Long orderId, Long userId);
    
    /**
     * 提交维修评价
     * @param dto 评价DTO
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean submitFeedback(RepairFeedbackDTO dto, Long userId);
    
    /**
     * 管理端分页查询报修工单
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<AdminRepairOrderVO> pageAdminRepairOrders(AdminRepairOrderPageQuery query);
    
    /**
     * 管理端获取报修工单详情
     * @param orderId 工单ID
     * @return 工单详情
     */
    AdminRepairOrderVO getAdminRepairOrderDetail(Long orderId);
    
    /**
     * 分配报修工单
     * @param dto 工单分配DTO
     * @param adminId 管理员ID
     * @return 是否成功
     */
    boolean assignRepairOrder(AdminRepairOrderAssignDTO dto, Long adminId);
    
    /**
     * 更新工单状态
     * @param dto 工单状态更新DTO
     * @param adminId 管理员ID
     * @return 是否成功
     */
    boolean updateOrderStatus(AdminRepairOrderUpdateDTO dto, Long adminId);
    
    /**
     * 获取工单统计数据
     * @return 统计数据
     */
    RepairStatsVO getRepairStats();
    
    /**
     * 验证用户是否有权限操作/查看此工单
     * @param orderId 工单ID
     * @param userId 用户ID
     */
    void validateUserOrder(Long orderId, Long userId);
}
