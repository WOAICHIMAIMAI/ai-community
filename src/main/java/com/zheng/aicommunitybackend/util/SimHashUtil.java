package com.zheng.aicommunitybackend.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SimHash算法工具类
 * 用于计算文本的指纹并进行相似度比较
 */
public class SimHashUtil {

    // 分词的正则表达式，简单实现，实际项目中可以使用专业分词库如HanLP
    private static final Pattern PATTERN = Pattern.compile("[\u4e00-\u9fa5]+|[a-zA-Z]+");
    
    // 汉明距离阈值，小于等于此值认为相似
    private static final int HAMMING_DIST_THRESHOLD = 3;
    
    // 指纹位数
    private static final int HASH_BITS = 64;

    /**
     * 计算文本的SimHash值
     * @param text 文本内容
     * @return SimHash值的十六进制字符串表示
     */
    public static String simHash(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        
        // 分词并计算权重
        Map<String, Integer> wordWeights = tokenizeAndCount(text);
        
        // 初始化特征向量
        int[] featureVector = new int[HASH_BITS];
        
        // 计算特征向量
        for (Map.Entry<String, Integer> entry : wordWeights.entrySet()) {
            String word = entry.getKey();
            int weight = entry.getValue();
            
            // 计算词的hash值
            String wordHash = DigestUtils.md5Hex(word);
            BigInteger bigInt = new BigInteger(wordHash, 16);
            
            // 将hash值转换为二进制字符串
            StringBuilder binaryHash = new StringBuilder(bigInt.toString(2));
            while (binaryHash.length() < HASH_BITS) {
                binaryHash.insert(0, "0");
            }
            if (binaryHash.length() > HASH_BITS) {
                binaryHash = new StringBuilder(binaryHash.substring(0, HASH_BITS));
            }
            
            // 更新特征向量
            for (int i = 0; i < HASH_BITS; i++) {
                if (binaryHash.charAt(i) == '1') {
                    featureVector[i] += weight;
                } else {
                    featureVector[i] -= weight;
                }
            }
        }
        
        // 生成最终的SimHash值
        StringBuilder simHashValue = new StringBuilder();
        for (int i = 0; i < HASH_BITS; i++) {
            if (featureVector[i] > 0) {
                simHashValue.append("1");
            } else {
                simHashValue.append("0");
            }
        }
        
        // 转换为十六进制
        BigInteger hashInt = new BigInteger(simHashValue.toString(), 2);
        return hashInt.toString(16);
    }
    
    /**
     * 计算两个SimHash值的汉明距离
     * @param simHash1 第一个SimHash值
     * @param simHash2 第二个SimHash值
     * @return 汉明距离
     */
    public static int hammingDistance(String simHash1, String simHash2) {
        if (simHash1 == null || simHash2 == null || simHash1.length() != simHash2.length()) {
            return -1;
        }
        
        BigInteger hash1 = new BigInteger(simHash1, 16);
        BigInteger hash2 = new BigInteger(simHash2, 16);
        
        // 计算异或值中1的个数，即为汉明距离
        return Long.bitCount(hash1.xor(hash2).longValue());
    }
    
    /**
     * 判断两个SimHash值是否相似
     * @param simHash1 第一个SimHash值
     * @param simHash2 第二个SimHash值
     * @return 是否相似
     */
    public static boolean isSimilar(String simHash1, String simHash2) {
        int distance = hammingDistance(simHash1, simHash2);
        return distance >= 0 && distance <= HAMMING_DIST_THRESHOLD;
    }
    
    /**
     * 简单分词并计算词频
     * @param text 文本内容
     * @return 词频统计结果
     */
    private static Map<String, Integer> tokenizeAndCount(String text) {
        Map<String, Integer> wordCounts = new HashMap<>();
        
        // 使用正则表达式进行简单分词
        Matcher matcher = PATTERN.matcher(text);
        while (matcher.find()) {
            String word = matcher.group();
            if (word.length() > 1) {  // 忽略单字符
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            }
        }
        
        return wordCounts;
    }
    
    /**
     * 生成新闻的内容指纹
     * 综合考虑标题和内容
     * @param title 新闻标题
     * @param content 新闻内容
     * @return 内容指纹
     */
    public static String generateNewsFingerprint(String title, String content) {
        // 标题权重更高，重复3次
        String titleWeight = title + " " + title + " " + title;
        
        // 提取内容文本，去除HTML标签
        String plainContent = content.replaceAll("<[^>]*>", " ");
        
        // 取内容前200个字符，与标题组合生成指纹
        String textForHash = titleWeight + " " + 
                (plainContent.length() > 200 ? plainContent.substring(0, 200) : plainContent);
        
        return simHash(textForHash);
    }
} 