package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.dto.AdminProgressPageQuery;
import com.zheng.aicommunitybackend.domain.dto.RepairProgressDTO;
import com.zheng.aicommunitybackend.domain.entity.RepairOrders;
import com.zheng.aicommunitybackend.domain.entity.RepairProgress;
import com.zheng.aicommunitybackend.domain.entity.Users;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.RepairProgressVO;
import com.zheng.aicommunitybackend.exception.BaseException;
import com.zheng.aicommunitybackend.mapper.RepairOrdersMapper;
import com.zheng.aicommunitybackend.mapper.RepairProgressMapper;
import com.zheng.aicommunitybackend.mapper.RepairWorkersMapper;
import com.zheng.aicommunitybackend.mapper.UsersMapper;
import com.zheng.aicommunitybackend.service.RepairProgressService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author ZhengJJ
* @description 针对表【repair_progress(报修进度跟踪表)】的数据库操作Service实现
* @createDate 2025-07-10 11:03:49
*/
@Service
public class RepairProgressServiceImpl extends ServiceImpl<RepairProgressMapper, RepairProgress>
    implements RepairProgressService {

    @Autowired
    private RepairOrdersMapper repairOrdersMapper;
    
    @Autowired
    private UsersMapper usersMapper;
    
    @Autowired
    private RepairWorkersMapper repairWorkersMapper;

    @Override
    public Long addProgress(RepairProgressDTO dto, Long userId, Integer operatorType) {
        // 1. 校验工单是否存在
        RepairOrders order = repairOrdersMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BaseException("工单不存在");
        }
        
        // 2. 构建进度对象
        RepairProgress progress = new RepairProgress();
        BeanUtils.copyProperties(dto, progress);
        progress.setOperatorType(operatorType);
        progress.setOperatorId(userId);
        progress.setCreateTime(new Date());
        
        // 3. 保存进度记录
        save(progress);
        
        return progress.getId();
    }

    @Override
    public List<RepairProgressVO> getOrderProgressList(Long orderId) {
        // 1. 校验工单是否存在
        RepairOrders order = repairOrdersMapper.selectById(orderId);
        if (order == null) {
            throw new BaseException("工单不存在");
        }
        
        // 2. 查询工单的所有进度记录，按时间正序排列
        LambdaQueryWrapper<RepairProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairProgress::getOrderId, orderId)
               .orderByAsc(RepairProgress::getCreateTime);
        
        List<RepairProgress> progressList = list(wrapper);
        
        // 3. 转换为VO列表并返回
        return convertToVOList(progressList);
    }

    @Override
    public void recordSystemProgress(Long orderId, String action, String description) {
        // 系统自动记录进度，操作人类型为系统(3)，操作人ID为0
        RepairProgress progress = new RepairProgress();
        progress.setOrderId(orderId);
        progress.setOperatorType(3); // 系统操作
        progress.setOperatorId(0L); // 系统操作ID为0
        progress.setAction(action);
        progress.setDescription(description);
        progress.setCreateTime(new Date());
        
        save(progress);
    }
    
    @Override
    public PageResult<RepairProgressVO> pageProgressRecords(AdminProgressPageQuery query) {
        // 1. 构建查询条件
        LambdaQueryWrapper<RepairProgress> wrapper = new LambdaQueryWrapper<>();
        
        // 2. 添加各种筛选条件
        if (query.getOrderId() != null) {
            wrapper.eq(RepairProgress::getOrderId, query.getOrderId());
        }
        
        if (query.getOperatorType() != null) {
            wrapper.eq(RepairProgress::getOperatorType, query.getOperatorType());
        }
        
        if (query.getOperatorId() != null) {
            wrapper.eq(RepairProgress::getOperatorId, query.getOperatorId());
        }
        
        if (StringUtils.hasText(query.getAction())) {
            wrapper.eq(RepairProgress::getAction, query.getAction());
        }
        
        if (query.getStartTime() != null && query.getEndTime() != null) {
            wrapper.between(RepairProgress::getCreateTime, query.getStartTime(), query.getEndTime());
        } else if (query.getStartTime() != null) {
            wrapper.ge(RepairProgress::getCreateTime, query.getStartTime());
        } else if (query.getEndTime() != null) {
            wrapper.le(RepairProgress::getCreateTime, query.getEndTime());
        }
        
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(RepairProgress::getDescription, query.getKeyword());
        }
        
        // 3. 排序
        String sortField = query.getSortField();
        String sortOrder = query.getSortOrder();
        if (StringUtils.hasText(sortField)) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortField) {
                case "id":
                    wrapper.orderBy(true, isAsc, RepairProgress::getId);
                    break;
                case "createTime":
                    wrapper.orderBy(true, isAsc, RepairProgress::getCreateTime);
                    break;
                case "operatorType":
                    wrapper.orderBy(true, isAsc, RepairProgress::getOperatorType);
                    break;
                default:
                    wrapper.orderByDesc(RepairProgress::getCreateTime);
            }
        } else {
            // 默认按创建时间倒序
            wrapper.orderByDesc(RepairProgress::getCreateTime);
        }
        
        // 4. 执行分页查询
        Page<RepairProgress> page = page(
                new Page<>(query.getPage(), query.getPageSize()),
                wrapper
        );
        
        // 5. 转换为VO列表
        List<RepairProgressVO> voList = convertToVOList(page.getRecords());
        
        // 6. 返回分页结果
        return new PageResult<>(page.getTotal(), voList);
    }
    
    @Override
    public RepairProgressVO getProgressById(Long progressId) {
        // 1. 查询进度记录
        RepairProgress progress = getById(progressId);
        if (progress == null) {
            throw new BaseException("进度记录不存在");
        }
        
        // 2. 转换为VO并返回
        return convertToVO(progress);
    }
    
    @Override
    @Transactional
    public boolean deleteProgress(Long progressId, Long adminId) {
        // 1. 查询进度记录
        RepairProgress progress = getById(progressId);
        if (progress == null) {
            throw new BaseException("进度记录不存在");
        }
        
        // 2. 系统生成的进度记录不允许删除
        if (progress.getOperatorType() == 3) { // 3-系统
            throw new BaseException("系统生成的进度记录不允许删除");
        }
        
        // 3. 删除进度记录
        boolean result = removeById(progressId);
        
        // 4. 记录删除操作
        if (result) {
            // 创建一条删除记录的系统进度
            RepairProgress deleteRecord = new RepairProgress();
            deleteRecord.setOrderId(progress.getOrderId());
            deleteRecord.setOperatorType(4); // 管理员
            deleteRecord.setOperatorId(adminId);
            deleteRecord.setAction("删除进度记录");
            deleteRecord.setDescription("管理员删除了一条进度记录");
            deleteRecord.setCreateTime(new Date());
            save(deleteRecord);
        }
        
        return result;
    }
    
    /**
     * 将实体对象转换为VO
     * 
     * @param progress 进度实体
     * @return 进度VO
     */
    private RepairProgressVO convertToVO(RepairProgress progress) {
        if (progress == null) {
            return null;
        }
        
        RepairProgressVO vo = new RepairProgressVO();
        BeanUtils.copyProperties(progress, vo);
        
        // 设置操作人类型描述
        vo.setOperatorTypeDesc(convertOperatorTypeToDesc(progress.getOperatorType()));
        
        // 设置操作人名称
        if (progress.getOperatorType() != null) {
            switch (progress.getOperatorType()) {
                case 1: // 用户
                    if (progress.getOperatorId() != null && progress.getOperatorId() > 0) {
                        Users user = usersMapper.selectById(progress.getOperatorId());
                        if (user != null) {
                            vo.setOperatorName(user.getNickname());
                        }
                    }
                    break;
                case 2: // 维修工
                    if (progress.getOperatorId() != null && progress.getOperatorId() > 0) {
                        com.zheng.aicommunitybackend.domain.entity.RepairWorkers worker = 
                                repairWorkersMapper.selectById(progress.getOperatorId());
                        if (worker != null) {
                            vo.setOperatorName(worker.getName());
                        }
                    }
                    break;
                case 3: // 系统
                    vo.setOperatorName("系统");
                    break;
                case 4: // 管理员
                    if (progress.getOperatorId() != null && progress.getOperatorId() > 0) {
                        Users admin = usersMapper.selectById(progress.getOperatorId());
                        if (admin != null) {
                            vo.setOperatorName(admin.getNickname() + "(管理员)");
                        }
                    }
                    break;
            }
        }
        
        return vo;
    }
    
    /**
     * 将实体列表转换为VO列表
     * 
     * @param progressList 进度实体列表
     * @return 进度VO列表
     */
    private List<RepairProgressVO> convertToVOList(List<RepairProgress> progressList) {
        List<RepairProgressVO> voList = new ArrayList<>();
        if (progressList != null && !progressList.isEmpty()) {
            for (RepairProgress progress : progressList) {
                voList.add(convertToVO(progress));
            }
        }
        return voList;
    }
    
    /**
     * 转换操作人类型为描述文本
     * 
     * @param operatorType 操作人类型码
     * @return 操作人类型描述
     */
    private String convertOperatorTypeToDesc(Integer operatorType) {
        if (operatorType == null) {
            return "未知";
        }
        
        switch (operatorType) {
            case 1:
                return "用户";
            case 2:
                return "维修工";
            case 3:
                return "系统";
            case 4:
                return "管理员";
            default:
                return "未知";
        }
    }
}




