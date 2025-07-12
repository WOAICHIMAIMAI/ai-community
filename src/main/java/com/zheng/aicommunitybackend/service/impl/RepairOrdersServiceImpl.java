package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.dto.*;
import com.zheng.aicommunitybackend.domain.entity.RepairOrders;
import com.zheng.aicommunitybackend.domain.entity.RepairWorkers;
import com.zheng.aicommunitybackend.domain.entity.Users;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.AdminRepairOrderVO;
import com.zheng.aicommunitybackend.domain.vo.RepairOrderVO;
import com.zheng.aicommunitybackend.domain.vo.RepairStatsVO;
import com.zheng.aicommunitybackend.exception.BaseException;
import com.zheng.aicommunitybackend.mapper.RepairOrdersMapper;
import com.zheng.aicommunitybackend.mapper.RepairWorkersMapper;
import com.zheng.aicommunitybackend.mapper.UsersMapper;
import com.zheng.aicommunitybackend.service.RepairOrdersService;
import com.zheng.aicommunitybackend.service.RepairProgressService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author ZhengJJ
* @description 针对表【repair_orders(物业报修工单表)】的数据库操作Service实现
* @createDate 2025-07-10 11:03:49
*/
@Service
public class RepairOrdersServiceImpl extends ServiceImpl<RepairOrdersMapper, RepairOrders>
    implements RepairOrdersService {
    
    @Autowired
    private RepairProgressService repairProgressService;
    
    @Autowired
    private RepairWorkersMapper repairWorkersMapper;
    
    @Autowired
    private UsersMapper usersMapper;
    
    /**
     * 工单状态常量
     */
    public static final int STATUS_PENDING = 0; // 待受理
    public static final int STATUS_ASSIGNED = 1; // 已分配
    public static final int STATUS_PROCESSING = 2; // 处理中
    public static final int STATUS_COMPLETED = 3; // 已完成
    public static final int STATUS_CANCELLED = 4; // 已取消
    
    /**
     * 操作人类型
     */
    public static final int OPERATOR_TYPE_USER = 1; // 用户
    public static final int OPERATOR_TYPE_WORKER = 2; // 维修工
    public static final int OPERATOR_TYPE_SYSTEM = 3; // 系统
    public static final int OPERATOR_TYPE_ADMIN = 4; // 管理员

    @Override
    @Transactional
    public Long createRepairOrder(RepairOrderCreateDTO dto, Long userId) {
        // 1. 构建工单对象并设置初始属性
        RepairOrders order = new RepairOrders();
        BeanUtils.copyProperties(dto, order);
        
        // 2. 设置其他必要属性
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(userId);
        order.setStatus(STATUS_PENDING);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        
        // 3. 保存工单到数据库
        save(order);
        
        // 4. 记录创建工单的进度
        repairProgressService.recordSystemProgress(
                order.getId(), 
                "创建工单", 
                "用户创建了报修工单，等待物业受理");
        
        return order.getId();
    }

    @Override
    @Transactional
    public boolean cancelRepairOrder(Long orderId, Long userId) {
        // 1. 查询工单并校验
        RepairOrders order = getById(orderId);
        if (order == null) {
            throw new BaseException("工单不存在");
        }
        
        // 2. 验证工单归属权
        if (!order.getUserId().equals(userId)) {
            throw new BaseException("您无权操作此工单");
        }
        
        // 3. 验证工单状态是否允许取消
        if (order.getStatus() >= STATUS_PROCESSING) {
            throw new BaseException("该工单已在处理中或已完成，无法取消");
        }
        
        // 4. 更新工单状态为已取消
        order.setStatus(STATUS_CANCELLED);
        order.setUpdateTime(new Date());
        boolean result = updateById(order);
        
        // 5. 记录取消工单的进度
        if (result) {
            repairProgressService.recordSystemProgress(
                    orderId,
                    "取消工单",
                    "用户取消了报修工单");
        }
        
        return result;
    }

    @Override
    public PageResult<RepairOrderVO> pageUserRepairOrders(RepairOrderPageQuery query, Long userId) {
        // 1. 构建查询条件
        LambdaQueryWrapper<RepairOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrders::getUserId, userId);
        
        // 2. 添加其他查询条件
        if (query.getStatus() != null) {
            wrapper.eq(RepairOrders::getStatus, query.getStatus());
        }
        
        if (StringUtils.hasText(query.getRepairType())) {
            wrapper.eq(RepairOrders::getRepairType, query.getRepairType());
        }
        
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(RepairOrders::getTitle, query.getKeyword())
                    .or()
                    .like(RepairOrders::getDescription, query.getKeyword())
                    .or()
                    .like(RepairOrders::getOrderNumber, query.getKeyword())
            );
        }
        
        if (query.getStartTime() != null && query.getEndTime() != null) {
            wrapper.between(RepairOrders::getCreateTime, query.getStartTime(), query.getEndTime());
        } else if (query.getStartTime() != null) {
            wrapper.ge(RepairOrders::getCreateTime, query.getStartTime());
        } else if (query.getEndTime() != null) {
            wrapper.le(RepairOrders::getCreateTime, query.getEndTime());
        }
        
        // 3. 排序
        String sortField = query.getSortField();
        String sortOrder = query.getSortOrder();
        if (StringUtils.hasText(sortField)) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortField) {
                case "createTime":
                    wrapper.orderBy(true, isAsc, RepairOrders::getCreateTime);
                    break;
                case "status":
                    wrapper.orderBy(true, isAsc, RepairOrders::getStatus);
                    break;
                case "expectedTime":
                    wrapper.orderBy(true, isAsc, RepairOrders::getExpectedTime);
                    break;
                default:
                    wrapper.orderByDesc(RepairOrders::getCreateTime);
            }
        } else {
            // 默认按创建时间倒序
            wrapper.orderByDesc(RepairOrders::getCreateTime);
        }
        
        // 4. 执行分页查询
        Page<RepairOrders> page = page(
                new Page<>(query.getPage(), query.getPageSize()),
                wrapper
        );
        
        // 5. 转换为VO列表
        List<RepairOrderVO> voList = convertToVOList(page.getRecords());
        
        // 6. 返回分页结果
        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public RepairOrderVO getRepairOrderDetail(Long orderId, Long userId) {
        // 1. 查询工单
        RepairOrders order = getById(orderId);
        if (order == null) {
            throw new BaseException("工单不存在");
        }
        
        // 2. 验证归属权
        if (!order.getUserId().equals(userId)) {
            throw new BaseException("您无权查看此工单");
        }
        
        // 3. 转换为VO并返回
        return convertToVO(order);
    }

    @Override
    @Transactional
    public boolean submitFeedback(RepairFeedbackDTO dto, Long userId) {
        // 1. 查询工单并校验
        RepairOrders order = getById(dto.getOrderId());
        if (order == null) {
            throw new BaseException("工单不存在");
        }
        
        // 2. 验证工单归属权
        if (!order.getUserId().equals(userId)) {
            throw new BaseException("您无权操作此工单");
        }
        
        // 3. 验证工单状态是否已完成
        if (order.getStatus() != STATUS_COMPLETED) {
            throw new BaseException("工单尚未完成，无法评价");
        }
        
        // 4. 更新工单的满意度评价和反馈
        order.setSatisfactionLevel(dto.getSatisfactionLevel());
        order.setFeedback(dto.getFeedback());
        order.setUpdateTime(new Date());
        boolean result = updateById(order);
        
        // 5. 记录评价进度
        if (result) {
            repairProgressService.recordSystemProgress(
                    dto.getOrderId(),
                    "提交评价",
                    "用户对维修服务进行了评价：" + dto.getSatisfactionLevel() + "星");
            
            // 更新维修工的评分
            updateWorkerRating(order.getWorkerId());
        }
        
        return result;
    }

    @Override
    public PageResult<AdminRepairOrderVO> pageAdminRepairOrders(AdminRepairOrderPageQuery query) {
        // 1. 构建查询条件
        LambdaQueryWrapper<RepairOrders> wrapper = new LambdaQueryWrapper<>();
        
        // 2. 添加各种筛选条件
        if (query.getUserId() != null) {
            wrapper.eq(RepairOrders::getUserId, query.getUserId());
        }
        
        if (StringUtils.hasText(query.getOrderNumber())) {
            wrapper.eq(RepairOrders::getOrderNumber, query.getOrderNumber());
        }
        
        if (StringUtils.hasText(query.getRepairType())) {
            wrapper.eq(RepairOrders::getRepairType, query.getRepairType());
        }
        
        if (query.getStatus() != null) {
            wrapper.eq(RepairOrders::getStatus, query.getStatus());
        }
        
        if (query.getWorkerId() != null) {
            wrapper.eq(RepairOrders::getWorkerId, query.getWorkerId());
        }
        
        if (query.getStartTime() != null && query.getEndTime() != null) {
            wrapper.between(RepairOrders::getCreateTime, query.getStartTime(), query.getEndTime());
        } else if (query.getStartTime() != null) {
            wrapper.ge(RepairOrders::getCreateTime, query.getStartTime());
        } else if (query.getEndTime() != null) {
            wrapper.le(RepairOrders::getCreateTime, query.getEndTime());
        }
        
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(RepairOrders::getTitle, query.getKeyword())
                    .or()
                    .like(RepairOrders::getDescription, query.getKeyword())
                    .or()
                    .like(RepairOrders::getContactPhone, query.getKeyword())
            );
        }
        
        // 3. 排序
        String sortField = query.getSortField();
        String sortOrder = query.getSortOrder();
        if (StringUtils.hasText(sortField)) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortField) {
                case "id":
                    wrapper.orderBy(true, isAsc, RepairOrders::getId);
                    break;
                case "createTime":
                    wrapper.orderBy(true, isAsc, RepairOrders::getCreateTime);
                    break;
                case "status":
                    wrapper.orderBy(true, isAsc, RepairOrders::getStatus);
                    break;
                case "expectedTime":
                    wrapper.orderBy(true, isAsc, RepairOrders::getExpectedTime);
                    break;
                default:
                    wrapper.orderByDesc(RepairOrders::getCreateTime);
            }
        } else {
            // 默认按创建时间倒序
            wrapper.orderByDesc(RepairOrders::getCreateTime);
        }
        
        // 4. 执行分页查询
        Page<RepairOrders> page = page(
                new Page<>(query.getPage(), query.getPageSize()),
                wrapper
        );
        
        // 5. 转换为管理端VO列表
        List<AdminRepairOrderVO> voList = convertToAdminVOList(page.getRecords());
        
        // 6. 返回分页结果
        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public AdminRepairOrderVO getAdminRepairOrderDetail(Long orderId) {
        // 1. 查询工单
        RepairOrders order = getById(orderId);
        if (order == null) {
            throw new BaseException("工单不存在");
        }
        
        // 2. 转换为管理端VO并返回
        return convertToAdminVO(order);
    }

    @Override
    @Transactional
    public boolean assignRepairOrder(AdminRepairOrderAssignDTO dto, Long adminId) {
        // 1. 查询工单并校验
        RepairOrders order = getById(dto.getOrderId());
        if (order == null) {
            throw new BaseException("工单不存在");
        }
        
        // 2. 验证工单状态是否为待受理
        if (order.getStatus() != STATUS_PENDING) {
            throw new BaseException("工单状态不是待受理，无法分配");
        }
        
        // 3. 查询维修工是否存在
        RepairWorkers worker = repairWorkersMapper.selectById(dto.getWorkerId());
        if (worker == null) {
            throw new BaseException("维修工不存在");
        }
        
        // 4. 验证维修工状态是否可接单
        if (worker.getWorkStatus() != 1) { // 1-可接单
            throw new BaseException("维修工当前无法接单");
        }
        
        // 5. 更新工单信息
        order.setWorkerId(worker.getId());
        order.setWorkerName(worker.getName());
        order.setWorkerPhone(worker.getPhone());
        order.setStatus(STATUS_ASSIGNED);
        order.setAppointmentTime(dto.getAppointmentTime());
        order.setUpdateTime(new Date());
        boolean result = updateById(order);
        
        // 6. 记录分配进度
        if (result) {
            String remark = StringUtils.hasText(dto.getRemark()) ? "，备注：" + dto.getRemark() : "";
            repairProgressService.addProgress(
                    createProgressDTO(
                            dto.getOrderId(),
                            "分配维修工",
                            "管理员已将工单分配给维修工" + worker.getName() + 
                                    "，预约上门时间：" + formatDate(dto.getAppointmentTime()) + remark
                    ),
                    adminId,
                    OPERATOR_TYPE_ADMIN
            );
            
            // 更新维修工状态为忙碌
            worker.setWorkStatus(2); // 2-忙碌
            repairWorkersMapper.updateById(worker);
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(AdminRepairOrderUpdateDTO dto, Long adminId) {
        // 1. 查询工单并校验
        RepairOrders order = getById(dto.getOrderId());
        if (order == null) {
            throw new BaseException("工单不存在");
        }
        
        // 2. 验证状态变更的合法性
        validateStatusChange(order.getStatus(), dto.getStatus());
        
        // 3. 更新工单状态
        order.setStatus(dto.getStatus());
        order.setUpdateTime(new Date());
        
        // 如果状态变为已完成，设置完成时间
        if (dto.getStatus() == STATUS_COMPLETED) {
            order.setCompletionTime(new Date());
            
            // 如果有维修工，则更新维修工状态为可接单
            if (order.getWorkerId() != null) {
                RepairWorkers worker = repairWorkersMapper.selectById(order.getWorkerId());
                if (worker != null) {
                    worker.setWorkStatus(1); // 1-可接单
                    repairWorkersMapper.updateById(worker);
                }
            }
        }
        
        boolean result = updateById(order);
        
        // 4. 记录状态变更进度
        if (result) {
            String statusDesc = convertStatusToDesc(dto.getStatus());
            String remark = StringUtils.hasText(dto.getRemark()) ? "，备注：" + dto.getRemark() : "";
            repairProgressService.addProgress(
                    createProgressDTO(
                            dto.getOrderId(),
                            "更新状态",
                            "管理员将工单状态更新为：" + statusDesc + remark
                    ),
                    adminId,
                    OPERATOR_TYPE_ADMIN
            );
        }
        
        return result;
    }

    @Override
    public RepairStatsVO getRepairStats() {
        RepairStatsVO stats = new RepairStatsVO();
        
        // 1. 查询总工单数
        stats.setTotalCount(count());
        
        // 2. 按状态分类统计
        Map<Integer, Long> statusCounts = getStatusCounts();
        stats.setPendingCount(statusCounts.getOrDefault(STATUS_PENDING, 0L));
        stats.setAssignedCount(statusCounts.getOrDefault(STATUS_ASSIGNED, 0L));
        stats.setProcessingCount(statusCounts.getOrDefault(STATUS_PROCESSING, 0L));
        stats.setCompletedCount(statusCounts.getOrDefault(STATUS_COMPLETED, 0L));
        stats.setCancelledCount(statusCounts.getOrDefault(STATUS_CANCELLED, 0L));
        
        // 3. 今日工单统计
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
        
        Date todayStartDate = Date.from(todayStart.atZone(ZoneId.systemDefault()).toInstant());
        Date todayEndDate = Date.from(todayEnd.atZone(ZoneId.systemDefault()).toInstant());
        
        LambdaQueryWrapper<RepairOrders> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.between(RepairOrders::getCreateTime, todayStartDate, todayEndDate);
        stats.setTodayCount(count(todayWrapper));
        
        LambdaQueryWrapper<RepairOrders> todayCompletedWrapper = new LambdaQueryWrapper<>();
        todayCompletedWrapper.eq(RepairOrders::getStatus, STATUS_COMPLETED)
                .between(RepairOrders::getCompletionTime, todayStartDate, todayEndDate);
        stats.setTodayCompletedCount(count(todayCompletedWrapper));
        
        // 4. 本周工单统计
        LocalDate firstDayOfWeek = today.with(DayOfWeek.MONDAY);
        Date weekStartDate = Date.from(firstDayOfWeek.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        
        LambdaQueryWrapper<RepairOrders> weekWrapper = new LambdaQueryWrapper<>();
        weekWrapper.between(RepairOrders::getCreateTime, weekStartDate, todayEndDate);
        stats.setWeekCount(count(weekWrapper));
        
        // 5. 本月工单统计
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        Date monthStartDate = Date.from(firstDayOfMonth.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        
        LambdaQueryWrapper<RepairOrders> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.between(RepairOrders::getCreateTime, monthStartDate, todayEndDate);
        stats.setMonthCount(count(monthWrapper));
        
        // 6. 报修类型分布
        stats.setTypeCounts(getTypeCounts());
        
        // 7. 满意度分布和平均分
        Map<Integer, Long> satisfactionCounts = getSatisfactionCounts();
        stats.setSatisfactionCounts(satisfactionCounts);
        stats.setAvgSatisfaction(calculateAvgSatisfaction(satisfactionCounts));
        
        return stats;
    }
    
    @Override
    public void validateUserOrder(Long orderId, Long userId) {
        // 1. 查询工单
        RepairOrders order = getById(orderId);
        if (order == null) {
            throw new BaseException("工单不存在");
        }
        
        // 2. 验证工单归属权
        if (!order.getUserId().equals(userId)) {
            throw new BaseException("您无权操作此工单");
        }
    }
    
    /**
     * 更新维修工的评分
     * 
     * @param workerId 维修工ID
     */
    private void updateWorkerRating(Long workerId) {
        if (workerId == null) {
            return;
        }
        
        // 1. 查询该维修工的所有已评价工单
        LambdaQueryWrapper<RepairOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrders::getWorkerId, workerId)
                .eq(RepairOrders::getStatus, STATUS_COMPLETED)
                .isNotNull(RepairOrders::getSatisfactionLevel);
        
        List<RepairOrders> orders = list(wrapper);
        
        // 2. 计算平均分
        if (orders.isEmpty()) {
            return;
        }
        
        double totalScore = 0;
        for (RepairOrders order : orders) {
            totalScore += order.getSatisfactionLevel();
        }
        
        double avgScore = totalScore / orders.size();
        
        // 3. 更新维修工评分
        RepairWorkers worker = repairWorkersMapper.selectById(workerId);
        if (worker != null) {
            worker.setRating(new java.math.BigDecimal(avgScore));
            worker.setServiceCount(orders.size());
            repairWorkersMapper.updateById(worker);
        }
    }
    
    /**
     * 校验工单状态变更的合法性
     *
     * @param currentStatus 当前状态
     * @param newStatus 新状态
     */
    private void validateStatusChange(Integer currentStatus, Integer newStatus) {
        if (currentStatus == null || newStatus == null) {
            throw new BaseException("工单状态不能为空");
        }
        
        // 已完成或已取消的工单不能更改状态
        if (currentStatus == STATUS_COMPLETED || currentStatus == STATUS_CANCELLED) {
            throw new BaseException("工单已完成或已取消，无法更改状态");
        }
        
        // 状态只能按顺序变更，不能跳级或回退
        if (newStatus != STATUS_CANCELLED) { // 取消状态可以从任何未完成状态直接变更
            if (newStatus <= currentStatus || newStatus > currentStatus + 1) {
                throw new BaseException("工单状态变更不合法");
            }
        }
        
        // 如果变更为已分配，需要检查是否已指定维修工
        if (newStatus == STATUS_ASSIGNED) {
            // 这种检查应该在分配维修工的接口中完成，此处略过
        }
    }
    
    /**
     * 获取按状态分类的工单数量
     *
     * @return 状态 -> 数量的映射
     */
    private Map<Integer, Long> getStatusCounts() {
        QueryWrapper<RepairOrders> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status, count(*) as count_num")
                    .groupBy("status");
        
        List<Map<String, Object>> maps = getBaseMapper().selectMaps(queryWrapper);
        
        Map<Integer, Long> result = new HashMap<>();
        for (Map<String, Object> map : maps) {
            Integer status = (Integer) map.get("status");
            // 使用正确的字段名"count_num"获取计数
            Long count = ((Number) map.get("count_num")).longValue(); // 适应不同数据库可能返回不同数字类型
            if (status != null && count != null) {
                result.put(status, count);
            }
        }
        return result;
    }
    
    /**
     * 获取按报修类型分类的工单数量
     *
     * @return 报修类型 -> 数量的映射
     */
    private Map<String, Long> getTypeCounts() {
        QueryWrapper<RepairOrders> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("repair_type, count(*) as count_num")
                    .groupBy("repair_type");
        
        List<Map<String, Object>> maps = getBaseMapper().selectMaps(queryWrapper);
        
        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> map : maps) {
            String type = (String) map.get("repair_type");
            // 使用正确的字段名"count_num"获取计数
            Long count = ((Number) map.get("count_num")).longValue(); // 适应不同数据库可能返回不同数字类型
            if (type != null && count != null) {
                result.put(type, count);
            }
        }
        return result;
    }
    
    /**
     * 获取按满意度分类的工单数量
     *
     * @return 满意度 -> 数量的映射
     */
    private Map<Integer, Long> getSatisfactionCounts() {
        // 使用MybatisPlus提供的方法正确获取分组计数
        QueryWrapper<RepairOrders> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("satisfaction_level, count(*) as count_num")
                    .isNotNull("satisfaction_level")
                    .groupBy("satisfaction_level");
        
        List<Map<String, Object>> maps = getBaseMapper().selectMaps(queryWrapper);
        
        Map<Integer, Long> result = new HashMap<>();
        for (Map<String, Object> map : maps) {
            Integer level = (Integer) map.get("satisfaction_level");
            // 使用正确的字段名"count_num"获取计数
            Long count = ((Number) map.get("count_num")).longValue(); // 适应不同数据库可能返回不同数字类型
            if (level != null && count != null) {
                result.put(level, count);
            }
        }
        return result;
    }
    
    /**
     * 计算平均满意度评分
     *
     * @param satisfactionCounts 满意度分布
     * @return 平均分
     */
    private Double calculateAvgSatisfaction(Map<Integer, Long> satisfactionCounts) {
        if (satisfactionCounts == null || satisfactionCounts.isEmpty()) {
            return 0.0;
        }
        
        long totalCount = 0;
        double totalScore = 0;
        
        for (Map.Entry<Integer, Long> entry : satisfactionCounts.entrySet()) {
            Integer level = entry.getKey();
            Long count = entry.getValue();
            
            // 添加null检查
            if (level != null && count != null) {
                totalCount += count;
                totalScore += level * count;
            }
        }
        
        return totalCount > 0 ? totalScore / totalCount : 0.0;
    }
    
    /**
     * 创建进度记录DTO
     *
     * @param orderId 工单ID
     * @param action 操作类型
     * @param description 操作描述
     * @return 进度记录DTO
     */
    private RepairProgressDTO createProgressDTO(Long orderId, String action, String description) {
        RepairProgressDTO dto = new RepairProgressDTO();
        dto.setOrderId(orderId);
        dto.setAction(action);
        dto.setDescription(description);
        return dto;
    }
    
    /**
     * 格式化日期为易读格式
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return date.toString();
    }
    
    /**
     * 生成工单编号
     * 
     * @return 工单编号
     */
    private String generateOrderNumber() {
        // 生成格式：年月日时分秒 + 4位随机数
        Calendar calendar = Calendar.getInstance();
        String dateStr = String.format("%04d%02d%02d%02d%02d%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
        
        // 生成4位随机数
        int random = new Random().nextInt(10000);
        String randomStr = String.format("%04d", random);
        
        return "R" + dateStr + randomStr;
    }
    
    /**
     * 将实体对象转换为VO
     * 
     * @param order 工单实体
     * @return 工单VO
     */
    private RepairOrderVO convertToVO(RepairOrders order) {
        if (order == null) {
            return null;
        }
        
        RepairOrderVO vo = new RepairOrderVO();
        BeanUtils.copyProperties(order, vo);
        
        // 设置状态描述
        vo.setStatusDesc(convertStatusToDesc(order.getStatus()));
        
        // 如果有维修工，查询维修工头像
        if (order.getWorkerId() != null) {
            RepairWorkers worker = repairWorkersMapper.selectById(order.getWorkerId());
            if (worker != null) {
                vo.setWorkerAvatar(worker.getAvatarUrl());
            }
        }
        
        // TODO: 获取地址详情（需要地址服务）
        
        return vo;
    }
    
    /**
     * 将实体列表转换为VO列表
     * 
     * @param orderList 工单实体列表
     * @return 工单VO列表
     */
    private List<RepairOrderVO> convertToVOList(List<RepairOrders> orderList) {
        List<RepairOrderVO> voList = new ArrayList<>();
        if (orderList != null && !orderList.isEmpty()) {
            for (RepairOrders order : orderList) {
                voList.add(convertToVO(order));
            }
        }
        return voList;
    }
    
    /**
     * 将实体对象转换为管理端VO
     * 
     * @param order 工单实体
     * @return 管理端工单VO
     */
    private AdminRepairOrderVO convertToAdminVO(RepairOrders order) {
        if (order == null) {
            return null;
        }
        
        AdminRepairOrderVO vo = new AdminRepairOrderVO();
        BeanUtils.copyProperties(order, vo);
        
        // 设置状态描述
        vo.setStatusDesc(convertStatusToDesc(order.getStatus()));
        
        // 获取用户信息
        if (order.getUserId() != null) {
            Users user = usersMapper.selectById(order.getUserId());
            if (user != null) {
                vo.setUserName(user.getUsername());
                vo.setUserNickname(user.getNickname());
            }
        }
        
        // 如果有维修工，查询维修工头像
        if (order.getWorkerId() != null) {
            RepairWorkers worker = repairWorkersMapper.selectById(order.getWorkerId());
            if (worker != null) {
                vo.setWorkerAvatar(worker.getAvatarUrl());
            }
        }
        
        // TODO: 获取地址详情（需要地址服务）
        
        return vo;
    }
    
    /**
     * 将实体列表转换为管理端VO列表
     * 
     * @param orderList 工单实体列表
     * @return 管理端工单VO列表
     */
    private List<AdminRepairOrderVO> convertToAdminVOList(List<RepairOrders> orderList) {
        List<AdminRepairOrderVO> voList = new ArrayList<>();
        if (orderList != null && !orderList.isEmpty()) {
            for (RepairOrders order : orderList) {
                voList.add(convertToAdminVO(order));
            }
        }
        return voList;
    }
    
    /**
     * 转换状态为描述文本
     * 
     * @param status 状态码
     * @return 状态描述
     */
    private String convertStatusToDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        
        switch (status) {
            case STATUS_PENDING:
                return "待受理";
            case STATUS_ASSIGNED:
                return "已分配";
            case STATUS_PROCESSING:
                return "处理中";
            case STATUS_COMPLETED:
                return "已完成";
            case STATUS_CANCELLED:
                return "已取消";
            default:
                return "未知";
        }
    }
}
