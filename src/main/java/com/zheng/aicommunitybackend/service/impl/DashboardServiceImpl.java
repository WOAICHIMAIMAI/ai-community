package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zheng.aicommunitybackend.domain.entity.*;
import com.zheng.aicommunitybackend.domain.vo.*;
import com.zheng.aicommunitybackend.mapper.*;
import com.zheng.aicommunitybackend.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现类
 */
@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UsersMapper usersMapper;
    
    @Autowired
    private CommunityPostsMapper communityPostsMapper;
    
    @Autowired
    private RepairOrdersMapper repairOrdersMapper;
    
    @Autowired
    private PostCommentsMapper postCommentsMapper;
    
    @Autowired
    private UserVerificationMapper userVerificationMapper;

    /**
     * 获取仪表盘概览数据
     *
     * @return 仪表盘概览数据
     */
    @Override
    public DashboardVO getDashboardOverview() {
        DashboardVO dashboardVO = new DashboardVO();
        
        try {
            // 获取用户总数和增长率
            Long userCount = getUserCount();
            Double userIncrease = getUserIncrease();
            dashboardVO.setUserCount(userCount);
            dashboardVO.setUserIncrease(userIncrease);
            
            // 获取帖子总数和增长率
            Long postCount = getPostCount();
            Double postIncrease = getPostIncrease();
            dashboardVO.setPostCount(postCount);
            dashboardVO.setPostIncrease(postIncrease);
            
            // 获取待处理工单数和增长率
            Long pendingOrderCount = getPendingOrderCount();
            Double orderIncrease = getOrderIncrease();
            dashboardVO.setPendingOrderCount(pendingOrderCount);
            dashboardVO.setOrderIncrease(orderIncrease);
            
            // 获取待审核内容数和增长率
            Long reviewCount = getReviewCount();
            Double reviewIncrease = getReviewIncrease();
            dashboardVO.setReviewCount(reviewCount);
            dashboardVO.setReviewIncrease(reviewIncrease);
        } catch (Exception e) {
            log.error("获取仪表盘概览数据出错", e);
        }
        
        return dashboardVO;
    }

    /**
     * 获取用户增长趋势
     *
     * @param timeRange 时间范围：week-本周, month-本月, year-全年
     * @return 用户增长趋势数据
     */
    @Override
    public UserGrowthVO getUserGrowthTrend(String timeRange) {
        UserGrowthVO userGrowthVO = new UserGrowthVO();
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        try {
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
            
            switch (timeRange) {
                case "week":
                    // 本周数据
                    LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    for (int i = 0; i < 7; i++) {
                        LocalDate date = startOfWeek.plusDays(i);
                        dates.add(date.format(formatter));
                        
                        // 查询当天注册的用户数
                        Date start = java.sql.Date.valueOf(date);
                        Date end = java.sql.Date.valueOf(date.plusDays(1));
                        
                        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.between(Users::getRegisterTime, start, end);
                        Long count = Long.valueOf(usersMapper.selectCount(queryWrapper));
                        counts.add(count);
                    }
                    break;
                    
                case "month":
                    // 本月数据
                    LocalDate startOfMonth = now.withDayOfMonth(1);
                    int daysInMonth = now.lengthOfMonth();
                    for (int i = 0; i < daysInMonth; i++) {
                        LocalDate date = startOfMonth.plusDays(i);
                        dates.add(date.format(formatter));
                        
                        // 查询当天注册的用户数
                        Date start = java.sql.Date.valueOf(date);
                        Date end = java.sql.Date.valueOf(date.plusDays(1));
                        
                        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.between(Users::getRegisterTime, start, end);
                        Long count = Long.valueOf(usersMapper.selectCount(queryWrapper));
                        counts.add(count);
                    }
                    break;
                    
                case "year":
                    // 全年数据
                    LocalDate startOfYear = now.withDayOfYear(1);
                    for (int i = 0; i < 12; i++) {
                        LocalDate date = startOfYear.plusMonths(i);
                        dates.add(date.format(DateTimeFormatter.ofPattern("yyyy-MM")));
                        
                        // 查询当月注册的用户数
                        LocalDate nextMonth = date.plusMonths(1);
                        Date start = java.sql.Date.valueOf(date);
                        Date end = java.sql.Date.valueOf(nextMonth);
                        
                        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.between(Users::getRegisterTime, start, end);
                        Long count = Long.valueOf(usersMapper.selectCount(queryWrapper));
                        counts.add(count);
                    }
                    break;
                    
                default:
                    // 默认使用本周数据
                    return getUserGrowthTrend("week");
            }
            
            userGrowthVO.setDates(dates);
            userGrowthVO.setCounts(counts);
        } catch (Exception e) {
            log.error("获取用户增长趋势数据出错", e);
        }
        
        return userGrowthVO;
    }

    /**
     * 获取报修工单类型分布
     *
     * @return 工单类型分布数据
     */
    @Override
    public RepairTypeDistributionVO getRepairTypeDistribution() {
        RepairTypeDistributionVO distributionVO = new RepairTypeDistributionVO();
        List<String> types = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        try {
            // 获取工单类型及数量
            Map<Integer, String> typeMap = new HashMap<>();
            typeMap.put(1, "水电故障");
            typeMap.put(2, "设备损坏");
            typeMap.put(3, "环境问题");
            typeMap.put(4, "安全隐患");
            typeMap.put(5, "其他");
            
            for (Map.Entry<Integer, String> entry : typeMap.entrySet()) {
                Integer typeId = entry.getKey();
                String typeName = entry.getValue();
                
                LambdaQueryWrapper<RepairOrders> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(RepairOrders::getRepairType, typeId);
                Long count = Long.valueOf(repairOrdersMapper.selectCount(queryWrapper));
                
                types.add(typeName);
                counts.add(count);
            }
            
            distributionVO.setTypes(types);
            distributionVO.setCounts(counts);
        } catch (Exception e) {
            log.error("获取报修工单类型分布数据出错", e);
        }
        
        return distributionVO;
    }

    /**
     * 获取最新工单
     *
     * @param limit 数量限制
     * @return 最新工单列表
     */
    @Override
    public List<LatestOrderVO> getLatestOrders(Integer limit) {
        List<LatestOrderVO> latestOrders = new ArrayList<>();
        
        try {
            // 查询最新的工单
            LambdaQueryWrapper<RepairOrders> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(RepairOrders::getCreateTime);
            queryWrapper.last("LIMIT " + (limit == null ? 5 : limit));
            List<RepairOrders> orders = repairOrdersMapper.selectList(queryWrapper);
            
            // 获取用户信息
            Set<Long> userIds = orders.stream()
                    .map(RepairOrders::getUserId)
                    .collect(Collectors.toSet());
            
            Map<Long, Users> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                LambdaQueryWrapper<Users> userQueryWrapper = new LambdaQueryWrapper<>();
                userQueryWrapper.in(Users::getId, userIds);
                List<Users> users = usersMapper.selectList(userQueryWrapper);
                userMap = users.stream().collect(Collectors.toMap(Users::getId, user -> user));
            }
            
            // 转换为VO
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (RepairOrders order : orders) {
                LatestOrderVO vo = new LatestOrderVO();
                vo.setId(order.getId());
                vo.setTitle(order.getDescription());
                
                Users user = userMap.get(order.getUserId());
                vo.setUserName(user != null ? user.getUsername() : "未知用户");
                
                // 状态转换
                String status;
                switch (order.getStatus()) {
                    case 0:
                        status = "pending";
                        break;
                    case 1:
                        status = "processing";
                        break;
                    case 2:
                        status = "completed";
                        break;
                    default:
                        status = "unknown";
                }
                vo.setStatus(status);
                
                vo.setCreatedAt(dateFormat.format(order.getCreateTime()));
                
                latestOrders.add(vo);
            }
        } catch (Exception e) {
            log.error("获取最新工单数据出错", e);
        }
        
        return latestOrders;
    }

    /**
     * 获取待审核内容
     *
     * @param limit 数量限制
     * @return 待审核内容列表
     */
    @Override
    public List<PendingReviewVO> getPendingReviews(Integer limit) {
        List<PendingReviewVO> pendingReviews = new ArrayList<>();
        
        try {
            int actualLimit = limit == null ? 5 : limit;
            int halfLimit = actualLimit / 2;
            
            // 查询待审核的帖子
            LambdaQueryWrapper<CommunityPosts> postQueryWrapper = new LambdaQueryWrapper<>();
            postQueryWrapper.eq(CommunityPosts::getStatus, 0); // 待审核状态
            postQueryWrapper.orderByDesc(CommunityPosts::getCreateTime);
            postQueryWrapper.last("LIMIT " + halfLimit);
            List<CommunityPosts> posts = communityPostsMapper.selectList(postQueryWrapper);
            
            // 查询待审核的评论
            LambdaQueryWrapper<PostComments> commentQueryWrapper = new LambdaQueryWrapper<>();
            commentQueryWrapper.eq(PostComments::getStatus, 0); // 待审核状态
            commentQueryWrapper.orderByDesc(PostComments::getCreateTime);
            commentQueryWrapper.last("LIMIT " + halfLimit);
            List<PostComments> comments = postCommentsMapper.selectList(commentQueryWrapper);
            
            // 获取用户信息
            Set<Long> userIds = new HashSet<>();
            posts.forEach(post -> userIds.add(post.getUserId()));
            comments.forEach(comment -> userIds.add(comment.getUserId()));
            
            Map<Long, Users> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                LambdaQueryWrapper<Users> userQueryWrapper = new LambdaQueryWrapper<>();
                userQueryWrapper.in(Users::getId, userIds);
                List<Users> users = usersMapper.selectList(userQueryWrapper);
                userMap = users.stream().collect(Collectors.toMap(Users::getId, user -> user));
            }
            
            // 转换帖子为VO
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (CommunityPosts post : posts) {
                PendingReviewVO vo = new PendingReviewVO();
                vo.setId(post.getId());
                vo.setTitle(post.getTitle());
                vo.setType("post");
                
                Users user = userMap.get(post.getUserId());
                vo.setUserName(user != null ? user.getUsername() : "未知用户");
                
                vo.setCreatedAt(dateFormat.format(post.getCreateTime()));
                
                pendingReviews.add(vo);
            }
            
            // 转换评论为VO
            for (PostComments comment : comments) {
                PendingReviewVO vo = new PendingReviewVO();
                vo.setId(comment.getId());
                vo.setTitle(comment.getContent().length() > 20 ? 
                        comment.getContent().substring(0, 20) + "..." : 
                        comment.getContent());
                vo.setType("comment");
                
                Users user = userMap.get(comment.getUserId());
                vo.setUserName(user != null ? user.getUsername() : "未知用户");
                
                vo.setCreatedAt(dateFormat.format(comment.getCreateTime()));
                
                pendingReviews.add(vo);
            }
            
            // 按时间排序
            pendingReviews.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            
            // 限制数量
            if (pendingReviews.size() > actualLimit) {
                pendingReviews = pendingReviews.subList(0, actualLimit);
            }
        } catch (Exception e) {
            log.error("获取待审核内容数据出错", e);
        }
        
        return pendingReviews;
    }
    
    /**
     * 获取用户总数
     */
    private Long getUserCount() {
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        return Long.valueOf(usersMapper.selectCount(queryWrapper));
    }
    
    /**
     * 获取用户增长率
     */
    private Double getUserIncrease() {
        // 获取今日新增用户数
        LocalDate today = LocalDate.now();
        Date todayStart = java.sql.Date.valueOf(today);
        Date todayEnd = java.sql.Date.valueOf(today.plusDays(1));
        
        LambdaQueryWrapper<Users> todayQueryWrapper = new LambdaQueryWrapper<>();
        todayQueryWrapper.between(Users::getRegisterTime, todayStart, todayEnd);
        long todayCount = usersMapper.selectCount(todayQueryWrapper);
        
        // 获取昨日新增用户数
        LocalDate yesterday = today.minusDays(1);
        Date yesterdayStart = java.sql.Date.valueOf(yesterday);
        Date yesterdayEnd = java.sql.Date.valueOf(today);
        
        LambdaQueryWrapper<Users> yesterdayQueryWrapper = new LambdaQueryWrapper<>();
        yesterdayQueryWrapper.between(Users::getRegisterTime, yesterdayStart, yesterdayEnd);
        long yesterdayCount = usersMapper.selectCount(yesterdayQueryWrapper);
        
        // 计算增长率
        if (yesterdayCount == 0) {
            return todayCount > 0 ? 100.0 : 0.0;
        }
        
        return (double) (todayCount - yesterdayCount) / yesterdayCount * 100;
    }
    
    /**
     * 获取帖子总数
     */
    private Long getPostCount() {
        LambdaQueryWrapper<CommunityPosts> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(CommunityPosts::getStatus, 2); // 排除已删除的帖子
        return Long.valueOf(communityPostsMapper.selectCount(queryWrapper));
    }
    
    /**
     * 获取帖子增长率
     */
    private Double getPostIncrease() {
        // 获取今日新增帖子数
        LocalDate today = LocalDate.now();
        Date todayStart = java.sql.Date.valueOf(today);
        Date todayEnd = java.sql.Date.valueOf(today.plusDays(1));
        
        LambdaQueryWrapper<CommunityPosts> todayQueryWrapper = new LambdaQueryWrapper<>();
        todayQueryWrapper.between(CommunityPosts::getCreateTime, todayStart, todayEnd);
        todayQueryWrapper.ne(CommunityPosts::getStatus, 2); // 排除已删除的帖子
        long todayCount = communityPostsMapper.selectCount(todayQueryWrapper);
        
        // 获取昨日新增帖子数
        LocalDate yesterday = today.minusDays(1);
        Date yesterdayStart = java.sql.Date.valueOf(yesterday);
        Date yesterdayEnd = java.sql.Date.valueOf(today);
        
        LambdaQueryWrapper<CommunityPosts> yesterdayQueryWrapper = new LambdaQueryWrapper<>();
        yesterdayQueryWrapper.between(CommunityPosts::getCreateTime, yesterdayStart, yesterdayEnd);
        yesterdayQueryWrapper.ne(CommunityPosts::getStatus, 2); // 排除已删除的帖子
        long yesterdayCount = communityPostsMapper.selectCount(yesterdayQueryWrapper);
        
        // 计算增长率
        if (yesterdayCount == 0) {
            return todayCount > 0 ? 100.0 : 0.0;
        }
        
        return (double) (todayCount - yesterdayCount) / yesterdayCount * 100;
    }
    
    /**
     * 获取待处理工单数
     */
    private Long getPendingOrderCount() {
        LambdaQueryWrapper<RepairOrders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RepairOrders::getStatus, 0); // 待处理状态
        return Long.valueOf(repairOrdersMapper.selectCount(queryWrapper));
    }
    
    /**
     * 获取工单增长率
     */
    private Double getOrderIncrease() {
        // 获取今日新增工单数
        LocalDate today = LocalDate.now();
        Date todayStart = java.sql.Date.valueOf(today);
        Date todayEnd = java.sql.Date.valueOf(today.plusDays(1));
        
        LambdaQueryWrapper<RepairOrders> todayQueryWrapper = new LambdaQueryWrapper<>();
        todayQueryWrapper.between(RepairOrders::getCreateTime, todayStart, todayEnd);
        long todayCount = repairOrdersMapper.selectCount(todayQueryWrapper);
        
        // 获取昨日新增工单数
        LocalDate yesterday = today.minusDays(1);
        Date yesterdayStart = java.sql.Date.valueOf(yesterday);
        Date yesterdayEnd = java.sql.Date.valueOf(today);
        
        LambdaQueryWrapper<RepairOrders> yesterdayQueryWrapper = new LambdaQueryWrapper<>();
        yesterdayQueryWrapper.between(RepairOrders::getCreateTime, yesterdayStart, yesterdayEnd);
        long yesterdayCount = repairOrdersMapper.selectCount(yesterdayQueryWrapper);
        
        // 计算增长率
        if (yesterdayCount == 0) {
            return todayCount > 0 ? 100.0 : 0.0;
        }
        
        return (double) (todayCount - yesterdayCount) / yesterdayCount * 100;
    }
    
    /**
     * 获取待审核内容数
     */
    private Long getReviewCount() {
        // 待审核帖子数
        LambdaQueryWrapper<CommunityPosts> postQueryWrapper = new LambdaQueryWrapper<>();
        postQueryWrapper.eq(CommunityPosts::getStatus, 0); // 待审核状态
        long postCount = communityPostsMapper.selectCount(postQueryWrapper);
        
        // 待审核评论数
        LambdaQueryWrapper<PostComments> commentQueryWrapper = new LambdaQueryWrapper<>();
        commentQueryWrapper.eq(PostComments::getStatus, 0); // 待审核状态
        long commentCount = postCommentsMapper.selectCount(commentQueryWrapper);
        
        // 待审核认证数
        LambdaQueryWrapper<UserVerification> verificationQueryWrapper = new LambdaQueryWrapper<>();
        verificationQueryWrapper.eq(UserVerification::getVerificationStatus, 0); // 待审核状态
        long verificationCount = userVerificationMapper.selectCount(verificationQueryWrapper);
        
        return postCount + commentCount + verificationCount;
    }
    
    /**
     * 获取审核内容增长率
     */
    private Double getReviewIncrease() {
        // 获取今日新增待审核内容数
        LocalDate today = LocalDate.now();
        Date todayStart = java.sql.Date.valueOf(today);
        Date todayEnd = java.sql.Date.valueOf(today.plusDays(1));
        
        // 今日新增待审核帖子
        LambdaQueryWrapper<CommunityPosts> todayPostQueryWrapper = new LambdaQueryWrapper<>();
        todayPostQueryWrapper.eq(CommunityPosts::getStatus, 0);
        todayPostQueryWrapper.between(CommunityPosts::getCreateTime, todayStart, todayEnd);
        long todayPostCount = communityPostsMapper.selectCount(todayPostQueryWrapper);
        
        // 今日新增待审核评论
        LambdaQueryWrapper<PostComments> todayCommentQueryWrapper = new LambdaQueryWrapper<>();
        todayCommentQueryWrapper.eq(PostComments::getStatus, 0);
        todayCommentQueryWrapper.between(PostComments::getCreateTime, todayStart, todayEnd);
        long todayCommentCount = postCommentsMapper.selectCount(todayCommentQueryWrapper);
        
        // 今日新增待审核认证
        LambdaQueryWrapper<UserVerification> todayVerificationQueryWrapper = new LambdaQueryWrapper<>();
        todayVerificationQueryWrapper.eq(UserVerification::getVerificationStatus, 0);
        todayVerificationQueryWrapper.between(UserVerification::getSubmitTime, todayStart, todayEnd);
        long todayVerificationCount = userVerificationMapper.selectCount(todayVerificationQueryWrapper);
        
        long todayCount = todayPostCount + todayCommentCount + todayVerificationCount;
        
        // 获取昨日新增待审核内容数
        LocalDate yesterday = today.minusDays(1);
        Date yesterdayStart = java.sql.Date.valueOf(yesterday);
        Date yesterdayEnd = java.sql.Date.valueOf(today);
        
        // 昨日新增待审核帖子
        LambdaQueryWrapper<CommunityPosts> yesterdayPostQueryWrapper = new LambdaQueryWrapper<>();
        yesterdayPostQueryWrapper.eq(CommunityPosts::getStatus, 0);
        yesterdayPostQueryWrapper.between(CommunityPosts::getCreateTime, yesterdayStart, yesterdayEnd);
        long yesterdayPostCount = communityPostsMapper.selectCount(yesterdayPostQueryWrapper);
        
        // 昨日新增待审核评论
        LambdaQueryWrapper<PostComments> yesterdayCommentQueryWrapper = new LambdaQueryWrapper<>();
        yesterdayCommentQueryWrapper.eq(PostComments::getStatus, 0);
        yesterdayCommentQueryWrapper.between(PostComments::getCreateTime, yesterdayStart, yesterdayEnd);
        long yesterdayCommentCount = postCommentsMapper.selectCount(yesterdayCommentQueryWrapper);
        
        // 昨日新增待审核认证
        LambdaQueryWrapper<UserVerification> yesterdayVerificationQueryWrapper = new LambdaQueryWrapper<>();
        yesterdayVerificationQueryWrapper.eq(UserVerification::getVerificationStatus, 0);
        yesterdayVerificationQueryWrapper.between(UserVerification::getSubmitTime, yesterdayStart, yesterdayEnd);
        long yesterdayVerificationCount = userVerificationMapper.selectCount(yesterdayVerificationQueryWrapper);
        
        long yesterdayCount = yesterdayPostCount + yesterdayCommentCount + yesterdayVerificationCount;
        
        // 计算增长率
        if (yesterdayCount == 0) {
            return todayCount > 0 ? 100.0 : 0.0;
        }
        
        return (double) (todayCount - yesterdayCount) / yesterdayCount * 100;
    }
} 