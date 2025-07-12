package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.AdminProgressPageQuery;
import com.zheng.aicommunitybackend.domain.dto.RepairProgressDTO;
import com.zheng.aicommunitybackend.domain.entity.RepairProgress;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.RepairProgressVO;

import java.util.List;

/**
* @author ZhengJJ
* @description 针对表【repair_progress(报修进度跟踪表)】的数据库操作Service
* @createDate 2025-07-10 11:03:49
*/
public interface RepairProgressService extends IService<RepairProgress> {

    /**
     * 添加维修进度记录
     * @param dto 进度记录DTO
     * @param userId 操作用户ID
     * @param operatorType 操作人类型：1-用户 2-维修工 3-系统
     * @return 进度记录ID
     */
    Long addProgress(RepairProgressDTO dto, Long userId, Integer operatorType);
    
    /**
     * 获取报修单的所有进度记录
     * @param orderId 报修单ID
     * @return 进度记录列表
     */
    List<RepairProgressVO> getOrderProgressList(Long orderId);
    
    /**
     * 记录系统自动操作的进度
     * @param orderId 报修单ID
     * @param action 操作类型
     * @param description 操作描述
     */
    void recordSystemProgress(Long orderId, String action, String description);
    
    /**
     * 分页查询维修进度记录
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<RepairProgressVO> pageProgressRecords(AdminProgressPageQuery query);
    
    /**
     * 根据ID获取进度详情
     * @param progressId 进度记录ID
     * @return 进度记录详情
     */
    RepairProgressVO getProgressById(Long progressId);
    
    /**
     * 删除进度记录
     * @param progressId 进度记录ID
     * @param adminId 管理员ID
     * @return 是否成功
     */
    boolean deleteProgress(Long progressId, Long adminId);
}
