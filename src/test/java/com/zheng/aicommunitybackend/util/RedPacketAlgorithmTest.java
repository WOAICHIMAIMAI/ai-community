package com.zheng.aicommunitybackend.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 红包分配算法测试
 */
@SpringBootTest
public class RedPacketAlgorithmTest {

    @Test
    public void testDoubleAverageAlgorithm() {
        // 测试二倍均值法
        Long totalAmount = 10000L; // 100元
        Integer count = 10;
        Long minAmount = 1L;
        
        List<Long> amounts = RedPacketAlgorithm.allocateByDoubleAverage(totalAmount, count, minAmount);
        
        // 验证数量
        assertEquals(count.intValue(), amounts.size());
        
        // 验证总金额
        long sum = amounts.stream().mapToLong(Long::longValue).sum();
        assertEquals(totalAmount.longValue(), sum);
        
        // 验证最小金额
        long min = amounts.stream().mapToLong(Long::longValue).min().orElse(0);
        assertTrue(min >= minAmount);
        
        // 打印结果
        System.out.println("二倍均值法分配结果:");
        for (int i = 0; i < amounts.size(); i++) {
            System.out.printf("红包%d: %d分 (%.2f元)%n", i + 1, amounts.get(i), amounts.get(i) / 100.0);
        }
        System.out.printf("总计: %d分 (%.2f元)%n", sum, sum / 100.0);
    }

    @Test
    public void testRandomAlgorithm() {
        // 测试随机分配法
        Long totalAmount = 5000L; // 50元
        Integer count = 5;
        Long minAmount = 1L;
        
        List<Long> amounts = RedPacketAlgorithm.allocateByRandom(totalAmount, count, minAmount);
        
        // 验证数量
        assertEquals(count.intValue(), amounts.size());
        
        // 验证总金额
        long sum = amounts.stream().mapToLong(Long::longValue).sum();
        assertEquals(totalAmount.longValue(), sum);
        
        // 验证最小金额
        long min = amounts.stream().mapToLong(Long::longValue).min().orElse(0);
        assertTrue(min >= minAmount);
        
        // 打印结果
        System.out.println("随机分配法结果:");
        for (int i = 0; i < amounts.size(); i++) {
            System.out.printf("红包%d: %d分 (%.2f元)%n", i + 1, amounts.get(i), amounts.get(i) / 100.0);
        }
        System.out.printf("总计: %d分 (%.2f元)%n", sum, sum / 100.0);
    }

    @Test
    public void testEvenlyAlgorithm() {
        // 测试均匀分配法
        Long totalAmount = 1000L; // 10元
        Integer count = 3;
        
        List<Long> amounts = RedPacketAlgorithm.allocateEvenly(totalAmount, count);
        
        // 验证数量
        assertEquals(count.intValue(), amounts.size());
        
        // 验证总金额
        long sum = amounts.stream().mapToLong(Long::longValue).sum();
        assertEquals(totalAmount.longValue(), sum);
        
        // 打印结果
        System.out.println("均匀分配法结果:");
        for (int i = 0; i < amounts.size(); i++) {
            System.out.printf("红包%d: %d分 (%.2f元)%n", i + 1, amounts.get(i), amounts.get(i) / 100.0);
        }
        System.out.printf("总计: %d分 (%.2f元)%n", sum, sum / 100.0);
    }

    @Test
    public void testAllocateByName() {
        // 测试通过算法名称分配
        Long totalAmount = 2000L; // 20元
        Integer count = 4;
        Long minAmount = 1L;
        
        // 测试二倍均值法
        List<Long> doubleAvgAmounts = RedPacketAlgorithm.allocate("DOUBLE_AVERAGE", totalAmount, count, minAmount);
        assertEquals(count.intValue(), doubleAvgAmounts.size());
        assertEquals(totalAmount.longValue(), doubleAvgAmounts.stream().mapToLong(Long::longValue).sum());
        
        // 测试随机分配法
        List<Long> randomAmounts = RedPacketAlgorithm.allocate("RANDOM", totalAmount, count, minAmount);
        assertEquals(count.intValue(), randomAmounts.size());
        assertEquals(totalAmount.longValue(), randomAmounts.stream().mapToLong(Long::longValue).sum());
        
        // 测试均匀分配法
        List<Long> evenlyAmounts = RedPacketAlgorithm.allocate("EVENLY", totalAmount, count, minAmount);
        assertEquals(count.intValue(), evenlyAmounts.size());
        assertEquals(totalAmount.longValue(), evenlyAmounts.stream().mapToLong(Long::longValue).sum());
        
        // 测试未知算法（应该使用默认的二倍均值法）
        List<Long> unknownAmounts = RedPacketAlgorithm.allocate("UNKNOWN", totalAmount, count, minAmount);
        assertEquals(count.intValue(), unknownAmounts.size());
        assertEquals(totalAmount.longValue(), unknownAmounts.stream().mapToLong(Long::longValue).sum());
    }

    @Test
    public void testEdgeCases() {
        // 测试边界情况
        
        // 只有1个红包
        List<Long> singlePacket = RedPacketAlgorithm.allocateByDoubleAverage(100L, 1, 1L);
        assertEquals(1, singlePacket.size());
        assertEquals(100L, singlePacket.get(0).longValue());
        
        // 最小金额等于平均金额
        List<Long> minEqualAvg = RedPacketAlgorithm.allocateByDoubleAverage(500L, 5, 100L);
        assertEquals(5, minEqualAvg.size());
        assertEquals(500L, minEqualAvg.stream().mapToLong(Long::longValue).sum());
        assertTrue(minEqualAvg.stream().allMatch(amount -> amount >= 100L));
    }

    @Test
    public void testInvalidParameters() {
        // 测试无效参数
        
        // 总金额不足
        assertThrows(IllegalArgumentException.class, () -> {
            RedPacketAlgorithm.allocateByDoubleAverage(100L, 5, 50L);
        });
        
        // 数量为0
        assertThrows(IllegalArgumentException.class, () -> {
            RedPacketAlgorithm.allocateByDoubleAverage(100L, 0, 1L);
        });
        
        // 总金额为0
        assertThrows(IllegalArgumentException.class, () -> {
            RedPacketAlgorithm.allocateByDoubleAverage(0L, 5, 1L);
        });
        
        // null参数
        assertThrows(IllegalArgumentException.class, () -> {
            RedPacketAlgorithm.allocateByDoubleAverage(null, 5, 1L);
        });
    }
}
