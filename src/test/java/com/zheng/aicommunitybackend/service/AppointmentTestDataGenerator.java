package com.zheng.aicommunitybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zheng.aicommunitybackend.domain.entity.AppointmentOrders;
import com.zheng.aicommunitybackend.domain.entity.AppointmentServices;
import com.zheng.aicommunitybackend.domain.entity.AppointmentWorkers;
import com.zheng.aicommunitybackend.domain.entity.Users;
import com.zheng.aicommunitybackend.mapper.AppointmentOrdersMapper;
import com.zheng.aicommunitybackend.mapper.AppointmentServicesMapper;
import com.zheng.aicommunitybackend.mapper.AppointmentWorkersMapper;
import com.zheng.aicommunitybackend.mapper.UsersMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 预约服务测试数据生成器
 * 用于为预约服务模块生成虚拟测试数据
 */
@SpringBootTest
public class AppointmentTestDataGenerator {

    @Autowired
    private AppointmentServicesMapper appointmentServicesMapper;

    @Autowired
    private AppointmentWorkersMapper appointmentWorkersMapper;

    @Autowired
    private AppointmentOrdersMapper appointmentOrdersMapper;

    @Autowired
    private UsersMapper usersMapper;

    private final Random random = new Random();

    /**
     * 验证数据库连接和表是否存在
     */
    @Test
    public void validateDatabaseConnection() {
        System.out.println("=== 验证数据库连接和表结构 ===");
        
        try {
            // 检查表是否有数据
            long userCount = usersMapper.selectCount(null);
            long serviceCount = appointmentServicesMapper.selectCount(null);
            long workerCount = appointmentWorkersMapper.selectCount(null);
            long orderCount = appointmentOrdersMapper.selectCount(null);
            
            System.out.println("当前数据统计:");
            System.out.println("- 用户表: " + userCount + " 条");
            System.out.println("- 服务类型表: " + serviceCount + " 条");
            System.out.println("- 服务人员表: " + workerCount + " 条");
            System.out.println("- 预约订单表: " + orderCount + " 条");
            
            // 尝试插入一条测试数据来验证表结构
            System.out.println("\n验证表结构...");
            
            // 测试服务类型表插入
            AppointmentServices testService = new AppointmentServices();
            testService.setServiceType("test");
            testService.setServiceName("测试服务");
            testService.setDescription("测试描述");
            testService.setIcon("test-icon");
            testService.setBasePrice(new BigDecimal("100.00"));
            testService.setUnit("次");
            testService.setIsHot(0);
            testService.setSortOrder(999);
            testService.setStatus(1);
            
            int result = appointmentServicesMapper.insert(testService);
            if (result > 0) {
                System.out.println("✅ 服务类型表结构正常，测试数据插入成功 (ID: " + testService.getId() + ")");
                // 删除测试数据
                appointmentServicesMapper.deleteById(testService.getId());
                System.out.println("✅ 测试数据已清理");
            } else {
                System.out.println("❌ 服务类型表插入失败");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 数据库验证失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 数据库验证完成 ===\n");
    }

    /**
     * 生成所有测试数据 - 持久化版本（数据会真正保存到数据库）
     */
    @Test
    public void generateAllTestDataPersistent() {
        System.out.println("开始生成预约服务测试数据（持久化模式）...");
        
        try {
            // 1. 生成测试用户
            generateTestUsersPersistent();
            
            // 2. 生成服务类型
            generateServiceTypesPersistent();
            
            // 3. 生成服务人员
            generateServiceWorkersPersistent();
            
            // 4. 生成预约订单
            generateAppointmentOrdersPersistent();
            
            System.out.println("✅ 所有测试数据生成完成！");
            
            // 显示数据统计
            showDataStatistics();
            
        } catch (Exception e) {
            System.err.println("❌ 测试数据生成失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 生成所有测试数据
     */
    @Test
    @Transactional
    @Rollback(false)
    public void generateAllTestData() {
        System.out.println("开始生成预约服务测试数据...");
        
        try {
            // 0. 验证数据库连接
            validateDatabaseConnection();
            
            // 1. 生成测试用户数据
            generateTestUsers();
            
            // 2. 生成服务类型数据
            generateServiceTypes();
            
            // 3. 生成服务人员数据
            generateServiceWorkers();
            
            // 4. 生成预约订单数据
            generateAppointmentOrders();
            
            // 5. 显示最终统计
            showDataStatistics();
            
            System.out.println("预约服务测试数据生成完成！");
        } catch (Exception e) {
            System.err.println("测试数据生成失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 显示数据统计
     */
    private void showDataStatistics() {
        System.out.println("\n=== 数据生成统计 ===");
        try {
            long userCount = usersMapper.selectCount(null);
            long serviceCount = appointmentServicesMapper.selectCount(null);
            long workerCount = appointmentWorkersMapper.selectCount(null);
            long orderCount = appointmentOrdersMapper.selectCount(null);
            
            System.out.println("✅ 用户表: " + userCount + " 条");
            System.out.println("✅ 服务类型表: " + serviceCount + " 条");
            System.out.println("✅ 服务人员表: " + workerCount + " 条");
            System.out.println("✅ 预约订单表: " + orderCount + " 条");
            System.out.println("=== 统计完成 ===\n");
        } catch (Exception e) {
            System.err.println("统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 生成测试用户数据
     */
    @Test
    @Transactional
    @Rollback(false)
    public void generateTestUsers() {
        System.out.println("正在生成测试用户数据...");

        String[] usernames = {
            "test_user01", "test_user02", "test_user03", "test_user04", "test_user05",
            "zhang_san", "li_si", "wang_wu", "zhao_liu", "chen_qi"
        };

        String[] nicknames = {
            "小明", "小红", "小李", "小张", "小王",
            "阳光少年", "快乐女孩", "居家达人", "生活小助手", "温暖如春"
        };

        String[] phones = {
            "13812345678", "13987654321", "15612345678", "15987654321", "18612345678",
            "18987654321", "17612345678", "17987654321", "13612345678", "13987654322"
        };

        int successCount = 0;
        for (int i = 0; i < usernames.length; i++) {
            try {
                // 检查用户是否已存在
                Users existingUser = usersMapper.selectByUsername(usernames[i]);
                if (existingUser != null) {
                    System.out.println("用户 " + usernames[i] + " 已存在，跳过创建");
                    continue;
                }

                Users user = new Users();
                user.setUsername(usernames[i]);
                user.setPassword("$2a$10$encrypted_password_hash"); // 模拟加密密码
                user.setPhone(phones[i]);
                user.setNickname(nicknames[i]);
                user.setGender(random.nextInt(3)); // 0-未知 1-男 2-女
                user.setRegisterTime(new Date());
                user.setStatus(1); // 正常状态
                user.setIsVerified(random.nextInt(2)); // 随机认证状态
                user.setRole(0); // 普通用户

                int result = usersMapper.insert(user);
                if (result > 0) {
                    successCount++;
                    System.out.println("✅ 创建测试用户: " + user.getUsername() + " (ID: " + user.getId() + ")");
                } else {
                    System.out.println("❌ 用户创建失败: " + usernames[i]);
                }
            } catch (Exception e) {
                System.err.println("❌ 创建用户 " + usernames[i] + " 失败: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("测试用户数据生成完成！成功创建 " + successCount + " 个用户");
    }

    /**
     * 生成服务类型数据
     */
    @Test
    @Transactional
    @Rollback(false)
    public void generateServiceTypes() {
        System.out.println("正在生成服务类型数据...");

        // 服务类型数据
        Object[][] serviceData = {
            {"cleaning", "家政保洁", "专业保洁团队，深度清洁您的家", "brush-o", new BigDecimal("80.00"), "次", 1, 1},
            {"repair", "维修服务", "水电维修，家具安装，快速上门", "setting-o", new BigDecimal("50.00"), "次", 1, 2},
            {"appliance", "家电维修", "家电故障维修，专业技师服务", "tv-o", new BigDecimal("60.00"), "次", 0, 3},
            {"moving", "搬家服务", "专业搬家团队，安全可靠搬运", "logistics", new BigDecimal("200.00"), "次", 1, 4},
            {"gardening", "园艺服务", "花草养护，园艺设计服务", "flower-o", new BigDecimal("100.00"), "次", 0, 5},
            {"pest", "除虫服务", "专业除虫除螨，安全环保", "delete-o", new BigDecimal("120.00"), "次", 0, 6},
            {"tutoring", "家教服务", "专业家教辅导，提升学习成绩", "book-o", new BigDecimal("150.00"), "小时", 1, 7},
            {"eldercare", "养老护理", "专业护理人员，贴心照顾老人", "user-o", new BigDecimal("180.00"), "小时", 0, 8},
            {"childcare", "育儿服务", "专业育婴师，科学育儿指导", "friends-o", new BigDecimal("160.00"), "小时", 1, 9},
            {"cooking", "厨师服务", "专业厨师上门，制作美味佳肴", "smile-o", new BigDecimal("200.00"), "次", 0, 10}
        };

        int successCount = 0;
        for (Object[] data : serviceData) {
            try {
                // 检查服务类型是否已存在
                QueryWrapper<AppointmentServices> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("service_type", data[0]);
                AppointmentServices existingService = appointmentServicesMapper.selectOne(queryWrapper);
                
                if (existingService != null) {
                    System.out.println("服务类型 " + data[1] + " 已存在，跳过创建");
                    continue;
                }

                AppointmentServices service = new AppointmentServices();
                service.setServiceType((String) data[0]);
                service.setServiceName((String) data[1]);
                service.setDescription((String) data[2]);
                service.setIcon((String) data[3]);
                service.setBasePrice(((BigDecimal) data[4]).setScale(2, RoundingMode.HALF_UP));
                service.setUnit((String) data[5]);
                service.setIsHot((Integer) data[6]);
                service.setSortOrder((Integer) data[7]);
                service.setStatus(1); // 启用状态

                int result = appointmentServicesMapper.insert(service);
                if (result > 0) {
                    successCount++;
                    System.out.println("✅ 创建服务类型: " + service.getServiceName() + " (ID: " + service.getId() + ")");
                } else {
                    System.out.println("❌ 服务类型创建失败: " + data[1]);
                }
            } catch (Exception e) {
                System.err.println("❌ 创建服务类型 " + data[1] + " 失败: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("服务类型数据生成完成！成功创建 " + successCount + " 个服务类型");
    }

    /**
     * 生成服务人员数据
     */
    @Test
    @Transactional
    @Rollback(false)
    public void generateServiceWorkers() {
        System.out.println("正在生成服务人员数据...");

        // 服务人员数据
        Object[][] workerData = {
            // 家政保洁人员
            {"李阿姨", "139****1001", 2, 45, 8, "cleaning", "专业家政保洁，经验丰富，服务细致周到", new BigDecimal("4.8"), 156},
            {"王阿姨", "138****1002", 2, 42, 6, "cleaning", "深度清洁专家，注重细节，客户满意度高", new BigDecimal("4.7"), 128},
            {"张师傅", "137****1003", 1, 38, 10, "cleaning", "商业清洁经验丰富，团队作业效率高", new BigDecimal("4.6"), 203},
            
            // 维修服务人员
            {"刘师傅", "136****2001", 1, 35, 12, "repair,appliance", "水电维修专家，家电维修经验丰富", new BigDecimal("4.9"), 285},
            {"陈师傅", "135****2002", 1, 40, 15, "repair", "装修维修全能手，技术精湛", new BigDecimal("4.8"), 312},
            {"赵师傅", "134****2003", 1, 33, 8, "appliance", "家电维修专业技师，故障诊断准确", new BigDecimal("4.7"), 198},
            
            // 搬家服务人员
            {"孙队长", "133****3001", 1, 42, 10, "moving", "专业搬家团队负责人，服务周到安全", new BigDecimal("4.6"), 89},
            {"马师傅", "132****3002", 1, 38, 7, "moving", "搬家搬运专家，物品保护措施到位", new BigDecimal("4.5"), 156},
            
            // 园艺服务人员
            {"周师傅", "131****4001", 1, 35, 6, "gardening", "园艺设计师，植物养护专家", new BigDecimal("4.5"), 67},
            {"吴师傅", "130****4002", 1, 41, 9, "gardening", "景观设计与维护，绿植搭配专业", new BigDecimal("4.4"), 92},
            
            // 除虫服务人员
            {"郑师傅", "139****5001", 1, 40, 9, "pest", "专业除虫技师，环保安全用药", new BigDecimal("4.4"), 78},
            {"冯师傅", "138****5002", 1, 37, 7, "pest", "害虫防治专家，长期效果保证", new BigDecimal("4.3"), 65},
            
            // 家教服务人员
            {"林老师", "137****6001", 2, 28, 5, "tutoring", "数学专业硕士，中小学辅导经验丰富", new BigDecimal("4.9"), 245},
            {"黄老师", "136****6002", 1, 32, 8, "tutoring", "英语专业八级，口语发音标准", new BigDecimal("4.8"), 198},
            {"杨老师", "135****6003", 2, 30, 6, "tutoring", "物理化学双修，理科辅导专家", new BigDecimal("4.7"), 167},
            
            // 护理服务人员
            {"宋护士", "134****7001", 2, 35, 10, "eldercare,childcare", "专业护理师，老人儿童护理经验丰富", new BigDecimal("4.8"), 123},
            {"田护士", "133****7002", 2, 32, 8, "eldercare", "老年护理专家，康复护理技能娴熟", new BigDecimal("4.6"), 89},
            {"袁阿姨", "132****7003", 2, 40, 12, "childcare", "专业育婴师，科学育儿方法先进", new BigDecimal("4.7"), 156},
            
            // 厨师服务人员
            {"何师傅", "131****8001", 1, 45, 20, "cooking", "酒店主厨出身，中西料理样样精通", new BigDecimal("4.9"), 78},
            {"蒋师傅", "130****8002", 1, 38, 12, "cooking", "家常菜专家，营养搭配合理健康", new BigDecimal("4.7"), 134}
        };

        int successCount = 0;
        for (Object[] data : workerData) {
            try {
                // 检查服务人员是否已存在（根据姓名和电话）
                QueryWrapper<AppointmentWorkers> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("worker_name", data[0]);
                AppointmentWorkers existingWorker = appointmentWorkersMapper.selectOne(queryWrapper);
                
                if (existingWorker != null) {
                    System.out.println("服务人员 " + data[0] + " 已存在，跳过创建");
                    continue;
                }

                AppointmentWorkers worker = new AppointmentWorkers();
                worker.setWorkerName((String) data[0]);
                worker.setWorkerPhone((String) data[1]);
                worker.setGender((Integer) data[2]);
                worker.setAge((Integer) data[3]);
                worker.setExperienceYears((Integer) data[4]);
                worker.setServiceTypes((String) data[5]);
                worker.setSkillDescription((String) data[6]);
                worker.setRating(((BigDecimal) data[7]).setScale(1, RoundingMode.HALF_UP));
                worker.setOrderCount((Integer) data[8]);
                worker.setStatus(1); // 在职状态

                int result = appointmentWorkersMapper.insert(worker);
                if (result > 0) {
                    successCount++;
                    System.out.println("✅ 创建服务人员: " + worker.getWorkerName() + " (ID: " + worker.getId() + ")");
                } else {
                    System.out.println("❌ 服务人员创建失败: " + data[0]);
                }
            } catch (Exception e) {
                System.err.println("❌ 创建服务人员 " + data[0] + " 失败: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("服务人员数据生成完成！成功创建 " + successCount + " 个服务人员");
    }

    /**
     * 生成预约订单数据
     */
    @Test
    @Transactional
    @Rollback(false)
    public void generateAppointmentOrders() {
        System.out.println("正在生成预约订单数据...");

        try {
            // 获取已有的服务类型、服务人员和用户
            List<AppointmentServices> services = appointmentServicesMapper.selectList(null);
            List<AppointmentWorkers> workers = appointmentWorkersMapper.selectList(null);
            List<Users> users = usersMapper.selectList(null);

            System.out.println("数据检查: 服务类型=" + services.size() + ", 服务人员=" + workers.size() + ", 用户=" + users.size());

            if (services.isEmpty() || workers.isEmpty() || users.isEmpty()) {
                System.out.println("❌ 请先生成服务类型、服务人员和用户数据！");
                System.out.println("建议执行顺序: generateTestUsers() -> generateServiceTypes() -> generateServiceWorkers() -> generateAppointmentOrders()");
                return;
            }

        // 订单状态：0-待确认 1-已确认 2-服务中 3-已完成 4-已取消
        Integer[] statuses = {0, 1, 2, 3, 4};
        String[] statusNames = {"待确认", "已确认", "服务中", "已完成", "已取消"};

        // 联系人姓名池
        String[] contactNames = {
            "张先生", "李女士", "王先生", "赵女士", "陈先生", 
            "刘女士", "杨先生", "黄女士", "周先生", "吴女士"
        };

        // 地址池
        String[] addresses = {
            "阳光小区1号楼1单元101室", "温馨家园2号楼3单元502室", "绿色花园3号楼2单元203室",
            "幸福社区5号楼4单元801室", "和谐小区7号楼1单元301室", "美好家园8号楼5单元701室",
            "花园新城10号楼2单元405室", "梦想家园12号楼3单元602室", "星光小区15号楼1单元908室",
            "月亮湾小区18号楼4单元1203室", "阳光花园20号楼2单元306室", "温馨港湾22号楼5单元1105室"
        };

        // 特殊要求池
        String[] requirementsList = {
            "请提前电话联系", "需要带专业工具", "请准时到达", "家里有小孩，请轻声",
            "楼下没有电梯，需要爬楼", "需要开发票", "请穿鞋套进入", "家里有宠物，请注意",
            "老人在家，请耐心服务", "时间可能会延长，请做好准备", null, null
        };

        // 取消原因池
        String[] cancelReasons = {
            "临时有事，无法按时服务", "服务人员生病", "客户改变需求", 
            "天气原因", "其他紧急事务", null, null, null
        };

        // 评价内容池
        String[] comments = {
            "服务态度很好，技术专业，很满意！", "准时到达，服务质量不错，推荐！",
            "师傅很负责任，解决了问题，点赞！", "价格合理，服务到位，下次还会选择！",
            "专业水平高，服务效率快，非常好！", "态度友善，工作认真，值得信赖！",
            null, null, null, null
        };

            // 生成50个订单
            int successCount = 0;
            for (int i = 0; i < 50; i++) {
                try {
                    AppointmentOrders order = new AppointmentOrders();

                    // 随机选择用户、服务和服务人员
                    Users user = users.get(random.nextInt(users.size()));
                    AppointmentServices service = services.get(random.nextInt(services.size()));
                    
                    // 根据服务类型匹配服务人员
                    AppointmentWorkers worker = findWorkerByServiceType(workers, service.getServiceType());
                    if (worker == null) {
                        worker = workers.get(random.nextInt(workers.size()));
                    }

                    // 生成订单编号
                    String orderNo = "APT" + System.currentTimeMillis() + String.format("%03d", i);
                    
                    // 生成预约时间（未来7-30天内）
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime appointmentTime = now.plusDays(random.nextInt(23) + 7)
                            .withHour(8 + random.nextInt(12))
                            .withMinute(random.nextInt(2) * 30)
                            .withSecond(0)
                            .withNano(0);

                    // 随机状态
                    Integer status = statuses[random.nextInt(statuses.length)];

                    order.setOrderNo(orderNo);
                    order.setUserId(user.getId());
                    order.setServiceId(service.getId());
                    order.setServiceType(service.getServiceType());
                    order.setServiceName(service.getServiceName());
                    order.setWorkerId(worker.getId());
                    order.setWorkerName(worker.getWorkerName());
                    order.setWorkerPhone(worker.getWorkerPhone());
                    order.setAppointmentTime(Date.from(appointmentTime.atZone(ZoneId.systemDefault()).toInstant()));
                    order.setAddress(addresses[random.nextInt(addresses.length)]);
                    order.setContactName(contactNames[random.nextInt(contactNames.length)]);
                    order.setContactPhone("138****" + String.format("%04d", random.nextInt(10000)));
                    order.setRequirements(requirementsList[random.nextInt(requirementsList.length)]);
                    
                    // 生成价格
                    BigDecimal basePrice = service.getBasePrice();
                    order.setEstimatedPrice(basePrice.multiply(BigDecimal.valueOf(0.8 + random.nextDouble() * 0.4))
                            .setScale(2, RoundingMode.HALF_UP)); // 基础价格 ± 20%
                    
                    if (status >= 3) { // 已完成或已取消的订单设置实际价格
                        order.setActualPrice(basePrice.multiply(BigDecimal.valueOf(0.9 + random.nextDouble() * 0.2))
                                .setScale(2, RoundingMode.HALF_UP));
                    }

                    order.setStatus(status);

                    // 已取消的订单设置取消原因
                    if (status == 4) {
                        order.setCancelReason(cancelReasons[random.nextInt(3)]);
                    }

                    // 已完成的订单设置评分和评价
                    if (status == 3) {
                        order.setRating(4 + random.nextInt(2)); // 4-5分
                        if (random.nextBoolean()) {
                            order.setComment(comments[random.nextInt(6)]);
                        }
                    }

                    int result = appointmentOrdersMapper.insert(order);
                    if (result > 0) {
                        successCount++;
                        System.out.println("✅ 创建预约订单: " + order.getOrderNo() + 
                                " (" + service.getServiceName() + " - " + statusNames[status] + ")");
                    } else {
                        System.out.println("❌ 订单创建失败: " + orderNo);
                    }
                } catch (Exception e) {
                    System.err.println("❌ 创建订单失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("预约订单数据生成完成！成功创建 " + successCount + " 个订单");
            
        } catch (Exception e) {
            System.err.println("❌ 预约订单数据生成失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据服务类型查找匹配的服务人员
     */
    private AppointmentWorkers findWorkerByServiceType(List<AppointmentWorkers> workers, String serviceType) {
        for (AppointmentWorkers worker : workers) {
            if (worker.getServiceTypes() != null && 
                Arrays.asList(worker.getServiceTypes().split(",")).contains(serviceType)) {
                return worker;
            }
        }
        return null;
    }

    // ==================== 持久化版本方法（不使用事务，确保数据真正保存） ====================

    /**
     * 生成测试用户数据 - 持久化版本
     */
    public void generateTestUsersPersistent() {
        System.out.println("正在生成测试用户数据（持久化）...");

        String[] usernames = {
            "test_user01", "test_user02", "test_user03", "test_user04", "test_user05",
            "zhang_san", "li_si", "wang_wu", "zhao_liu", "chen_qi"
        };

        String[] nicknames = {
            "测试用户01", "测试用户02", "测试用户03", "测试用户04", "测试用户05", 
            "张三", "李四", "王五", "赵六", "陈七"
        };

        int successCount = 0;
        for (int i = 0; i < usernames.length; i++) {
            try {
                // 检查用户是否已存在
                Users existingUser = usersMapper.selectByUsername(usernames[i]);
                if (existingUser != null) {
                    System.out.println("⚠️ 用户已存在，跳过: " + usernames[i]);
                    continue;
                }

                Users user = new Users();
                user.setUsername(usernames[i]);
                user.setNickname(nicknames[i]);
                // Users 实体无 email 字段
                user.setPhone("138" + String.format("%08d", random.nextInt(100000000)));
                user.setGender(random.nextInt(2));
                // 0-普通用户 1-管理员
                user.setRole(0);
                user.setStatus(1);

                int result = usersMapper.insert(user);
                if (result > 0) {
                    successCount++;
                    System.out.println("✅ 创建用户: " + user.getUsername() + " (" + user.getNickname() + ")");
                } else {
                    System.out.println("❌ 用户创建失败: " + usernames[i]);
                }
            } catch (Exception e) {
                System.err.println("❌ 创建用户失败: " + usernames[i] + " - " + e.getMessage());
            }
        }

        System.out.println("测试用户数据生成完成！成功创建 " + successCount + " 个用户\n");
    }

    /**
     * 生成服务类型数据 - 持久化版本
     */
    public void generateServiceTypesPersistent() {
        System.out.println("正在生成服务类型数据（持久化）...");

        Object[][] serviceData = {
            {"cleaning", "家政保洁", "专业保洁团队，深度清洁您的家", "brush-o", new BigDecimal("80.00"), "次", 1, 1},
            {"repair", "维修服务", "专业维修师傅，解决家居维修问题", "tool", new BigDecimal("120.00"), "次", 1, 1},
            {"install", "安装服务", "家具家电安装，专业快捷", "cog", new BigDecimal("100.00"), "次", 1, 1},
            {"move", "搬家服务", "专业搬家团队，安全快捷", "truck", new BigDecimal("200.00"), "次", 1, 1},
            {"elderly_care", "居家养老", "专业护理人员，贴心照顾老人", "heart-o", new BigDecimal("150.00"), "小时", 1, 1},
            {"baby_care", "月嫂服务", "专业月嫂，产后母婴护理", "child", new BigDecimal("300.00"), "天", 1, 1},
            {"tutor", "家教服务", "专业教师，一对一辅导", "graduation-cap", new BigDecimal("100.00"), "小时", 1, 1},
            {"cooking", "上门做饭", "专业厨师，营养美味", "cutlery", new BigDecimal("80.00"), "餐", 1, 1},
            {"gardening", "花园养护", "专业园艺，花园美化", "leaf", new BigDecimal("60.00"), "次", 1, 1},
            {"pet_care", "宠物照护", "专业宠物护理，让您安心", "paw", new BigDecimal("50.00"), "天", 1, 1}
        };

        int successCount = 0;
        for (Object[] data : serviceData) {
            try {
                String serviceType = (String) data[0];
                
                // 检查服务类型是否已存在
                QueryWrapper<AppointmentServices> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("service_type", serviceType);
                AppointmentServices existing = appointmentServicesMapper.selectOne(queryWrapper);
                
                if (existing != null) {
                    System.out.println("⚠️ 服务类型已存在，跳过: " + serviceType);
                    continue;
                }

                AppointmentServices service = new AppointmentServices();
                service.setServiceType(serviceType);
                service.setServiceName((String) data[1]);
                service.setDescription((String) data[2]);
                service.setIcon((String) data[3]);
                service.setBasePrice((BigDecimal) data[4]);
                service.setUnit((String) data[5]);
                service.setIsHot((Integer) data[6]);
                service.setStatus((Integer) data[7]);

                int result = appointmentServicesMapper.insert(service);
                if (result > 0) {
                    successCount++;
                    System.out.println("✅ 创建服务类型: " + service.getServiceName());
                } else {
                    System.out.println("❌ 服务类型创建失败: " + service.getServiceName());
                }
            } catch (Exception e) {
                System.err.println("❌ 创建服务类型失败: " + data[1] + " - " + e.getMessage());
            }
        }

        System.out.println("服务类型数据生成完成！成功创建 " + successCount + " 种服务\n");
    }

    /**
     * 生成服务人员数据 - 持久化版本
     */
    public void generateServiceWorkersPersistent() {
        System.out.println("正在生成服务人员数据（持久化）...");

        Object[][] workerData = {
            {"cleaning", "王阿姨", "female", "138****1001", "5年家政经验，专业保洁", new BigDecimal("4.8")},
            {"cleaning", "李师傅", "male", "138****1002", "10年保洁经验，服务周到", new BigDecimal("4.9")},
            {"repair", "张师傅", "male", "138****2001", "15年维修经验，技术精湛", new BigDecimal("4.9")},
            {"repair", "刘师傅", "male", "138****2002", "水电维修专家，服务迅速", new BigDecimal("4.7")},
            {"install", "陈师傅", "male", "138****3001", "家具安装专家，经验丰富", new BigDecimal("4.8")},
            {"install", "赵师傅", "male", "138****3002", "家电安装专家，服务周到", new BigDecimal("4.6")},
            {"move", "搬家小队长", "male", "138****4001", "专业搬家团队，安全可靠", new BigDecimal("4.8")},
            {"move", "货运达人", "male", "138****4002", "大型搬家专家，设备齐全", new BigDecimal("4.7")},
            {"elderly_care", "护理阿姨", "female", "138****5001", "专业护理证书，温柔体贴", new BigDecimal("4.9")},
            {"elderly_care", "康复师", "female", "138****5002", "康复专业，护理经验丰富", new BigDecimal("4.8")},
            {"baby_care", "金牌月嫂", "female", "138****6001", "资深月嫂，母婴护理专家", new BigDecimal("5.0")},
            {"baby_care", "专业月嫂", "female", "138****6002", "经验丰富，产后护理专业", new BigDecimal("4.9")},
            {"tutor", "数学老师", "female", "138****7001", "数学专业，教学经验10年", new BigDecimal("4.8")},
            {"tutor", "英语老师", "male", "138****7002", "英语专业，口语纯正", new BigDecimal("4.7")},
            {"cooking", "家庭厨师", "female", "138****8001", "营养搭配专家，味道一流", new BigDecimal("4.9")},
            {"cooking", "私人厨师", "male", "138****8002", "酒店厨师出身，技艺精湛", new BigDecimal("4.8")},
            {"gardening", "园艺师", "male", "138****9001", "园艺专业，植物养护专家", new BigDecimal("4.7")},
            {"gardening", "花艺师", "female", "138****9002", "花卉种植专家，美化环境", new BigDecimal("4.8")},
            {"pet_care", "宠物护理师", "female", "138****0001", "宠物医学专业，爱心满满", new BigDecimal("4.9")},
            {"pet_care", "训犬师", "male", "138****0002", "宠物训练专家，行为矫正", new BigDecimal("4.6")}
        };

        int successCount = 0;
        for (Object[] data : workerData) {
            try {
                String workerName = (String) data[1];
                
                // 检查服务人员是否已存在
                QueryWrapper<AppointmentWorkers> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("worker_name", workerName);
                AppointmentWorkers existing = appointmentWorkersMapper.selectOne(queryWrapper);
                
                if (existing != null) {
                    System.out.println("⚠️ 服务人员已存在，跳过: " + workerName);
                    continue;
                }

                AppointmentWorkers worker = new AppointmentWorkers();
                // AppointmentWorkers 使用 serviceTypes (逗号分隔)
                worker.setServiceTypes((String) data[0]);
                worker.setWorkerName(workerName);
                // 性别：0-未知 1-男 2-女
                String genderStr = (String) data[2];
                Integer genderVal = "male".equalsIgnoreCase(genderStr) ? 1 : ("female".equalsIgnoreCase(genderStr) ? 2 : 0);
                worker.setGender(genderVal);
                worker.setWorkerPhone((String) data[3]);
                worker.setSkillDescription((String) data[4]);
                worker.setRating((BigDecimal) data[5]);
                worker.setStatus(1);

                int result = appointmentWorkersMapper.insert(worker);
                if (result > 0) {
                    successCount++;
                    System.out.println("✅ 创建服务人员: " + worker.getWorkerName() + " (" + worker.getServiceTypes() + ")");
                } else {
                    System.out.println("❌ 服务人员创建失败: " + workerName);
                }
            } catch (Exception e) {
                System.err.println("❌ 创建服务人员失败: " + data[1] + " - " + e.getMessage());
            }
        }

        System.out.println("服务人员数据生成完成！成功创建 " + successCount + " 个服务人员\n");
    }

    /**
     * 生成预约订单数据 - 持久化版本
     */
    public void generateAppointmentOrdersPersistent() {
        System.out.println("正在生成预约订单数据（持久化）...");

        try {
            // 获取已有的服务类型、服务人员和用户
            List<AppointmentServices> services = appointmentServicesMapper.selectList(null);
            List<AppointmentWorkers> workers = appointmentWorkersMapper.selectList(null);
            List<Users> users = usersMapper.selectList(null);

            if (services.isEmpty()) {
                System.out.println("❌ 没有找到服务类型数据，请先生成服务类型");
                return;
            }
            if (workers.isEmpty()) {
                System.out.println("❌ 没有找到服务人员数据，请先生成服务人员");
                return;
            }
            if (users.isEmpty()) {
                System.out.println("❌ 没有找到用户数据，请先生成用户");
                return;
            }

            // 订单状态 (0:待确认, 1:已确认, 2:服务中, 3:已完成, 4:已取消)
            Integer[] statuses = {0, 1, 2, 3, 4};
            String[] statusNames = {"待确认", "已确认", "服务中", "已完成", "已取消"};

            String[] addresses = {
                "北京市朝阳区三里屯SOHO", "上海市浦东新区陆家嘴金融中心",
                "广州市天河区珠江新城", "深圳市南山区科技园",
                "杭州市西湖区文三路", "成都市高新区天府大道",
                "武汉市江汉区中山大道", "南京市鼓楼区中山路",
                "西安市雁塔区高新一路", "重庆市渝中区解放碑"
            };

            String[] contactNames = {"张先生", "李女士", "王先生", "赵女士", "陈先生", "刘女士", "杨先生", "周女士"};

            String[] requirementsList = {
                "服务时间要准时", "需要自带工具", "家里有宠物，请注意",
                "老人在家，请轻声", "需要发票", "有小孩，注意安全",
                "楼层较高，请做好准备", "停车不便，建议公共交通"
            };

            String[] cancelReasons = {"时间冲突", "临时有事", "价格问题"};

            String[] comments = {
                "服务很满意，师傅很专业！", "态度很好，下次还会选择！",
                "师傅很负责任，解决了问题，点赞！", "价格合理，服务到位，下次还会选择！",
                "专业水平高，服务效率快，非常好！", "态度友善，工作认真，值得信赖！"
            };

            int successCount = 0;
            for (int i = 0; i < 50; i++) {
                try {
                    AppointmentOrders order = new AppointmentOrders();

                    // 随机选择用户、服务和服务人员
                    Users user = users.get(random.nextInt(users.size()));
                    AppointmentServices service = services.get(random.nextInt(services.size()));
                    
                    // 根据服务类型匹配服务人员
                    AppointmentWorkers worker = findWorkerByServiceType(workers, service.getServiceType());
                    if (worker == null) {
                        worker = workers.get(random.nextInt(workers.size()));
                    }

                    // 生成订单编号
                    String orderNo = "APT" + System.currentTimeMillis() + String.format("%03d", i);
                    
                    // 生成预约时间（未来7-30天内）
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime appointmentTime = now.plusDays(random.nextInt(23) + 7)
                            .withHour(8 + random.nextInt(12))
                            .withMinute(random.nextInt(2) * 30)
                            .withSecond(0)
                            .withNano(0);

                    // 随机状态
                    Integer status = statuses[random.nextInt(statuses.length)];

                    order.setOrderNo(orderNo);
                    order.setUserId(user.getId());
                    order.setServiceId(service.getId());
                    order.setServiceType(service.getServiceType());
                    order.setServiceName(service.getServiceName());
                    order.setWorkerId(worker.getId());
                    order.setWorkerName(worker.getWorkerName());
                    order.setWorkerPhone(worker.getWorkerPhone());
                    order.setAppointmentTime(Date.from(appointmentTime.atZone(ZoneId.systemDefault()).toInstant()));
                    order.setAddress(addresses[random.nextInt(addresses.length)]);
                    order.setContactName(contactNames[random.nextInt(contactNames.length)]);
                    order.setContactPhone("138****" + String.format("%04d", random.nextInt(10000)));
                    order.setRequirements(requirementsList[random.nextInt(requirementsList.length)]);
                    
                    // 生成价格
                    BigDecimal basePrice = service.getBasePrice();
                    order.setEstimatedPrice(basePrice.multiply(BigDecimal.valueOf(0.8 + random.nextDouble() * 0.4))
                            .setScale(2, RoundingMode.HALF_UP)); // 基础价格 ± 20%
                    
                    if (status >= 3) { // 已完成或已取消的订单设置实际价格
                        order.setActualPrice(basePrice.multiply(BigDecimal.valueOf(0.9 + random.nextDouble() * 0.2))
                                .setScale(2, RoundingMode.HALF_UP));
                    }

                    order.setStatus(status);

                    // 已取消的订单设置取消原因
                    if (status == 4) {
                        order.setCancelReason(cancelReasons[random.nextInt(3)]);
                    }

                    // 已完成的订单设置评分和评价
                    if (status == 3) {
                        order.setRating(4 + random.nextInt(2)); // 4-5分
                        if (random.nextBoolean()) {
                            order.setComment(comments[random.nextInt(6)]);
                        }
                    }

                    int result = appointmentOrdersMapper.insert(order);
                    if (result > 0) {
                        successCount++;
                        System.out.println("✅ 创建预约订单: " + order.getOrderNo() + 
                                " (" + service.getServiceName() + " - " + statusNames[status] + ")");
                    } else {
                        System.out.println("❌ 订单创建失败: " + orderNo);
                    }
                } catch (Exception e) {
                    System.err.println("❌ 创建订单失败: " + e.getMessage());
                }
            }

            System.out.println("预约订单数据生成完成！成功创建 " + successCount + " 个订单\n");
            
        } catch (Exception e) {
            System.err.println("❌ 预约订单数据生成失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 清理所有测试数据
     */
    @Test
    @Transactional
    @Rollback(false)
    public void clearAllTestData() {
        System.out.println("开始清理测试数据...");
        
        try {
            // 清理订单数据
            long orderCount = appointmentOrdersMapper.selectCount(null);
            if (orderCount > 0) {
                appointmentOrdersMapper.delete(null);
                System.out.println("✅ 清理预约订单数据完成 (" + orderCount + " 条)");
            } else {
                System.out.println("订单表已经为空");
            }
            
            // 清理服务人员数据
            long workerCount = appointmentWorkersMapper.selectCount(null);
            if (workerCount > 0) {
                appointmentWorkersMapper.delete(null);
                System.out.println("✅ 清理服务人员数据完成 (" + workerCount + " 条)");
            } else {
                System.out.println("服务人员表已经为空");
            }
            
            // 清理服务类型数据
            long serviceCount = appointmentServicesMapper.selectCount(null);
            if (serviceCount > 0) {
                appointmentServicesMapper.delete(null);
                System.out.println("✅ 清理服务类型数据完成 (" + serviceCount + " 条)");
            } else {
                System.out.println("服务类型表已经为空");
            }
            
            // 清理测试用户数据（只清理测试用户）
            int userDeleteCount = usersMapper.deleteByUsername("test_user%");
            if (userDeleteCount > 0) {
                System.out.println("✅ 清理测试用户数据完成 (" + userDeleteCount + " 条)");
            } else {
                System.out.println("没有测试用户需要清理");
            }
            
            System.out.println("✅ 所有测试数据清理完成！");
            
            // 显示清理后的统计
            showDataStatistics();
            
        } catch (Exception e) {
            System.err.println("❌ 清理测试数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
