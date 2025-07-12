package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.RepairWorkerDTO;
import com.zheng.aicommunitybackend.domain.dto.RepairWorkerPageQuery;
import com.zheng.aicommunitybackend.domain.dto.RepairWorkerStatusDTO;
import com.zheng.aicommunitybackend.domain.entity.RepairWorkers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.RepairOrderVO;
import com.zheng.aicommunitybackend.domain.vo.RepairWorkerVO;
import com.zheng.aicommunitybackend.domain.vo.WorkerStatsVO;

import java.util.List;

/**
* @author ZhengJJ
* @description 针对表【repair_workers(维修工信息表)】的数据库操作Service
* @createDate 2025-07-10 11:03:49
*/
public interface RepairWorkersService extends IService<RepairWorkers> {

    /**
     * 分页查询维修工列表
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<RepairWorkerVO> pageWorkers(RepairWorkerPageQuery query);
    
    /**
     * 根据ID获取维修工详情
     * @param workerId 维修工ID
     * @return 维修工详情
     */
    RepairWorkerVO getWorkerById(Long workerId);
    
    /**
     * 根据服务类型查询可用维修工
     * @param serviceType 服务类型
     * @return 维修工列表
     */
    List<RepairWorkerVO> getAvailableWorkersByType(String serviceType);
    
    /**
     * 添加维修工
     * @param dto 维修工信息
     * @return 维修工ID
     */
    Long addWorker(RepairWorkerDTO dto);
    
    /**
     * 更新维修工信息
     * @param dto 维修工信息
     * @return 是否成功
     */
    boolean updateWorker(RepairWorkerDTO dto);
    
    /**
     * 删除维修工
     * @param workerId 维修工ID
     * @return 是否成功
     */
    boolean deleteWorker(Long workerId);
    
    /**
     * 更新维修工工作状态
     * @param dto 状态更新DTO
     * @return 是否成功
     */
    boolean updateWorkerStatus(RepairWorkerStatusDTO dto);
    
    /**
     * 获取维修工绩效统计
     * @param workerId 维修工ID
     * @return 绩效统计
     */
    WorkerStatsVO getWorkerStats(Long workerId);
    
    /**
     * 获取维修工负责的工单列表
     * @param workerId 维修工ID
     * @param status 工单状态，可选
     * @return 工单列表
     */
    List<RepairOrderVO> getWorkerOrders(Long workerId, Integer status);
    
    /**
     * 更新维修工评分
     * @param workerId 维修工ID
     */
    void updateWorkerRating(Long workerId);
}
