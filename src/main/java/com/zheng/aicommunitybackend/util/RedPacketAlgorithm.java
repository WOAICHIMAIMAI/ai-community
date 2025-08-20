package com.zheng.aicommunitybackend.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 红包分配算法工具类
 */
@Slf4j
public class RedPacketAlgorithm {

    /**
     * 二倍均值法分配红包
     * 这是微信红包使用的算法，保证每个红包金额在 [0.01, 2*平均值] 范围内
     * 
     * @param totalAmount 总金额（分）
     * @param count 红包数量
     * @param minAmount 最小金额（分）
     * @return 红包金额列表（分）
     */
    public static List<Long> allocateByDoubleAverage(Long totalAmount, Integer count, Long minAmount) {
        if (totalAmount == null || count == null || count <= 0 || totalAmount <= 0) {
            throw new IllegalArgumentException("参数不能为空且必须大于0");
        }
        
        if (minAmount == null || minAmount <= 0) {
            minAmount = 1L; // 默认最小1分
        }
        
        // 检查总金额是否足够分配最小金额
        if (totalAmount < count * minAmount) {
            throw new IllegalArgumentException("总金额不足以分配最小金额");
        }
        
        List<Long> amounts = new ArrayList<>();
        long remaining = totalAmount;
        
        for (int i = 0; i < count - 1; i++) {
            // 计算剩余红包的平均值
            long avgAmount = remaining / (count - i);
            
            // 计算当前红包的最大值（二倍均值法）
            long maxAmount = Math.min(avgAmount * 2, remaining - (count - i - 1) * minAmount);
            
            // 确保最大值不小于最小值
            maxAmount = Math.max(maxAmount, minAmount);
            
            // 随机生成当前红包金额
            long currentAmount;
            if (maxAmount == minAmount) {
                currentAmount = minAmount;
            } else {
                currentAmount = ThreadLocalRandom.current().nextLong(minAmount, maxAmount + 1);
            }
            
            amounts.add(currentAmount);
            remaining -= currentAmount;
        }
        
        // 最后一个红包是剩余的所有金额
        amounts.add(remaining);
        
        // 验证分配结果
        validateAllocation(amounts, totalAmount, count, minAmount);
        
        log.info("红包分配完成，总金额: {}分, 数量: {}, 最小金额: {}分", totalAmount, count, minAmount);
        return amounts;
    }

    /**
     * 随机分配红包
     * 简单的随机分配算法，适用于对公平性要求不高的场景
     * 
     * @param totalAmount 总金额（分）
     * @param count 红包数量
     * @param minAmount 最小金额（分）
     * @return 红包金额列表（分）
     */
    public static List<Long> allocateByRandom(Long totalAmount, Integer count, Long minAmount) {
        if (totalAmount == null || count == null || count <= 0 || totalAmount <= 0) {
            throw new IllegalArgumentException("参数不能为空且必须大于0");
        }
        
        if (minAmount == null || minAmount <= 0) {
            minAmount = 1L;
        }
        
        if (totalAmount < count * minAmount) {
            throw new IllegalArgumentException("总金额不足以分配最小金额");
        }
        
        List<Long> amounts = new ArrayList<>();
        
        // 先给每个红包分配最小金额
        for (int i = 0; i < count; i++) {
            amounts.add(minAmount);
        }
        
        // 剩余金额随机分配
        long remainingAmount = totalAmount - count * minAmount;
        
        while (remainingAmount > 0) {
            int randomIndex = ThreadLocalRandom.current().nextInt(count);
            long addAmount = Math.min(remainingAmount, ThreadLocalRandom.current().nextLong(1, remainingAmount + 1));
            amounts.set(randomIndex, amounts.get(randomIndex) + addAmount);
            remainingAmount -= addAmount;
        }
        
        // 打乱顺序
        Collections.shuffle(amounts);
        
        validateAllocation(amounts, totalAmount, count, minAmount);
        
        log.info("随机红包分配完成，总金额: {}分, 数量: {}, 最小金额: {}分", totalAmount, count, minAmount);
        return amounts;
    }

    /**
     * 均匀分配红包
     * 尽可能平均分配，适用于公平性要求很高的场景
     * 
     * @param totalAmount 总金额（分）
     * @param count 红包数量
     * @return 红包金额列表（分）
     */
    public static List<Long> allocateEvenly(Long totalAmount, Integer count) {
        if (totalAmount == null || count == null || count <= 0 || totalAmount <= 0) {
            throw new IllegalArgumentException("参数不能为空且必须大于0");
        }
        
        List<Long> amounts = new ArrayList<>();
        
        // 计算平均金额
        long avgAmount = totalAmount / count;
        long remainder = totalAmount % count;
        
        // 分配平均金额
        for (int i = 0; i < count; i++) {
            amounts.add(avgAmount);
        }
        
        // 将余数随机分配给部分红包
        for (int i = 0; i < remainder; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(count);
            amounts.set(randomIndex, amounts.get(randomIndex) + 1);
        }
        
        // 打乱顺序
        Collections.shuffle(amounts);
        
        validateAllocation(amounts, totalAmount, count, 1L);
        
        log.info("均匀红包分配完成，总金额: {}分, 数量: {}", totalAmount, count);
        return amounts;
    }

    /**
     * 验证红包分配结果
     * 
     * @param amounts 分配的金额列表
     * @param expectedTotal 期望的总金额
     * @param expectedCount 期望的数量
     * @param minAmount 最小金额
     */
    private static void validateAllocation(List<Long> amounts, Long expectedTotal, Integer expectedCount, Long minAmount) {
        if (amounts.size() != expectedCount) {
            throw new RuntimeException("红包数量不匹配，期望: " + expectedCount + ", 实际: " + amounts.size());
        }
        
        long actualTotal = amounts.stream().mapToLong(Long::longValue).sum();
        if (actualTotal != expectedTotal) {
            throw new RuntimeException("红包总金额不匹配，期望: " + expectedTotal + ", 实际: " + actualTotal);
        }
        
        for (Long amount : amounts) {
            if (amount < minAmount) {
                throw new RuntimeException("存在小于最小金额的红包，最小金额: " + minAmount + ", 实际: " + amount);
            }
        }
        
        log.debug("红包分配验证通过，总金额: {}, 数量: {}, 最小金额: {}, 最大金额: {}, 平均金额: {}", 
                expectedTotal, expectedCount, amounts.stream().mapToLong(Long::longValue).min().orElse(0),
                amounts.stream().mapToLong(Long::longValue).max().orElse(0),
                amounts.stream().mapToLong(Long::longValue).average().orElse(0));
    }

    /**
     * 根据算法名称分配红包
     * 
     * @param algorithm 算法名称
     * @param totalAmount 总金额（分）
     * @param count 红包数量
     * @param minAmount 最小金额（分）
     * @return 红包金额列表（分）
     */
    public static List<Long> allocate(String algorithm, Long totalAmount, Integer count, Long minAmount) {
        if (algorithm == null || algorithm.trim().isEmpty()) {
            algorithm = "DOUBLE_AVERAGE";
        }
        
        switch (algorithm.toUpperCase()) {
            case "DOUBLE_AVERAGE":
                return allocateByDoubleAverage(totalAmount, count, minAmount);
            case "RANDOM":
                return allocateByRandom(totalAmount, count, minAmount);
            case "EVENLY":
                return allocateEvenly(totalAmount, count);
            default:
                log.warn("未知的红包分配算法: {}, 使用默认的二倍均值法", algorithm);
                return allocateByDoubleAverage(totalAmount, count, minAmount);
        }
    }
}
