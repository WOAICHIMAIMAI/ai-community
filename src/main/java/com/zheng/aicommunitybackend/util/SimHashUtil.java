package com.zheng.aicommunitybackend.util;

import com.huaban.analysis.jieba.JiebaSegmenter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SimHash算法工具类
 * 用于计算文本的指纹并进行相似度比较
 */
public class SimHashUtil {

    // 结巴分词器实例，静态单例模式
    private static final JiebaSegmenter JIEBA_SEGMENTER = new JiebaSegmenter();
    
    // 英文单词匹配的正则表达式（仅用于英文分词补充）
    private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]+");
    
    // 汉明距离阈值，小于等于此值认为相似
    // 调整阈值为10（原为8），允许更多差异度，获取更多不同内容
    private static final int HAMMING_DIST_THRESHOLD = 10;
    
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
        if (simHash1 == null || simHash2 == null) {
            return -1;
        }
        
        try {
            BigInteger hash1 = new BigInteger(simHash1, 16);
            BigInteger hash2 = new BigInteger(simHash2, 16);
            
            // 计算异或值中1的个数，即为汉明距离
            return Long.bitCount(hash1.xor(hash2).longValue());
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * 判断两个SimHash值是否相似
     * @param simHash1 第一个SimHash值
     * @param simHash2 第二个SimHash值
     * @return 是否相似
     */
    public static boolean isSimilar(String simHash1, String simHash2) {
        int distance = hammingDistance(simHash1, simHash2);
        // 距离为负数表示发生错误，不相似
        if (distance < 0) {
            return false;
        }
        // 距离小于等于阈值认为相似
        return distance <= HAMMING_DIST_THRESHOLD;
    }
    
    /**
     * 使用结巴分词并计算词频
     * @param text 文本内容
     * @return 词频统计结果
     */
    private static Map<String, Integer> tokenizeAndCount(String text) {
        if (text == null || text.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Integer> wordCounts = new HashMap<>();
        
        try {
            // 1. 使用结巴分词器处理中文
            List<String> words = JIEBA_SEGMENTER.sentenceProcess(text);
            
            // 2. 使用流操作统计词频
            Map<String, Long> wordFrequencies = words.stream()
                    .filter(word -> word.length() > 1) // 忽略单字符
                    .collect(Collectors.groupingBy(
                            Function.identity(),
                            Collectors.counting()
                    ));
            
            // 3. 转换为Integer类型的计数
            wordFrequencies.forEach((word, count) -> 
                wordCounts.put(word, count.intValue())
            );
            
            // 4. 额外处理英文单词（结巴可能未完全处理英文）
            Matcher matcher = ENGLISH_PATTERN.matcher(text);
            while (matcher.find()) {
                String word = matcher.group();
                if (word.length() > 1) { // 忽略单字符
                    wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                }
            }
        } catch (Exception e) {
            // 如果结巴分词失败，回退到简单分词方式
            String[] words = text.split("\\s+");
            for (String word : words) {
                if (word.length() > 1) {
                    wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                }
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
        
        // 取内容前500个字符，增加取样范围，与标题组合生成指纹
        String textForHash = titleWeight + " " + 
                (plainContent.length() > 500 ? plainContent.substring(0, 500) : plainContent);
        
        return simHash(textForHash);
    }
} 