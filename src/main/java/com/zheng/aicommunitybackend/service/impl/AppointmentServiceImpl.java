package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.zheng.aicommunitybackend.domain.dto.AppointmentCreateDTO;
import com.zheng.aicommunitybackend.domain.dto.AppointmentOrderCreateDTO;
import com.zheng.aicommunitybackend.domain.dto.AppointmentPageQuery;
import com.zheng.aicommunitybackend.domain.dto.AppointmentRateDTO;
import com.zheng.aicommunitybackend.domain.dto.UserAppointServicesApproveDTO;
import com.zheng.aicommunitybackend.domain.entity.AppointmentOrders;
import com.zheng.aicommunitybackend.domain.entity.AppointmentServices;
import com.zheng.aicommunitybackend.domain.entity.AppointmentWorkers;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.AppointmentOrderVO;
import com.zheng.aicommunitybackend.domain.vo.AppointmentServiceVO;
import com.zheng.aicommunitybackend.domain.vo.AppointmentRecommendVO;
import com.zheng.aicommunitybackend.mapper.AppointmentOrdersMapper;
import com.zheng.aicommunitybackend.mapper.AppointmentServicesMapper;
import com.zheng.aicommunitybackend.mapper.AppointmentWorkersMapper;
import com.zheng.aicommunitybackend.service.AppointmentService;
import com.zheng.aicommunitybackend.util.ServiceTypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 预约服务实现类
 */
@Slf4j
@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentServicesMapper appointmentServicesMapper;

    @Autowired
    private AppointmentWorkersMapper appointmentWorkersMapper;

    @Autowired
    private AppointmentOrdersMapper appointmentOrdersMapper;
    
    @Autowired
    private com.zheng.aicommunitybackend.mapper.UsersMapper usersMapper;
    
    @Autowired
    private com.zheng.aicommunitybackend.service.UserAccountsService userAccountsService;

    // 服务类型渐变色配置
    private static final Map<String, String> SERVICE_GRADIENTS = new HashMap<>();
    private static final Map<String, String> SERVICE_COLORS = new HashMap<>();

    static {
        SERVICE_GRADIENTS.put("cleaning", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");
        SERVICE_GRADIENTS.put("repair", "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)");
        SERVICE_GRADIENTS.put("appliance", "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)");
        SERVICE_GRADIENTS.put("moving", "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)");
        SERVICE_GRADIENTS.put("gardening", "linear-gradient(135deg, #fa709a 0%, #fee140 100%)");
        SERVICE_GRADIENTS.put("pest", "linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)");

        SERVICE_COLORS.put("cleaning", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");
        SERVICE_COLORS.put("repair", "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)");
        SERVICE_COLORS.put("appliance", "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)");
        SERVICE_COLORS.put("moving", "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)");
    }

    @Override
    public List<AppointmentServiceVO> getAllServices() {
        LambdaQueryWrapper<AppointmentServices> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppointmentServices::getStatus, 1)
                .orderByAsc(AppointmentServices::getSortOrder);

        List<AppointmentServices> services = appointmentServicesMapper.selectList(wrapper);
        return services.stream().map(this::convertToServiceVO).collect(Collectors.toList());
    }

    @Override
    public List<AppointmentServiceVO> getHotServices() {
        LambdaQueryWrapper<AppointmentServices> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppointmentServices::getStatus, 1)
                .eq(AppointmentServices::getIsHot, 1)
                .orderByAsc(AppointmentServices::getSortOrder);

        List<AppointmentServices> services = appointmentServicesMapper.selectList(wrapper);
        return services.stream().map(this::convertToServiceVO).collect(Collectors.toList());
    }

    @Override
    public List<AppointmentRecommendVO> getRecommendServices(Long userId) {
        // 简单推荐逻辑：返回用户最近使用的服务类型和热门服务
        List<AppointmentRecommendVO> recommendList = new ArrayList<>();

        // 获取用户最近预约的服务类型
        LambdaQueryWrapper<AppointmentOrders> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(AppointmentOrders::getUserId, userId)
                .orderByDesc(AppointmentOrders::getCreateTime)
                .last("LIMIT 2");

        List<AppointmentOrders> recentOrders = appointmentOrdersMapper.selectList(orderWrapper);
        Set<String> recentServiceTypes = recentOrders.stream()
                .map(AppointmentOrders::getServiceType)
                .collect(Collectors.toSet());

        // 根据最近使用的服务类型推荐
        for (String serviceType : recentServiceTypes) {
            LambdaQueryWrapper<AppointmentServices> serviceWrapper = new LambdaQueryWrapper<>();
            serviceWrapper.eq(AppointmentServices::getServiceType, serviceType)
                    .eq(AppointmentServices::getStatus, 1);

            AppointmentServices service = appointmentServicesMapper.selectOne(serviceWrapper);
            if (service != null) {
                AppointmentRecommendVO vo = new AppointmentRecommendVO();
                vo.setType(service.getServiceType());
                vo.setName(service.getServiceName());
                vo.setIcon(service.getIcon());
                vo.setReason("您上次预约的服务，体验很棒");
                recommendList.add(vo);
            }
        }

        // 如果推荐列表不足，补充热门服务
        if (recommendList.size() < 2) {
            List<AppointmentServiceVO> hotServices = getHotServices();
            for (AppointmentServiceVO hotService : hotServices) {
                if (recommendList.size() >= 2) break;
                if (recommendList.stream().noneMatch(r -> r.getType().equals(hotService.getType()))) {
                    AppointmentRecommendVO vo = new AppointmentRecommendVO();
                    vo.setType(hotService.getType());
                    vo.setName(hotService.getName());
                    vo.setIcon(hotService.getIcon());
                    vo.setReason("热门服务，用户评价很高");
                    recommendList.add(vo);
                }
            }
        }

        return recommendList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAppointment(AppointmentCreateDTO dto, Long userId) {
        // 将中文服务类型转换为英文
        String convertedType = ServiceTypeConverter.convertToEnglish(dto.getServiceType());
        String serviceType = convertedType != null ? convertedType : dto.getServiceType();
        
        // 查询服务信息
        LambdaQueryWrapper<AppointmentServices> serviceWrapper = new LambdaQueryWrapper<>();
        serviceWrapper.eq(AppointmentServices::getServiceType, serviceType)
                .eq(AppointmentServices::getStatus, 1);

        AppointmentServices service = appointmentServicesMapper.selectOne(serviceWrapper);
        if (service == null) {
            throw new RuntimeException("服务类型不存在");
        }

        // 使用传入的预估价格，如果没有则使用服务基础价格
        BigDecimal estimatedPrice = dto.getEstimatedPrice() != null ? dto.getEstimatedPrice() : service.getBasePrice();
        
        // 检查余额是否充足
        if (!userAccountsService.checkBalance(userId, estimatedPrice)) {
            throw new RuntimeException("账户余额不足，请先充值");
        }

        // 生成订单编号
        String orderNo = generateOrderNo();

        // 创建预约订单
        AppointmentOrders order = new AppointmentOrders();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setServiceId(service.getId());
        order.setServiceType(service.getServiceType());
        order.setServiceName(service.getServiceName());
        order.setAppointmentTime(dto.getAppointmentTime());
        order.setAddress(dto.getAddress());
        order.setContactName(dto.getContactName());
        order.setContactPhone(dto.getContactPhone());
        order.setRequirements(dto.getRequirements());
        order.setEstimatedPrice(estimatedPrice);
        order.setStatus(0); // 待确认

        // 根据服务提供者查询worker信息
        if (service.getUserId() != null) {
            com.zheng.aicommunitybackend.domain.entity.Users serviceProvider = usersMapper.selectById(service.getUserId());
            if (serviceProvider != null) {
                order.setWorkerId(serviceProvider.getId());
                order.setWorkerName(serviceProvider.getNickname() != null ? serviceProvider.getNickname() : serviceProvider.getUsername());
                order.setWorkerPhone(serviceProvider.getPhone());
                log.info("自动分配服务人员：{}（ID：{}）", order.getWorkerName(), order.getWorkerId());
            }
        }

        // 如果指定了服务人员（覆盖自动分配）
        if (dto.getWorkerId() != null) {
            AppointmentWorkers worker = appointmentWorkersMapper.selectById(dto.getWorkerId());
            if (worker != null && worker.getStatus() == 1) {
                order.setWorkerId(worker.getId());
                order.setWorkerName(worker.getWorkerName());
                order.setWorkerPhone(worker.getWorkerPhone());
                log.info("手动指定服务人员：{}（ID：{}）", order.getWorkerName(), order.getWorkerId());
            }
        }

        // 先插入订单以获取订单ID
        appointmentOrdersMapper.insert(order);
        
        // 执行支付（扣除余额并记录流水）
        boolean paymentSuccess = userAccountsService.payForAppointment(userId, order.getId(), orderNo, estimatedPrice);
        
        if (!paymentSuccess) {
            throw new RuntimeException("支付失败，订单创建失败");
        }

        log.info("创建预约成功，订单号：{}，用户ID：{}，服务人员：{}，支付金额：{}", orderNo, userId, order.getWorkerName(), estimatedPrice);
        return orderNo;
    }

    @Override
    @Transactional
    public String createAppointmentOrder(AppointmentOrderCreateDTO dto, Long userId) {
        // 验证服务项不为空
        if (dto.getServiceItems() == null || dto.getServiceItems().isEmpty()) {
            throw new RuntimeException("服务项不能为空");
        }

        // 生成订单编号
        String orderNo = generateOrderNo();

        // 计算总价格
        BigDecimal totalEstimatedPrice = BigDecimal.ZERO;
        StringBuilder serviceNames = new StringBuilder();

        // 为每个服务项创建订单记录
        for (AppointmentOrderCreateDTO.ServiceItem item : dto.getServiceItems()) {
            // 查询服务信息
            AppointmentServices service = appointmentServicesMapper.selectById(item.getServiceId());
            if (service == null) {
                throw new RuntimeException("服务ID " + item.getServiceId() + " 不存在");
            }
            if (service.getStatus() != 1) {
                throw new RuntimeException("服务 " + service.getServiceName() + " 当前不可用");
            }

            // 计算服务价格
            BigDecimal servicePrice = service.getBasePrice().multiply(new BigDecimal(item.getQuantity()));
            totalEstimatedPrice = totalEstimatedPrice.add(servicePrice);

            // 拼接服务名称
            if (serviceNames.length() > 0) {
                serviceNames.append(", ");
            }
            serviceNames.append(service.getServiceName());
            if (item.getQuantity() > 1) {
                serviceNames.append(" x").append(item.getQuantity());
            }
        }

        // 处理特殊要求费用
        if (dto.getSpecialRequests() != null && !dto.getSpecialRequests().isEmpty()) {
            for (String request : dto.getSpecialRequests()) {
                if ("urgent".equals(request)) {
                    totalEstimatedPrice = totalEstimatedPrice.add(new BigDecimal("30"));
                } else if ("insurance".equals(request)) {
                    totalEstimatedPrice = totalEstimatedPrice.add(new BigDecimal("20"));
                }
            }
        }

        // 创建主订单记录（使用第一个服务的信息作为主要信息）
        AppointmentOrderCreateDTO.ServiceItem firstItem = dto.getServiceItems().get(0);
        AppointmentServices firstService = appointmentServicesMapper.selectById(firstItem.getServiceId());

        AppointmentOrders order = new AppointmentOrders();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setServiceId(firstService.getId());
        order.setServiceType(firstService.getServiceType());
        order.setServiceName(serviceNames.toString()); // 使用拼接后的服务名称
        order.setAppointmentTime(dto.getAppointmentTime());
        order.setAddress(dto.getAddress());
        order.setContactName(dto.getContactName());
        order.setContactPhone(dto.getContactPhone());
        
        // 拼接需求说明和特殊要求
        StringBuilder requirements = new StringBuilder();
        if (dto.getRequirements() != null && !dto.getRequirements().isEmpty()) {
            requirements.append(dto.getRequirements());
        }
        if (dto.getSpecialRequests() != null && !dto.getSpecialRequests().isEmpty()) {
            if (requirements.length() > 0) {
                requirements.append(" | ");
            }
            requirements.append("特殊要求：");
            for (int i = 0; i < dto.getSpecialRequests().size(); i++) {
                String request = dto.getSpecialRequests().get(i);
                if (i > 0) requirements.append("、");
                switch (request) {
                    case "tools" -> requirements.append("自带工具");
                    case "materials" -> requirements.append("自带材料");
                    case "urgent" -> requirements.append("加急服务");
                    case "insurance" -> requirements.append("需要保险");
                    default -> requirements.append(request);
                }
            }
        }
        order.setRequirements(requirements.toString());
        
        order.setEstimatedPrice(totalEstimatedPrice);
        order.setStatus(0); // 待确认

        appointmentOrdersMapper.insert(order);

        log.info("创建预约订单成功，订单号：{}，用户ID：{}，服务数量：{}，预估价格：{}", 
                orderNo, userId, dto.getServiceItems().size(), totalEstimatedPrice);
        return orderNo;
    }

    @Override
    public PageResult<AppointmentOrderVO> getUserAppointmentPage(AppointmentPageQuery query, Long userId) {
        Page<AppointmentOrderVO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<AppointmentOrderVO> result = appointmentOrdersMapper.selectUserAppointmentPage(page, query, userId);

        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    @Override
    public AppointmentOrderVO getAppointmentDetail(Long id, Long userId) {
        AppointmentOrderVO detail = appointmentOrdersMapper.selectAppointmentDetail(id, userId);
        if (detail == null) {
            throw new RuntimeException("预约记录不存在");
        }

        // 设置状态描述
        detail.setStatusDesc(getStatusDesc(detail.getStatus()));
        // 设置是否已评价
        detail.setRated(detail.getRating() != null && detail.getRating() > 0);

        return detail;
    }

    @Override
    @Transactional
    public Boolean cancelAppointment(Long id, Long userId, String reason) {
        // 查询预约订单
        LambdaQueryWrapper<AppointmentOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppointmentOrders::getId, id)
                .eq(AppointmentOrders::getUserId, userId);

        AppointmentOrders order = appointmentOrdersMapper.selectOne(wrapper);
        if (order == null) {
            throw new RuntimeException("预约记录不存在");
        }

        // 检查订单状态
        if (order.getStatus() == 4) {
            throw new RuntimeException("订单已取消");
        }
        if (order.getStatus() == 3) {
            throw new RuntimeException("订单已完成，无法取消");
        }

        // 更新订单状态
        order.setStatus(4);
        order.setCancelReason(reason);
        order.setCancelTime(new Date());

        int result = appointmentOrdersMapper.updateById(order);
        log.info("取消预约，订单ID：{}，用户ID：{}，原因：{}", id, userId, reason);

        return result > 0;
    }

    @Override
    @Transactional
    public Boolean rescheduleAppointment(Long id, Long userId, Date newAppointmentTime) {
        // 查询预约订单
        LambdaQueryWrapper<AppointmentOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppointmentOrders::getId, id)
                .eq(AppointmentOrders::getUserId, userId);

        AppointmentOrders order = appointmentOrdersMapper.selectOne(wrapper);
        if (order == null) {
            throw new RuntimeException("预约记录不存在");
        }

        // 检查订单状态
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new RuntimeException("当前状态不允许改期");
        }

        // 更新预约时间
        order.setAppointmentTime(newAppointmentTime);
        order.setStatus(0); // 重新设为待确认状态

        int result = appointmentOrdersMapper.updateById(order);
        log.info("改期预约，订单ID：{}，用户ID：{}，新时间：{}", id, userId, newAppointmentTime);

        return result > 0;
    }

    @Override
    @Transactional
    public Boolean rateAppointment(AppointmentRateDTO dto, Long userId) {
        // 查询预约订单
        LambdaQueryWrapper<AppointmentOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppointmentOrders::getId, dto.getAppointmentId())
                .eq(AppointmentOrders::getUserId, userId);

        AppointmentOrders order = appointmentOrdersMapper.selectOne(wrapper);
        if (order == null) {
            throw new RuntimeException("预约记录不存在");
        }

        // 检查订单状态
        if (order.getStatus() != 3) {
            throw new RuntimeException("只有已完成的订单才能评价");
        }

        // 检查是否已评价
        if (order.getRating() != null && order.getRating() > 0) {
            throw new RuntimeException("该订单已评价");
        }

        // 更新评价信息
        order.setRating(dto.getRating());
        order.setComment(dto.getComment());

        int result = appointmentOrdersMapper.updateById(order);
        log.info("评价预约，订单ID：{}，用户ID：{}，评分：{}", dto.getAppointmentId(), userId, dto.getRating());

        return result > 0;
    }

    @Override
    public List<AppointmentOrderVO> getRecentAppointments(Long userId, Integer limit) {
        List<AppointmentOrderVO> appointments = appointmentOrdersMapper.selectRecentAppointments(userId, limit);

        // 设置状态描述
        appointments.forEach(appointment -> {
            appointment.setStatusDesc(getStatusDesc(appointment.getStatus()));
            appointment.setRated(appointment.getRating() != null && appointment.getRating() > 0);
        });

        return appointments;
    }

    @Override
    public List<String> getAvailableTimeSlots(String serviceType, String date) {
        // 简单实现：返回固定的时间段
        List<String> timeSlots = Arrays.asList(
                "09:00-11:00",
                "11:00-13:00",
                "14:00-16:00",
                "16:00-18:00",
                "19:00-21:00"
        );

        // TODO: 实际应该查询数据库中的时间段配置和已预约情况
        return timeSlots;
    }

    @Override
    public List<Object> getServiceWorkers(String serviceType) {
        LambdaQueryWrapper<AppointmentWorkers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppointmentWorkers::getStatus, 1);
        
        // 将中文服务类型转换为英文
        if (StringUtils.hasText(serviceType)) {
            String convertedType = ServiceTypeConverter.convertToEnglish(serviceType);
            // 如果转换后不为null（即不是"全部"），则添加筛选条件
            if (convertedType != null) {
                wrapper.like(AppointmentWorkers::getServiceTypes, convertedType);
            }
        }
        
        wrapper.orderByDesc(AppointmentWorkers::getRating);

        List<AppointmentWorkers> workers = appointmentWorkersMapper.selectList(wrapper);

        return workers.stream().map(worker -> {
            Map<String, Object> workerInfo = new HashMap<>();
            workerInfo.put("id", worker.getId());
            workerInfo.put("name", worker.getWorkerName());
            workerInfo.put("phone", worker.getWorkerPhone());
            workerInfo.put("avatar", worker.getAvatarUrl());
            workerInfo.put("rating", worker.getRating());
            workerInfo.put("orderCount", worker.getOrderCount());
            workerInfo.put("experienceYears", worker.getExperienceYears());
            workerInfo.put("skillDescription", worker.getSkillDescription());
            return workerInfo;
        }).collect(Collectors.toList());
    }

    @Override
    public Object getAppointmentStats(Long userId) {
        // 查询用户预约统计
        LambdaQueryWrapper<AppointmentOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppointmentOrders::getUserId, userId);

        List<AppointmentOrders> allOrders = appointmentOrdersMapper.selectList(wrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", allOrders.size());
        stats.put("pendingCount", allOrders.stream().mapToInt(o -> o.getStatus() == 0 ? 1 : 0).sum());
        stats.put("confirmedCount", allOrders.stream().mapToInt(o -> o.getStatus() == 1 ? 1 : 0).sum());
        stats.put("completedCount", allOrders.stream().mapToInt(o -> o.getStatus() == 3 ? 1 : 0).sum());
        stats.put("cancelledCount", allOrders.stream().mapToInt(o -> o.getStatus() == 4 ? 1 : 0).sum());

        return stats;
    }

    @Override
    public void approveService(UserAppointServicesApproveDTO dto, Long userId) {
        AppointmentServices appointmentServices = new AppointmentServices();
        BeanUtils.copyProperties(dto, appointmentServices);
        appointmentServices.setCreateTime(new Date());
        appointmentServices.setUpdateTime(new Date());
        appointmentServices.setUserId(userId);
        appointmentServices.setApprovalStatus(0);
        appointmentServicesMapper.insert(appointmentServices);
    }

    // ==================== 管理员方法实现 ====================
    
    @Override
    public PageResult<AppointmentServiceVO> adminGetServicePage(com.zheng.aicommunitybackend.domain.dto.ServicePageQuery query) {
        Page<AppointmentServices> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<AppointmentServices> wrapper = new LambdaQueryWrapper<>();
        
        // 根据审核状态查询
        if (query.getApprovalStatus() != null) {
            wrapper.eq(AppointmentServices::getApprovalStatus, query.getApprovalStatus());
        }
        
        // 根据服务状态查询
        if (query.getStatus() != null) {
            wrapper.eq(AppointmentServices::getStatus, query.getStatus());
        }
        
        // 关键词搜索
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(AppointmentServices::getServiceName, query.getKeyword())
                           .or()
                           .like(AppointmentServices::getDescription, query.getKeyword()));
        }
        
        wrapper.orderByDesc(AppointmentServices::getCreateTime);
        
        IPage<AppointmentServices> result = appointmentServicesMapper.selectPage(page, wrapper);
        List<AppointmentServiceVO> voList = result.getRecords().stream()
                .map(this::convertToServiceVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(result.getTotal(), voList);
    }
    
    @Override
    public AppointmentServiceVO adminGetServiceDetail(Long id) {
        AppointmentServices service = appointmentServicesMapper.selectById(id);
        if (service == null) {
            throw new RuntimeException("服务不存在");
        }
        return convertToServiceVO(service);
    }
    
    @Override
    @Transactional
    public void adminApproveService(com.zheng.aicommunitybackend.domain.dto.ServiceApprovalDTO dto) {
        AppointmentServices service = appointmentServicesMapper.selectById(dto.getServiceId());
        if (service == null) {
            throw new RuntimeException("服务不存在");
        }
        
        if (dto.getApproved()) {
            // 审核通过
            service.setApprovalStatus(1);
            service.setStatus(1); // 自动启用
        } else {
            // 审核拒绝
            service.setApprovalStatus(2);
            service.setRejectReason(dto.getRejectReason());
        }
        service.setUpdateTime(new Date());
        appointmentServicesMapper.updateById(service);
    }
    
    @Override
    @Transactional
    public void adminUpdateServiceStatus(Long id, Integer status) {
        AppointmentServices service = appointmentServicesMapper.selectById(id);
        if (service == null) {
            throw new RuntimeException("服务不存在");
        }
        service.setStatus(status);
        service.setUpdateTime(new Date());
        appointmentServicesMapper.updateById(service);
    }
    
    @Override
    @Transactional
    public void adminSetHotService(Long id, Integer isHot) {
        AppointmentServices service = appointmentServicesMapper.selectById(id);
        if (service == null) {
            throw new RuntimeException("服务不存在");
        }
        service.setIsHot(isHot);
        service.setUpdateTime(new Date());
        appointmentServicesMapper.updateById(service);
    }
    
    @Override
    @Transactional
    public void adminDeleteService(Long id) {
        appointmentServicesMapper.deleteById(id);
    }
    
    @Override
    public Integer adminGetPendingServiceCount() {
        LambdaQueryWrapper<AppointmentServices> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppointmentServices::getApprovalStatus, 0);
        return Math.toIntExact(appointmentServicesMapper.selectCount(wrapper));
    }
    
    @Override
    public com.zheng.aicommunitybackend.domain.vo.ServiceStatisticsVO adminGetServiceStatistics(Long serviceId) {
        // 验证服务是否存在
        AppointmentServices service = appointmentServicesMapper.selectById(serviceId);
        if (service == null) {
            throw new RuntimeException("服务不存在");
        }
        
        // 查询该服务的所有订单
        LambdaQueryWrapper<AppointmentOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppointmentOrders::getServiceId, serviceId);
        List<AppointmentOrders> orders = appointmentOrdersMapper.selectList(wrapper);
        
        // 计算统计数据
        com.zheng.aicommunitybackend.domain.vo.ServiceStatisticsVO statistics = 
            new com.zheng.aicommunitybackend.domain.vo.ServiceStatisticsVO();
        
        statistics.setTotalOrders(orders.size());
        statistics.setCompletedOrders((int) orders.stream().filter(o -> o.getStatus() == 3).count());
        statistics.setPendingOrders((int) orders.stream().filter(o -> o.getStatus() == 0).count());
        statistics.setInProgressOrders((int) orders.stream().filter(o -> o.getStatus() == 2).count());
        statistics.setCancelledOrders((int) orders.stream().filter(o -> o.getStatus() == 4).count());
        
        // 计算总收入（只计算已完成的订单，使用actualPrice）
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus() == 3)
                .map(o -> o.getActualPrice() != null ? o.getActualPrice() : 
                         (o.getEstimatedPrice() != null ? o.getEstimatedPrice() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.setTotalRevenue(totalRevenue);
        
        // 计算平均评分（只计算已评分的订单）
        List<AppointmentOrders> ratedOrders = orders.stream()
                .filter(o -> o.getRating() != null && o.getRating() > 0)
                .collect(Collectors.toList());
        
        if (!ratedOrders.isEmpty()) {
            double avgRating = ratedOrders.stream()
                    .mapToInt(AppointmentOrders::getRating)
                    .average()
                    .orElse(0.0);
            statistics.setAverageRating(BigDecimal.valueOf(avgRating).setScale(1, java.math.RoundingMode.HALF_UP));
        } else {
            statistics.setAverageRating(BigDecimal.ZERO);
        }
        
        return statistics;
    }
    
    @Override
    public List<com.zheng.aicommunitybackend.domain.vo.ServiceRecentOrderVO> adminGetServiceRecentOrders(Long serviceId, Integer limit) {
        // 验证服务是否存在
        AppointmentServices service = appointmentServicesMapper.selectById(serviceId);
        if (service == null) {
            throw new RuntimeException("服务不存在");
        }
        
        // 查询最近的预约订单
        LambdaQueryWrapper<AppointmentOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppointmentOrders::getServiceId, serviceId);
        wrapper.orderByDesc(AppointmentOrders::getCreateTime);
        wrapper.last("LIMIT " + (limit != null && limit > 0 ? limit : 10));
        
        List<AppointmentOrders> orders = appointmentOrdersMapper.selectList(wrapper);
        
        // 转换为VO
        return orders.stream().map(order -> {
            com.zheng.aicommunitybackend.domain.vo.ServiceRecentOrderVO vo = 
                new com.zheng.aicommunitybackend.domain.vo.ServiceRecentOrderVO();
            vo.setId(order.getId());
            vo.setAppointmentTime(order.getAppointmentTime());
            vo.setStatus(order.getStatus());
            vo.setAddress(order.getAddress());
            vo.setContactPhone(order.getContactPhone());
            vo.setCreateTime(order.getCreateTime());
            
            // 查询用户昵称
            try {
                com.zheng.aicommunitybackend.domain.entity.Users user = 
                    usersMapper.selectById(order.getUserId());
                if (user != null) {
                    vo.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
                } else {
                    vo.setUsername("未知用户");
                }
            } catch (Exception e) {
                log.error("查询用户信息失败", e);
                vo.setUsername("未知用户");
            }
            
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public PageResult<AppointmentOrderVO> adminGetOrderPage(AppointmentPageQuery query) {
        Page<AppointmentOrders> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<AppointmentOrders> wrapper = new LambdaQueryWrapper<>();
        
        // 根据状态查询
        if (query.getStatus() != null) {
            wrapper.eq(AppointmentOrders::getStatus, query.getStatus());
        }
        
        // 根据服务类型查询
        if (StringUtils.hasText(query.getServiceType())) {
            wrapper.eq(AppointmentOrders::getServiceType, query.getServiceType());
        }
        
        wrapper.orderByDesc(AppointmentOrders::getCreateTime);
        
        IPage<AppointmentOrders> result = appointmentOrdersMapper.selectPage(page, wrapper);
        List<AppointmentOrderVO> voList = result.getRecords().stream()
                .map(this::convertToOrderVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(result.getTotal(), voList);
    }
    
    @Override
    public AppointmentOrderVO adminGetOrderDetail(Long id) {
        AppointmentOrders order = appointmentOrdersMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return convertToOrderVO(order);
    }
    
    @Override
    @Transactional
    public void adminConfirmOrder(Long id) {
        AppointmentOrders order = appointmentOrdersMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setStatus(1); // 已确认
        order.setUpdateTime(new Date());
        appointmentOrdersMapper.updateById(order);
    }
    
    @Override
    @Transactional
    public void adminAssignWorker(com.zheng.aicommunitybackend.domain.dto.WorkerAssignDTO dto) {
        AppointmentOrders order = appointmentOrdersMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setWorkerId(dto.getWorkerId());
        order.setWorkerName(dto.getWorkerName());
        order.setUpdateTime(new Date());
        appointmentOrdersMapper.updateById(order);
    }
    
    @Override
    @Transactional
    public void adminUpdateOrderStatus(com.zheng.aicommunitybackend.domain.dto.OrderStatusUpdateDTO dto) {
        AppointmentOrders order = appointmentOrdersMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setStatus(dto.getStatus());
        // 备注字段不存在，改为写入requirements
        if (StringUtils.hasText(dto.getRemark())) {
            order.setRequirements(dto.getRemark());
        }
        order.setUpdateTime(new Date());
        appointmentOrdersMapper.updateById(order);
    }
    
    @Override
    @Transactional
    public void adminCancelOrder(Long id, String reason) {
        AppointmentOrders order = appointmentOrdersMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setStatus(4); // 已取消
        order.setCancelReason(reason);
        order.setUpdateTime(new Date());
        appointmentOrdersMapper.updateById(order);
    }
    
    @Override
    @Transactional
    public void adminDeleteOrder(Long id) {
        appointmentOrdersMapper.deleteById(id);
    }
    
    @Override
    public Map<String, Object> adminGetOrderStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取所有订单
        List<AppointmentOrders> allOrders = appointmentOrdersMapper.selectList(null);
        
        // 统计各状态数量
        stats.put("totalOrders", allOrders.size());
        stats.put("pendingCount", allOrders.stream().filter(o -> o.getStatus() == 0).count());
        stats.put("confirmedCount", allOrders.stream().filter(o -> o.getStatus() == 1).count());
        stats.put("inProgressCount", allOrders.stream().filter(o -> o.getStatus() == 2).count());
        stats.put("completedCount", allOrders.stream().filter(o -> o.getStatus() == 3).count());
        stats.put("cancelledCount", allOrders.stream().filter(o -> o.getStatus() == 4).count());
        
        // 今日订单数 - 使用LocalDate比较
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayCount = allOrders.stream()
                .filter(o -> {
                    if (o.getCreateTime() == null) return false;
                    LocalDateTime createTime = o.getCreateTime().toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime();
                    return !createTime.isBefore(today);
                })
                .count();
        stats.put("todayOrders", todayCount);
        
        return stats;
    }
    
    // ==================== 商家接口实现 ====================
    
    @Override
    public PageResult<AppointmentOrderVO> getMerchantOrderPage(AppointmentPageQuery query, Long userId) {
        // 查询该用户提供的所有服务ID列表
        LambdaQueryWrapper<AppointmentServices> serviceWrapper = new LambdaQueryWrapper<>();
        serviceWrapper.eq(AppointmentServices::getUserId, userId);
        List<AppointmentServices> services = appointmentServicesMapper.selectList(serviceWrapper);
        
        if (services.isEmpty()) {
            // 如果用户没有提供任何服务，返回空结果
            return new PageResult<>(0L, new ArrayList<>());
        }
        
        List<Long> serviceIds = services.stream()
                .map(AppointmentServices::getId)
                .collect(Collectors.toList());
        
        // 构建查询条件
        Page<AppointmentOrders> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<AppointmentOrders> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询该商家服务的订单
        wrapper.in(AppointmentOrders::getServiceId, serviceIds);
        
        // 状态筛选
        if (query.getStatus() != null) {
            wrapper.eq(AppointmentOrders::getStatus, query.getStatus());
        }
        
        // 服务类型筛选
        if (StringUtils.hasText(query.getServiceType())) {
            wrapper.eq(AppointmentOrders::getServiceType, query.getServiceType());
        }
        
        // 关键词搜索
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(AppointmentOrders::getOrderNo, query.getKeyword())
                    .or().like(AppointmentOrders::getServiceName, query.getKeyword())
                    .or().like(AppointmentOrders::getContactName, query.getKeyword())
            );
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(AppointmentOrders::getCreateTime);
        
        IPage<AppointmentOrders> orderPage = appointmentOrdersMapper.selectPage(page, wrapper);
        
        // 转换为VO
        List<AppointmentOrderVO> voList = orderPage.getRecords().stream()
                .map(this::convertToOrderVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(orderPage.getTotal(), voList);
    }
    
    @Override
    public AppointmentOrderVO getMerchantOrderDetail(Long orderId, Long userId) {
        AppointmentOrders order = appointmentOrdersMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 验证该订单是否属于该商家的服务
        AppointmentServices service = appointmentServicesMapper.selectById(order.getServiceId());
        if (service == null || !service.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看此订单");
        }
        
        return convertToOrderVO(order);
    }
    
    @Override
    public Map<String, Object> getMerchantOrderStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 查询该用户提供的所有服务ID列表
        LambdaQueryWrapper<AppointmentServices> serviceWrapper = new LambdaQueryWrapper<>();
        serviceWrapper.eq(AppointmentServices::getUserId, userId);
        List<AppointmentServices> services = appointmentServicesMapper.selectList(serviceWrapper);
        
        if (services.isEmpty()) {
            // 如果用户没有提供任何服务，返回0统计
            stats.put("total", 0);
            stats.put("pending", 0);
            stats.put("processing", 0);
            stats.put("completed", 0);
            return stats;
        }
        
        List<Long> serviceIds = services.stream()
                .map(AppointmentServices::getId)
                .collect(Collectors.toList());
        
        // 查询该商家的所有订单
        LambdaQueryWrapper<AppointmentOrders> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AppointmentOrders::getServiceId, serviceIds);
        List<AppointmentOrders> orders = appointmentOrdersMapper.selectList(wrapper);
        
        // 统计各状态数量
        stats.put("total", orders.size());
        stats.put("pending", orders.stream().filter(o -> o.getStatus() == 0).count());
        stats.put("processing", orders.stream().filter(o -> o.getStatus() == 1 || o.getStatus() == 2).count());
        stats.put("completed", orders.stream().filter(o -> o.getStatus() == 3).count());
        
        return stats;
    }
    
    @Override
    @Transactional
    public Boolean merchantConfirmOrder(Long orderId, Long userId) {
        AppointmentOrders order = appointmentOrdersMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 验证该订单是否属于该商家的服务
        AppointmentServices service = appointmentServicesMapper.selectById(order.getServiceId());
        if (service == null || !service.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不正确，无法确认");
        }
        
        order.setStatus(1); // 已确认
        order.setConfirmTime(new Date());
        order.setUpdateTime(new Date());
        
        return appointmentOrdersMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public Boolean merchantStartService(Long orderId, Long userId) {
        AppointmentOrders order = appointmentOrdersMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 验证该订单是否属于该商家的服务
        AppointmentServices service = appointmentServicesMapper.selectById(order.getServiceId());
        if (service == null || !service.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        
        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态不正确，无法开始服务");
        }
        
        order.setStatus(2); // 服务中
        order.setStartTime(new Date());
        order.setUpdateTime(new Date());
        
        return appointmentOrdersMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public Boolean merchantFinishService(Long orderId, Long userId) {
        AppointmentOrders order = appointmentOrdersMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 验证该订单是否属于该商家的服务
        AppointmentServices service = appointmentServicesMapper.selectById(order.getServiceId());
        if (service == null || !service.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        
        if (order.getStatus() != 2) {
            throw new RuntimeException("订单状态不正确，无法完成服务");
        }
        
        order.setStatus(3); // 已完成
        order.setFinishTime(new Date());
        order.setUpdateTime(new Date());
        // 实际价格默认等于预估价格
        if (order.getActualPrice() == null) {
            order.setActualPrice(order.getEstimatedPrice());
        }
        
        return appointmentOrdersMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public Boolean merchantRejectOrder(Long orderId, Long userId, String reason) {
        AppointmentOrders order = appointmentOrdersMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 验证该订单是否属于该商家的服务
        AppointmentServices service = appointmentServicesMapper.selectById(order.getServiceId());
        if (service == null || !service.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不正确，无法拒绝");
        }
        
        order.setStatus(4); // 已取消
        order.setCancelTime(new Date());
        order.setCancelReason(reason != null ? reason : "商家拒绝接单");
        order.setUpdateTime(new Date());
        
        return appointmentOrdersMapper.updateById(order) > 0;
    }
    
    /**
     * 转换为订单VO
     */
    private AppointmentOrderVO convertToOrderVO(AppointmentOrders order) {
        AppointmentOrderVO vo = new AppointmentOrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setStatusDesc(getStatusDesc(order.getStatus()));
        
        // 设置用户信息
        if (order.getUserId() != null) {
            try {
                com.zheng.aicommunitybackend.domain.entity.Users user = usersMapper.selectById(order.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername() != null ? user.getUsername() : user.getNickname());
                    // 脱敏手机号
                    if (user.getPhone() != null && user.getPhone().length() >= 11) {
                        vo.setUserPhone(user.getPhone().substring(0, 3) + "****" + user.getPhone().substring(7));
                    } else {
                        vo.setUserPhone(user.getPhone());
                    }
                }
            } catch (Exception e) {
                log.error("查询用户信息失败 userId: {}", order.getUserId(), e);
                vo.setUsername("未知用户");
            }
        }
        
        // 设置服务人员信息
        if (order.getWorkerId() != null) {
            AppointmentOrderVO.WorkerInfo workerInfo = new AppointmentOrderVO.WorkerInfo();
            workerInfo.setName(order.getWorkerName());
            workerInfo.setPhone(order.getWorkerPhone());
            vo.setWorker(workerInfo);
        }
        vo.setRated(order.getRating() != null && order.getRating() > 0);
        return vo;
    }

    /**
     * 转换为服务VO
     */
    private AppointmentServiceVO convertToServiceVO(AppointmentServices service) {
        AppointmentServiceVO vo = new AppointmentServiceVO();
        
        // 基础信息
        vo.setId(service.getId());
        vo.setType(service.getServiceType());
        vo.setName(service.getServiceName());
        vo.setServiceName(service.getServiceName());
        vo.setServiceType(service.getServiceType());
        vo.setDescription(service.getDescription());
        vo.setIcon(service.getIcon());
        
        // 价格信息
        vo.setBasePrice(service.getBasePrice());
        vo.setPrice(service.getBasePrice() != null ? service.getBasePrice().toString() : "0");
        vo.setUnit(service.getUnit());
        
        // 时长信息 - 实体没有duration字段，设置默认值或从配置获取
        vo.setDuration(60); // 默认60分钟
        
        // 状态信息
        vo.setStatus(service.getStatus());
        vo.setIsActive(service.getStatus() != null && service.getStatus() == 1);
        vo.setApprovalStatus(service.getApprovalStatus());
        vo.setIsHot(service.getIsHot() != null && service.getIsHot() == 1);
        
        // 时间信息 - 转换Date为LocalDateTime
        if (service.getCreateTime() != null) {
            vo.setCreatedAt(new java.sql.Timestamp(service.getCreateTime().getTime()).toLocalDateTime());
        }
        if (service.getUpdateTime() != null) {
            vo.setUpdatedAt(new java.sql.Timestamp(service.getUpdateTime().getTime()).toLocalDateTime());
        }
        vo.setCreatedBy(service.getUserId());
        
        // 前端展示配置
        vo.setRating("4.8"); // 默认评分，实际应该从统计数据获取
        vo.setGradient(SERVICE_GRADIENTS.get(service.getServiceType()));
        vo.setColor(SERVICE_COLORS.get(service.getServiceType()));
        
        return vo;
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        return "APT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        switch (status) {
            case 0: return "待确认";
            case 1: return "已确认";
            case 2: return "服务中";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知状态";
        }
    }
}
