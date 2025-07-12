package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.dto.RepairWorkerDTO;
import com.zheng.aicommunitybackend.domain.dto.RepairWorkerPageQuery;
import com.zheng.aicommunitybackend.domain.dto.RepairWorkerStatusDTO;
import com.zheng.aicommunitybackend.domain.entity.RepairOrders;
import com.zheng.aicommunitybackend.domain.entity.RepairWorkers;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.RepairOrderVO;
import com.zheng.aicommunitybackend.domain.vo.RepairWorkerVO;
import com.zheng.aicommunitybackend.domain.vo.WorkerStatsVO;
import com.zheng.aicommunitybackend.exception.BaseException;
import com.zheng.aicommunitybackend.mapper.RepairOrdersMapper;
import com.zheng.aicommunitybackend.mapper.RepairWorkersMapper;
import com.zheng.aicommunitybackend.service.RepairWorkersService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author ZhengJJ
* @description 针对表【repair_workers(维修工信息表)】的数据库操作Service实现
* @createDate 2025-07-10 11:03:49
*/
@Service
public class RepairWorkersServiceImpl extends ServiceImpl<RepairWorkersMapper, RepairWorkers>
    implements RepairWorkersService {

    @Autowired
    private RepairOrdersMapper repairOrdersMapper;

    /**
     * 工作状态常量
     */
    private static final int STATUS_REST = 0; // 休息
    private static final int STATUS_AVAILABLE = 1; // 可接单
    private static final int STATUS_BUSY = 2; // 忙碌

    @Override
    public PageResult<RepairWorkerVO> pageWorkers(RepairWorkerPageQuery query) {
        // 1. 构建查询条件
        LambdaQueryWrapper<RepairWorkers> wrapper = new LambdaQueryWrapper<>();
        
        // 2. 添加筛选条件
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(RepairWorkers::getName, query.getName());
        }
        
        if (StringUtils.hasText(query.getPhone())) {
            wrapper.like(RepairWorkers::getPhone, query.getPhone());
        }
        
        if (StringUtils.hasText(query.getServiceType())) {
            wrapper.like(RepairWorkers::getServiceType, query.getServiceType());
        }
        
        if (query.getWorkStatus() != null) {
            wrapper.eq(RepairWorkers::getWorkStatus, query.getWorkStatus());
        }
        
        if (query.getMinRating() != null) {
            wrapper.ge(RepairWorkers::getRating, BigDecimal.valueOf(query.getMinRating()));
        }
        
        if (query.getMaxRating() != null) {
            wrapper.le(RepairWorkers::getRating, BigDecimal.valueOf(query.getMaxRating()));
        }
        
        if (query.getStartTime() != null && query.getEndTime() != null) {
            wrapper.between(RepairWorkers::getCreateTime, query.getStartTime(), query.getEndTime());
        } else if (query.getStartTime() != null) {
            wrapper.ge(RepairWorkers::getCreateTime, query.getStartTime());
        } else if (query.getEndTime() != null) {
            wrapper.le(RepairWorkers::getCreateTime, query.getEndTime());
        }
        
        // 3. 排序
        String sortField = query.getSortField();
        String sortOrder = query.getSortOrder();
        if (StringUtils.hasText(sortField)) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortField) {
                case "createTime":
                    wrapper.orderBy(true, isAsc, RepairWorkers::getCreateTime);
                    break;
                case "rating":
                    wrapper.orderBy(true, isAsc, RepairWorkers::getRating);
                    break;
                case "serviceCount":
                    wrapper.orderBy(true, isAsc, RepairWorkers::getServiceCount);
                    break;
                default:
                    wrapper.orderByDesc(RepairWorkers::getCreateTime);
            }
        } else {
            // 默认按创建时间倒序
            wrapper.orderByDesc(RepairWorkers::getCreateTime);
        }
        
        // 4. 执行分页查询
        Page<RepairWorkers> page = page(
                new Page<>(query.getPage(), query.getPageSize()),
                wrapper
        );
        
        // 5. 转换为VO列表
        List<RepairWorkerVO> voList = convertToVOList(page.getRecords());
        
        // 6. 返回分页结果
        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public RepairWorkerVO getWorkerById(Long workerId) {
        // 1. 查询维修工
        RepairWorkers worker = getById(workerId);
        if (worker == null) {
            throw new BaseException("维修工不存在");
        }
        
        // 2. 转换为VO并返回
        return convertToVO(worker);
    }

    @Override
    public List<RepairWorkerVO> getAvailableWorkersByType(String serviceType) {
        // 1. 构建查询条件
        LambdaQueryWrapper<RepairWorkers> wrapper = new LambdaQueryWrapper<>();
        
        // 2. 查询可接单的维修工
        wrapper.eq(RepairWorkers::getWorkStatus, STATUS_AVAILABLE);
        
        // 3. 如果指定了服务类型，则筛选对应类型的维修工
        if (StringUtils.hasText(serviceType)) {
            wrapper.like(RepairWorkers::getServiceType, serviceType);
        }
        
        // 4. 按评分降序排序
        wrapper.orderByDesc(RepairWorkers::getRating);
        
        // 5. 执行查询
        List<RepairWorkers> workers = list(wrapper);
        
        // 6. 转换为VO列表并返回
        return convertToVOList(workers);
    }

    @Override
    @Transactional
    public Long addWorker(RepairWorkerDTO dto) {
        // 1. 检查手机号是否已存在
        LambdaQueryWrapper<RepairWorkers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairWorkers::getPhone, dto.getPhone());
        if (count(wrapper) > 0) {
            throw new BaseException("该手机号已被注册");
        }
        
        // 2. 检查身份证号是否已存在
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairWorkers::getIdCardNumber, dto.getIdCardNumber());
        if (count(wrapper) > 0) {
            throw new BaseException("该身份证号已被注册");
        }
        
        // 3. 构建维修工对象
        RepairWorkers worker = new RepairWorkers();
        BeanUtils.copyProperties(dto, worker);
        
        // 4. 设置初始属性
        worker.setWorkStatus(STATUS_REST); // 默认为休息状态
        worker.setRating(new BigDecimal("5.0")); // 初始评分为5分
        worker.setServiceCount(0); // 初始服务次数为0
        worker.setCreateTime(new Date());
        
        // 5. 保存维修工信息
        save(worker);
        
        return worker.getId();
    }

    @Override
    @Transactional
    public boolean updateWorker(RepairWorkerDTO dto) {
        // 1. 检查维修工是否存在
        RepairWorkers existingWorker = getById(dto.getId());
        if (existingWorker == null) {
            throw new BaseException("维修工不存在");
        }
        
        // 2. 如果修改了手机号，检查是否与其他维修工冲突
        if (!existingWorker.getPhone().equals(dto.getPhone())) {
            LambdaQueryWrapper<RepairWorkers> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RepairWorkers::getPhone, dto.getPhone())
                   .ne(RepairWorkers::getId, dto.getId());
            if (count(wrapper) > 0) {
                throw new BaseException("该手机号已被其他维修工使用");
            }
        }
        
        // 3. 如果修改了身份证号，检查是否与其他维修工冲突
        if (!existingWorker.getIdCardNumber().equals(dto.getIdCardNumber())) {
            LambdaQueryWrapper<RepairWorkers> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RepairWorkers::getIdCardNumber, dto.getIdCardNumber())
                   .ne(RepairWorkers::getId, dto.getId());
            if (count(wrapper) > 0) {
                throw new BaseException("该身份证号已被其他维修工使用");
            }
        }
        
        // 4. 更新维修工信息
        RepairWorkers worker = new RepairWorkers();
        BeanUtils.copyProperties(dto, worker);
        
        // 5. 保留原有的不可修改字段
        worker.setRating(existingWorker.getRating());
        worker.setServiceCount(existingWorker.getServiceCount());
        worker.setCreateTime(existingWorker.getCreateTime());
        
        // 6. 执行更新
        return updateById(worker);
    }

    @Override
    @Transactional
    public boolean deleteWorker(Long workerId) {
        // 1. 检查维修工是否存在
        RepairWorkers worker = getById(workerId);
        if (worker == null) {
            throw new BaseException("维修工不存在");
        }
        
        // 2. 检查维修工是否有未完成的工单
        LambdaQueryWrapper<RepairOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrders::getWorkerId, workerId)
               .in(RepairOrders::getStatus, Arrays.asList(1, 2)); // 已分配或处理中的工单
        if (repairOrdersMapper.selectCount(wrapper) > 0) {
            throw new BaseException("该维修工有未完成的工单，无法删除");
        }
        
        // 3. 执行删除
        return removeById(workerId);
    }

    @Override
    @Transactional
    public boolean updateWorkerStatus(RepairWorkerStatusDTO dto) {
        // 1. 检查维修工是否存在
        RepairWorkers worker = getById(dto.getWorkerId());
        if (worker == null) {
            throw new BaseException("维修工不存在");
        }
        
        // 2. 如果要设置为休息状态，检查是否有未完成的工单
        if (dto.getWorkStatus() == STATUS_REST) {
            LambdaQueryWrapper<RepairOrders> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RepairOrders::getWorkerId, dto.getWorkerId())
                   .in(RepairOrders::getStatus, Arrays.asList(1, 2)); // 已分配或处理中的工单
            if (repairOrdersMapper.selectCount(wrapper) > 0) {
                throw new BaseException("该维修工有未完成的工单，无法设置为休息状态");
            }
        }
        
        // 3. 更新工作状态
        worker.setWorkStatus(dto.getWorkStatus());
        
        // 4. 执行更新
        return updateById(worker);
    }

    @Override
    public WorkerStatsVO getWorkerStats(Long workerId) {
        // 1. 检查维修工是否存在
        RepairWorkers worker = getById(workerId);
        if (worker == null) {
            throw new BaseException("维修工不存在");
        }
        
        // 2. 构建统计VO
        WorkerStatsVO stats = new WorkerStatsVO();
        stats.setWorkerId(worker.getId());
        stats.setWorkerName(worker.getName());
        stats.setTotalServiceCount(worker.getServiceCount());
        stats.setAverageRating(worker.getRating());
        
        // 3. 查询本月服务次数
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date monthStart = calendar.getTime();
        
        LambdaQueryWrapper<RepairOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrders::getWorkerId, workerId)
               .eq(RepairOrders::getStatus, 3) // 已完成的工单
               .ge(RepairOrders::getUpdateTime, monthStart);
        stats.setMonthlyServiceCount(repairOrdersMapper.selectCount(wrapper).intValue());
        
        // 4. 获取评分统计
        stats.setRatingCounts(getRatingCounts(workerId));
        
        // 5. 获取工单状态统计
        stats.setStatusCounts(getStatusCounts(workerId));
        
        // 6. 获取服务类型统计
        stats.setServiceTypeCounts(getServiceTypeCounts(workerId));
        
        // 7. 获取月度统计数据
        stats.setMonthlyStats(getMonthlyStats(workerId));
        
        return stats;
    }

    @Override
    public List<RepairOrderVO> getWorkerOrders(Long workerId, Integer status) {
        // 1. 检查维修工是否存在
        RepairWorkers worker = getById(workerId);
        if (worker == null) {
            throw new BaseException("维修工不存在");
        }
        
        // 2. 构建查询条件
        LambdaQueryWrapper<RepairOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrders::getWorkerId, workerId);
        
        // 3. 如果指定了状态，则添加状态筛选
        if (status != null) {
            wrapper.eq(RepairOrders::getStatus, status);
        }
        
        // 4. 按更新时间倒序排序
        wrapper.orderByDesc(RepairOrders::getUpdateTime);
        
        // 5. 执行查询
        List<RepairOrders> orders = repairOrdersMapper.selectList(wrapper);
        
        // 6. 转换为VO列表
        return orders.stream()
                .map(this::convertOrderToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateWorkerRating(Long workerId) {
        // 1. 检查维修工是否存在
        RepairWorkers worker = getById(workerId);
        if (worker == null) {
            throw new BaseException("维修工不存在");
        }
        
        // 2. 查询该维修工的所有已完成且有评分的工单
        LambdaQueryWrapper<RepairOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrders::getWorkerId, workerId)
               .eq(RepairOrders::getStatus, 3) // 已完成的工单
               .isNotNull(RepairOrders::getSatisfactionLevel); // 有评分的工单
        
        List<RepairOrders> orders = repairOrdersMapper.selectList(wrapper);
        
        // 3. 如果没有评价，则保持默认评分
        if (orders.isEmpty()) {
            return;
        }
        
        // 4. 计算平均评分
        double totalRating = 0;
        for (RepairOrders order : orders) {
            totalRating += order.getSatisfactionLevel();
        }
        BigDecimal avgRating = new BigDecimal(totalRating / orders.size())
                .setScale(1, RoundingMode.HALF_UP);
        
        // 5. 更新维修工评分
        worker.setRating(avgRating);
        worker.setServiceCount(orders.size());
        updateById(worker);
    }
    
    /**
     * 获取维修工各评分统计
     * @param workerId 维修工ID
     * @return 评分统计
     */
    private Map<Integer, Long> getRatingCounts(Long workerId) {
        Map<Integer, Long> ratingCounts = new HashMap<>();
        
        // 初始化评分统计
        for (int i = 1; i <= 5; i++) {
            ratingCounts.put(i, 0L);
        }
        
        // 查询已完成且有评分的工单
        LambdaQueryWrapper<RepairOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrders::getWorkerId, workerId)
               .eq(RepairOrders::getStatus, 3) // 已完成的工单
               .isNotNull(RepairOrders::getSatisfactionLevel); // 有评分的工单
        
        List<RepairOrders> orders = repairOrdersMapper.selectList(wrapper);
        
        // 统计各评分的数量
        for (RepairOrders order : orders) {
            Integer rating = order.getSatisfactionLevel();
            if (rating != null && rating >= 1 && rating <= 5) {
                ratingCounts.put(rating, ratingCounts.getOrDefault(rating, 0L) + 1);
            }
        }
        
        return ratingCounts;
    }
    
    /**
     * 获取维修工工单状态统计
     * @param workerId 维修工ID
     * @return 状态统计
     */
    private Map<Integer, Long> getStatusCounts(Long workerId) {
        Map<Integer, Long> statusCounts = new HashMap<>();
        
        // 查询该维修工的所有工单
        LambdaQueryWrapper<RepairOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrders::getWorkerId, workerId);
        
        List<RepairOrders> orders = repairOrdersMapper.selectList(wrapper);
        
        // 统计各状态的工单数量
        for (RepairOrders order : orders) {
            Integer status = order.getStatus();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
        }
        
        return statusCounts;
    }
    
    /**
     * 获取维修工服务类型统计
     * @param workerId 维修工ID
     * @return 服务类型统计
     */
    private Map<String, Long> getServiceTypeCounts(Long workerId) {
        Map<String, Long> typeCounts = new HashMap<>();
        
        // 查询该维修工的已完成工单
        LambdaQueryWrapper<RepairOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrders::getWorkerId, workerId)
               .eq(RepairOrders::getStatus, 3); // 已完成的工单
        
        List<RepairOrders> orders = repairOrdersMapper.selectList(wrapper);
        
        // 统计各服务类型的工单数量
        for (RepairOrders order : orders) {
            String type = order.getRepairType();
            if (StringUtils.hasText(type)) {
                typeCounts.put(type, typeCounts.getOrDefault(type, 0L) + 1);
            }
        }
        
        return typeCounts;
    }
    
    /**
     * 获取维修工月度服务统计（近6个月）
     * @param workerId 维修工ID
     * @return 月度统计
     */
    private Map<String, Long> getMonthlyStats(Long workerId) {
        Map<String, Long> monthlyStats = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        // 初始化近6个月的统计数据
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            monthlyStats.put(month.format(formatter), 0L);
        }
        
        // 查询该维修工的已完成工单
        LambdaQueryWrapper<RepairOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrders::getWorkerId, workerId)
               .eq(RepairOrders::getStatus, 3) // 已完成的工单
               .ge(RepairOrders::getUpdateTime, Date.from(now.minusMonths(6).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        
        List<RepairOrders> orders = repairOrdersMapper.selectList(wrapper);
        
        // 统计各月份的工单数量
        for (RepairOrders order : orders) {
            LocalDate orderDate = order.getUpdateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String monthKey = orderDate.format(formatter);
            if (monthlyStats.containsKey(monthKey)) {
                monthlyStats.put(monthKey, monthlyStats.get(monthKey) + 1);
            }
        }
        
        return monthlyStats;
    }
    
    /**
     * 将维修工实体转换为VO
     * @param worker 维修工实体
     * @return 维修工VO
     */
    private RepairWorkerVO convertToVO(RepairWorkers worker) {
        if (worker == null) {
            return null;
        }
        
        RepairWorkerVO vo = new RepairWorkerVO();
        BeanUtils.copyProperties(worker, vo);
        
        // 设置服务类型列表
        if (StringUtils.hasText(worker.getServiceType())) {
            vo.setServiceTypeList(worker.getServiceType().split(","));
        }
        
        // 设置工作状态描述
        vo.setWorkStatusDesc(convertWorkStatusToDesc(worker.getWorkStatus()));
        
        return vo;
    }
    
    /**
     * 将维修工列表转换为VO列表
     * @param workers 维修工列表
     * @return VO列表
     */
    private List<RepairWorkerVO> convertToVOList(List<RepairWorkers> workers) {
        if (workers == null || workers.isEmpty()) {
            return new ArrayList<>();
        }
        
        return workers.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将工单实体转换为VO
     * @param order 工单实体
     * @return 工单VO
     */
    private RepairOrderVO convertOrderToVO(RepairOrders order) {
        if (order == null) {
            return null;
        }
        
        // 这里只是简单转换，实际项目中应该使用RepairOrdersService中的转换方法
        RepairOrderVO vo = new RepairOrderVO();
        BeanUtils.copyProperties(order, vo);
        
        // 设置状态描述
        vo.setStatusDesc(convertOrderStatusToDesc(order.getStatus()));
        
        return vo;
    }
    
    /**
     * 转换工作状态为描述文本
     * @param status 状态码
     * @return 状态描述
     */
    private String convertWorkStatusToDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        
        switch (status) {
            case STATUS_REST:
                return "休息";
            case STATUS_AVAILABLE:
                return "可接单";
            case STATUS_BUSY:
                return "忙碌";
            default:
                return "未知";
        }
    }
    
    /**
     * 转换工单状态为描述文本
     * @param status 状态码
     * @return 状态描述
     */
    private String convertOrderStatusToDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        
        switch (status) {
            case 0:
                return "待受理";
            case 1:
                return "已分配";
            case 2:
                return "处理中";
            case 3:
                return "已完成";
            case 4:
                return "已取消";
            default:
                return "未知";
        }
    }
}




