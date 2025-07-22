package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.zheng.aicommunitybackend.domain.dto.AppointmentCreateDTO;
import com.zheng.aicommunitybackend.domain.dto.AppointmentPageQuery;
import com.zheng.aicommunitybackend.domain.dto.AppointmentRateDTO;
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
    @Transactional
    public String createAppointment(AppointmentCreateDTO dto, Long userId) {
        // 查询服务信息
        LambdaQueryWrapper<AppointmentServices> serviceWrapper = new LambdaQueryWrapper<>();
        serviceWrapper.eq(AppointmentServices::getServiceType, dto.getServiceType())
                .eq(AppointmentServices::getStatus, 1);

        AppointmentServices service = appointmentServicesMapper.selectOne(serviceWrapper);
        if (service == null) {
            throw new RuntimeException("服务类型不存在");
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
        order.setEstimatedPrice(service.getBasePrice());
        order.setStatus(0); // 待确认

        // 如果指定了服务人员
        if (dto.getWorkerId() != null) {
            AppointmentWorkers worker = appointmentWorkersMapper.selectById(dto.getWorkerId());
            if (worker != null && worker.getStatus() == 1) {
                order.setWorkerId(worker.getId());
                order.setWorkerName(worker.getWorkerName());
                order.setWorkerPhone(worker.getWorkerPhone());
            }
        }

        appointmentOrdersMapper.insert(order);

        log.info("创建预约成功，订单号：{}，用户ID：{}", orderNo, userId);
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
        wrapper.eq(AppointmentWorkers::getStatus, 1)
                .like(AppointmentWorkers::getServiceTypes, serviceType)
                .orderByDesc(AppointmentWorkers::getRating);

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

    /**
     * 转换为服务VO
     */
    private AppointmentServiceVO convertToServiceVO(AppointmentServices service) {
        AppointmentServiceVO vo = new AppointmentServiceVO();
        vo.setId(service.getId());
        vo.setType(service.getServiceType());
        vo.setName(service.getServiceName());
        vo.setDescription(service.getDescription());
        vo.setIcon(service.getIcon());
        vo.setPrice(service.getBasePrice().toString());
        vo.setUnit(service.getUnit());
        vo.setIsHot(service.getIsHot() == 1);
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
