package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.dto.RedPacketActivityCreateDTO;
import com.zheng.aicommunitybackend.domain.dto.RedPacketActivityQueryDTO;
import com.zheng.aicommunitybackend.domain.entity.*;
import com.zheng.aicommunitybackend.domain.vo.RedPacketActivityListVO;
import com.zheng.aicommunitybackend.domain.vo.RedPacketActivityVO;
import com.zheng.aicommunitybackend.domain.vo.RedPacketGrabVO;
import com.zheng.aicommunitybackend.domain.vo.RedPacketRecordVO;
import com.zheng.aicommunitybackend.mapper.*;
import com.zheng.aicommunitybackend.service.RedPacketService;
import com.zheng.aicommunitybackend.service.UserAccountsService;
import com.zheng.aicommunitybackend.util.RedPacketAlgorithm;
import com.zheng.aicommunitybackend.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 红包服务实现类
 */
@Slf4j
@Service
public class RedPacketServiceImpl extends ServiceImpl<RedPacketActivitiesMapper, RedPacketActivities> 
        implements RedPacketService {

    @Autowired
    private RedPacketActivitiesMapper activitiesMapper;
    
    @Autowired
    private RedPacketDetailsMapper detailsMapper;
    
    @Autowired
    private RedPacketRecordsMapper recordsMapper;
    
    @Autowired
    private RedPacketActivityConfigMapper configMapper;
    
    @Autowired
    private UserAccountsService userAccountsService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // Redis Key 前缀
    private static final String ACTIVITY_QUEUE_KEY = "red_packet:activity:%d:queue";
    private static final String ACTIVITY_INFO_KEY = "red_packet:activity:%d:info";
    private static final String USER_GRABBED_KEY = "red_packet:user:%d:grabbed";
    private static final String ACTIVITY_LOCK_KEY = "red_packet:activity:%d:lock";

    @Override
    @Transactional
    public Long createActivity(RedPacketActivityCreateDTO createDTO, Long creatorId) {
        log.info("开始创建红包活动，创建者ID: {}, 活动名称: {}", creatorId, createDTO.getActivityName());

        try {
            // 1. 创建活动记录
        RedPacketActivities activity = new RedPacketActivities();
        activity.setActivityName(createDTO.getActivityName());
        activity.setActivityDesc(createDTO.getActivityDesc());
        activity.setTotalAmount(createDTO.getTotalAmountCents());
        activity.setTotalCount(createDTO.getTotalCount());
        activity.setGrabbedCount(0);
        activity.setGrabbedAmount(0L);
        // 如果立即开始，调整开始时间为当前时间
        if (createDTO.getStartImmediately()) {
            activity.setStartTime(new Date());
            activity.setStatus(1);
        } else {
            activity.setStartTime(localDateTimeToDate(createDTO.getStartTime()));
            activity.setStatus(0);
        }
        activity.setEndTime(localDateTimeToDate(createDTO.getEndTime()));
        activity.setCreatorId(creatorId);
        activity.setCreatedTime(new Date());
        activity.setUpdatedTime(new Date());
        
        save(activity);
        Long activityId = activity.getId();
        
        // 2. 分配红包
        String algorithm = createDTO.getAlgorithm() != null ? createDTO.getAlgorithm() : "DOUBLE_AVERAGE";
        List<Long> amounts = RedPacketAlgorithm.allocate(
                algorithm, 
                createDTO.getTotalAmountCents(), 
                createDTO.getTotalCount(), 
                createDTO.getMinAmount()
        );
        
        // 3. 批量插入红包详情
        List<RedPacketDetails> details = new ArrayList<>();
        for (int i = 0; i < amounts.size(); i++) {
            RedPacketDetails detail = new RedPacketDetails();
            detail.setActivityId(activityId);
            detail.setPacketIndex(i + 1);
            detail.setAmount(amounts.get(i));
            detail.setStatus(0);
            detail.setCreatedTime(new Date());
            details.add(detail);
        }
        detailsMapper.batchInsert(details);
        
        // 4. 保存基本配置
        List<RedPacketActivityConfig> configs = new ArrayList<>();

        // 保存算法配置
        RedPacketActivityConfig algorithmConfig = new RedPacketActivityConfig();
        algorithmConfig.setActivityId(activityId);
        algorithmConfig.setConfigKey("algorithm");
        algorithmConfig.setConfigValue(createDTO.getAlgorithm() != null ? createDTO.getAlgorithm() : "DOUBLE_AVERAGE");
        algorithmConfig.setConfigDesc("红包分配算法");
        algorithmConfig.setCreatedTime(new Date());
        configs.add(algorithmConfig);

        // 保存最小金额配置
        RedPacketActivityConfig minAmountConfig = new RedPacketActivityConfig();
        minAmountConfig.setActivityId(activityId);
        minAmountConfig.setConfigKey("min_amount");
        minAmountConfig.setConfigValue(String.valueOf(createDTO.getMinAmount() != null ? createDTO.getMinAmount() : 1L));
        minAmountConfig.setConfigDesc("最小红包金额（分）");
        minAmountConfig.setCreatedTime(new Date());
        configs.add(minAmountConfig);

        // 保存最大金额配置（如果有）
        if (createDTO.getMaxAmount() != null) {
            RedPacketActivityConfig maxAmountConfig = new RedPacketActivityConfig();
            maxAmountConfig.setActivityId(activityId);
            maxAmountConfig.setConfigKey("max_amount");
            maxAmountConfig.setConfigValue(String.valueOf(createDTO.getMaxAmount()));
            maxAmountConfig.setConfigDesc("最大红包金额（分）");
            maxAmountConfig.setCreatedTime(new Date());
            configs.add(maxAmountConfig);
        }

        configMapper.batchInsert(configs);

        // 5. 如果立即开始，预加载到Redis
        if (createDTO.getStartImmediately()) {
            preloadActivityToRedis(activityId);
        }
        
            log.info("红包活动创建成功，活动ID: {}, 总金额: {}分, 总数量: {}",
                    activityId, createDTO.getTotalAmountCents(), createDTO.getTotalCount());

            return activityId;
        } catch (Exception e) {
            log.error("创建红包活动失败，创建者ID: {}, 活动名称: {}", creatorId, createDTO.getActivityName(), e);
            throw new BaseException("创建红包活动失败：" + e.getMessage());
        }
    }

    @Override
    public IPage<RedPacketActivityVO> getActivityPage(RedPacketActivityQueryDTO queryDTO) {
        Page<RedPacketActivityVO> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        return activitiesMapper.selectActivityPage(page, queryDTO.getStatus(), queryDTO.getActivityName());
    }

    @Override
    public List<RedPacketActivityListVO> getActiveActivitiesForUser(Long userId) {
        List<RedPacketActivityVO> activities = activitiesMapper.selectActiveActivities();
        List<RedPacketActivityListVO> result = new ArrayList<>();

        for (RedPacketActivityVO activity : activities) {
            RedPacketActivityListVO listVO = new RedPacketActivityListVO();
            listVO.setId(activity.getId());
            listVO.setActivityName(activity.getActivityName());
            listVO.setActivityDesc(activity.getActivityDesc());
            listVO.setTotalAmountYuan(activity.getTotalAmountYuan());
            listVO.setTotalCount(activity.getTotalCount());
            listVO.setGrabbedCount(activity.getGrabbedCount());
            listVO.setStartTime(activity.getStartTime());
            listVO.setEndTime(activity.getEndTime());
            listVO.setStatus(activity.getStatus());
            listVO.setStatusName(activity.getStatusName());
            listVO.setGrabRate(activity.getGrabRate());

            // 检查用户是否已抢过
            if (userId != null) {
                RedPacketDetails userPacket = detailsMapper.selectUserGrabbedPacket(activity.getId(), userId);
                listVO.setHasGrabbed(userPacket != null);
                listVO.setCanGrab(userPacket == null && activity.getStatus() == 1 && activity.getRemainingCount() > 0);
            } else {
                listVO.setHasGrabbed(false);
                listVO.setCanGrab(activity.getStatus() == 1 && activity.getRemainingCount() > 0);
            }

            result.add(listVO);
        }

        return result;
    }

    @Override
    public RedPacketActivityVO getActivityDetail(Long id, Long userId) {
        RedPacketActivityVO activity = activitiesMapper.selectActivityDetail(id);
        if (activity == null) {
            return null;
        }
        
        // 设置用户相关信息
        if (userId != null) {
            RedPacketDetails userPacket = detailsMapper.selectUserGrabbedPacket(id, userId);
            if (userPacket != null) {
                activity.setHasGrabbed(true);
                activity.setUserGrabbedAmount(userPacket.getAmount());
                activity.setUserGrabTime(userPacket.getGrabTime());
            } else {
                activity.setHasGrabbed(false);
            }
            
            // 判断是否可以抢红包
            activity.setCanGrab(canUserGrabRedPacket(id, userId));
            if (!activity.getCanGrab()) {
                activity.setCannotGrabReason(getCannotGrabReason(activity, userId));
            }
        }
        
        return activity;
    }

    @Override
    public RedPacketGrabVO grabRedPacket(Long userId, Long activityId) {
        log.info("用户 {} 尝试抢红包，活动ID: {}", userId, activityId);
        
        // 1. 检查活动状态
        RedPacketActivities activity = getById(activityId);
        if (activity == null) {
            return RedPacketGrabVO.failure("活动不存在");
        }
        
        if (activity.getStatus() != 1) {
            if (activity.getStatus() == 0) {
                return RedPacketGrabVO.activityNotStarted();
            } else {
                return RedPacketGrabVO.activityEnded();
            }
        }
        
        // 2. 检查时间
        Date now = new Date();
        if (now.before(activity.getStartTime())) {
            return RedPacketGrabVO.activityNotStarted();
        }
        if (now.after(activity.getEndTime())) {
            return RedPacketGrabVO.activityEnded();
        }
        
        // 3. 使用Redis Lua脚本原子性抢红包
        try {
            String queueKey = String.format(ACTIVITY_QUEUE_KEY, activityId);
            String userKey = String.format(USER_GRABBED_KEY, userId);

            String luaScript =
                "local queueKey = KEYS[1] " +
                "local userKey = KEYS[2] " +
                "local userId = ARGV[1] " +
                "local activityId = ARGV[2] " +

                "if redis.call('SISMEMBER', userKey, activityId) == 1 then " +
                    "return {0, 'already_grabbed'} " +
                "end " +

                "local packetId = redis.call('LPOP', queueKey) " +
                "if not packetId then " +
                    "return {0, 'no_packet'} " +
                "end " +

                "redis.call('SADD', userKey, activityId) " +
                "redis.call('EXPIRE', userKey, 86400) " +
                "return {1, packetId}";

            DefaultRedisScript<List> script = new DefaultRedisScript<>();
            script.setScriptText(luaScript);
            script.setResultType(List.class);

            // 使用StringRedisTemplate执行Lua脚本，避免JSON序列化问题
            @SuppressWarnings("unchecked")
            List<Object> result = stringRedisTemplate.execute(
                script,
                Arrays.asList(queueKey, userKey),
                userId.toString(), activityId.toString());

            if (result == null || result.size() != 2) {
                return RedPacketGrabVO.systemBusy();
            }

            // 使用StringRedisSerializer后，返回值都是字符串
            String successStr = result.get(0).toString();
            Integer success = Integer.valueOf(successStr);

            if (success == 0) {
                String reason = result.get(1).toString();
                if ("already_grabbed".equals(reason)) {
                    return RedPacketGrabVO.alreadyGrabbed();
                } else {
                    return RedPacketGrabVO.noPacketLeft();
                }
            }

            // 4. 抢到红包，更新数据库
            String packetIdStr = result.get(1).toString();
            Long packetId = Long.valueOf(packetIdStr);
            return processGrabbedRedPacket(userId, activityId, packetId);
        } catch (Exception e) {
            log.error("Redis操作异常，用户: {}, 活动: {}", userId, activityId, e);
            return RedPacketGrabVO.systemBusy();
        }
    }

    /**
     * 处理抢到的红包
     */
    @Transactional
    public RedPacketGrabVO processGrabbedRedPacket(Long userId, Long activityId, Long packetId) {
        try {
            Date grabTime = new Date();
            
            // 1. 更新红包详情状态
            int updateResult = detailsMapper.grabRedPacket(packetId, userId, grabTime);
            if (updateResult == 0) {
                log.warn("红包已被抢走，用户: {}, 红包ID: {}", userId, packetId);
                return RedPacketGrabVO.noPacketLeft();
            }
            
            // 2. 获取红包信息
            RedPacketDetails packet = detailsMapper.selectById(packetId);
            if (packet == null) {
                log.error("红包详情不存在，红包ID: {}", packetId);
                return RedPacketGrabVO.systemBusy();
            }
            
            // 3. 生成交易流水号
            String transactionNo = generateTransactionNo(activityId, userId);
            
            // 4. 创建抢红包记录
            RedPacketRecords record = new RedPacketRecords();
            record.setActivityId(activityId);
            record.setUserId(userId);
            record.setPacketDetailId(packetId);
            record.setAmount(packet.getAmount());
            record.setTransactionNo(transactionNo);
            record.setGrabTime(grabTime);
            record.setAccountUpdated(0);
            record.setCreatedTime(grabTime);
            recordsMapper.insert(record);
            
            // 5. 更新活动统计
            activitiesMapper.updateActivityStats(activityId, packet.getAmount());
            
            // 6. 异步更新用户账户余额
            updateUserAccountAsync(userId, packet.getAmount(), transactionNo, record.getId());
            
            // 7. 构造返回结果
            RedPacketGrabVO result = RedPacketGrabVO.success(packet.getAmount(), transactionNo, grabTime);
            result.setActivityId(activityId);
            result.setPacketIndex(packet.getPacketIndex());
            
            log.info("用户 {} 成功抢到红包，金额: {}分, 交易号: {}", userId, packet.getAmount(), transactionNo);
            return result;
            
        } catch (Exception e) {
            log.error("处理抢红包失败，用户: {}, 活动: {}, 红包: {}", userId, activityId, packetId, e);
            return RedPacketGrabVO.systemBusy();
        }
    }

    /**
     * 异步更新用户账户余额
     */
    private void updateUserAccountAsync(Long userId, Long amount, String transactionNo, Long recordId) {
        // 这里可以使用消息队列异步处理，暂时同步处理
        try {
            // 抢红包是增加余额，直接调用Mapper的updateBalance方法
            BigDecimal amountDecimal = new BigDecimal(amount).divide(new BigDecimal(100));

            // 确保用户账户存在
            userAccountsService.createAccount(userId);

            // 直接更新余额，使用正数表示增加
            // 注意：consume方法传入负数表示增加余额
            boolean updateResult = userAccountsService.consume(userId, amountDecimal.negate(), "抢红包-" + transactionNo);

            if (updateResult) {
                recordsMapper.updateAccountStatus(recordId, 1);
                log.info("用户账户更新成功，用户: {}, 金额: {}分", userId, amount);
            } else {
                log.error("用户账户更新失败，用户: {}, 金额: {}分", userId, amount);
            }
        } catch (Exception e) {
            log.error("异步更新用户账户失败，用户: {}, 金额: {}分", userId, amount, e);
        }
    }

    /**
     * 生成交易流水号
     */
    private String generateTransactionNo(Long activityId, Long userId) {
        return String.format("RP%d%d%d", 
                System.currentTimeMillis(), 
                activityId, 
                userId % 10000);
    }

    /**
     * 检查用户是否可以抢红包
     */
    private boolean canUserGrabRedPacket(Long activityId, Long userId) {
        // 检查用户是否已经抢过
        RedPacketDetails userPacket = detailsMapper.selectUserGrabbedPacket(activityId, userId);
        return userPacket == null;
    }

    /**
     * 获取不能抢红包的原因
     */
    private String getCannotGrabReason(RedPacketActivityVO activity, Long userId) {
        if (activity.getStatus() == 0) {
            return "活动还未开始";
        }
        if (activity.getStatus() == 2) {
            return "活动已结束";
        }
        if (activity.getStatus() == 3) {
            return "活动已取消";
        }
        if (activity.getHasGrabbed()) {
            return "您已经抢过这个红包了";
        }
        if (activity.getRemainingCount() <= 0) {
            return "红包已被抢完";
        }
        return "暂时无法抢红包";
    }

    @Override
    public IPage<RedPacketRecordVO> getUserRecordPage(Long userId, Long activityId, Integer page, Integer size) {
        Page<RedPacketRecordVO> pageParam = new Page<>(page, size);
        return recordsMapper.selectUserRecordPage(pageParam, userId, activityId);
    }

    @Override
    public IPage<RedPacketRecordVO> getActivityRecordPage(Long activityId, Integer page, Integer size) {
        Page<RedPacketRecordVO> pageParam = new Page<>(page, size);
        return recordsMapper.selectActivityRecordPage(pageParam, activityId);
    }

    @Override
    @Transactional
    public boolean cancelActivity(Long activityId, Long operatorId) {
        RedPacketActivities activity = getById(activityId);
        if (activity == null) {
            return false;
        }
        
        // 只有未开始的活动可以取消
        if (activity.getStatus() != 0) {
            return false;
        }
        
        activity.setStatus(3);
        activity.setUpdatedTime(new Date());
        updateById(activity);
        
        log.info("活动已取消，活动ID: {}, 操作者: {}", activityId, operatorId);
        return true;
    }

    @Override
    @Transactional
    public boolean startActivity(Long activityId, Long operatorId) {
        RedPacketActivities activity = getById(activityId);
        if (activity == null) {
            return false;
        }
        
        // 只有未开始的活动可以手动开始
        if (activity.getStatus() != 0) {
            return false;
        }
        
        activity.setStatus(1);
        activity.setUpdatedTime(new Date());
        updateById(activity);
        
        // 预加载到Redis
        preloadActivityToRedis(activityId);
        
        log.info("活动已开始，活动ID: {}, 操作者: {}", activityId, operatorId);
        return true;
    }

    @Override
    @Transactional
    public boolean endActivity(Long activityId, Long operatorId) {
        RedPacketActivities activity = getById(activityId);
        if (activity == null) {
            return false;
        }
        
        // 只有进行中的活动可以手动结束
        if (activity.getStatus() != 1) {
            return false;
        }
        
        activity.setStatus(2);
        activity.setUpdatedTime(new Date());
        updateById(activity);
        
        // 清理Redis数据
        clearActivityFromRedis(activityId);
        
        log.info("活动已结束，活动ID: {}, 操作者: {}", activityId, operatorId);
        return true;
    }

    @Override
    public boolean preloadActivityToRedis(Long activityId) {
        try {
            // 获取可抢红包ID列表
            List<Long> packetIds = detailsMapper.selectAvailablePacketIds(activityId);
            if (packetIds.isEmpty()) {
                log.warn("活动没有可抢红包，活动ID: {}", activityId);
                return false;
            }
            
            String queueKey = String.format(ACTIVITY_QUEUE_KEY, activityId);
            String infoKey = String.format(ACTIVITY_INFO_KEY, activityId);
            
            // 清空旧数据
            stringRedisTemplate.delete(queueKey);
            redisTemplate.delete(infoKey);

            // 将红包ID加入队列
            for (Long packetId : packetIds) {
                stringRedisTemplate.opsForList().rightPush(queueKey, packetId.toString());
            }
            stringRedisTemplate.expire(queueKey, 24, TimeUnit.HOURS);
            
            // 保存活动信息
            RedPacketActivities activity = getById(activityId);
            Map<String, Object> activityInfo = new HashMap<>();
            activityInfo.put("id", activity.getId());
            activityInfo.put("name", activity.getActivityName());
            activityInfo.put("totalCount", activity.getTotalCount());
            activityInfo.put("totalAmount", activity.getTotalAmount());
            activityInfo.put("startTime", activity.getStartTime().getTime());
            activityInfo.put("endTime", activity.getEndTime().getTime());
            
            redisTemplate.opsForHash().putAll(infoKey, activityInfo);
            redisTemplate.expire(infoKey, 24, TimeUnit.HOURS);
            
            log.info("活动预加载到Redis成功，活动ID: {}, 红包数量: {}", activityId, packetIds.size());
            return true;
            
        } catch (Exception e) {
            log.error("预加载活动到Redis失败，活动ID: {}", activityId, e);
            return false;
        }
    }

    @Override
    public boolean clearActivityFromRedis(Long activityId) {
        try {
            String queueKey = String.format(ACTIVITY_QUEUE_KEY, activityId);
            String infoKey = String.format(ACTIVITY_INFO_KEY, activityId);
            
            stringRedisTemplate.delete(queueKey);
            redisTemplate.delete(infoKey);
            
            log.info("清理活动Redis数据成功，活动ID: {}", activityId);
            return true;
            
        } catch (Exception e) {
            log.error("清理活动Redis数据失败，活动ID: {}", activityId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public int processUnprocessedRecords(Integer limit) {
        List<RedPacketRecords> records = recordsMapper.selectUnprocessedRecords(limit);
        int processedCount = 0;
        
        for (RedPacketRecords record : records) {
            try {
                BigDecimal amount = new BigDecimal(record.getAmount()).divide(new BigDecimal(100));

                // 确保用户账户存在
                userAccountsService.createAccount(record.getUserId());

                // 直接更新余额，使用正数表示增加
                // 注意：consume方法传入负数表示增加余额
                boolean updateResult = userAccountsService.consume(record.getUserId(),
                        amount.negate(), "抢红包补偿-" + record.getTransactionNo());

                if (updateResult) {
                    recordsMapper.updateAccountStatus(record.getId(), 1);
                    processedCount++;
                    log.info("补偿处理成功，记录ID: {}, 用户: {}, 金额: {}分",
                            record.getId(), record.getUserId(), record.getAmount());
                } else {
                    log.error("补偿处理失败，记录ID: {}, 用户: {}, 金额: {}分",
                            record.getId(), record.getUserId(), record.getAmount());
                }
            } catch (Exception e) {
                log.error("补偿处理异常，记录ID: {}", record.getId(), e);
            }
        }
        
        log.info("补偿处理完成，处理数量: {}/{}", processedCount, records.size());
        return processedCount;
    }

    @Override
    public Map<String, Object> getActivityStats(Long activityId) {
        return recordsMapper.selectActivityRecordStats(activityId);
    }

    @Override
    public Map<String, Object> getUserStats(Long userId) {
        return recordsMapper.selectUserRecordStats(userId);
    }

    /**
     * LocalDateTime转Date
     */
    private Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date转LocalDateTime
     */
    private LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
