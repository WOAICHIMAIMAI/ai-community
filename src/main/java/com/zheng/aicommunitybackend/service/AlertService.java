package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.config.BloomFilterConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 告警服务
 * 提供多种告警方式：钉钉、企业微信、邮件等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${bloom-filter.monitoring.dingtalk-webhook-url:}")
    private String dingtalkWebhookUrl;

    @Value("${bloom-filter.monitoring.wechat-webhook-url:}")
    private String wechatWebhookUrl;

    /**
     * 发送钉钉告警
     *
     * @param stats 布隆过滤器统计信息
     */
    public void sendDingtalkAlert(BloomFilterConfig.BloomFilterStats stats) {
        if (dingtalkWebhookUrl.isEmpty()) {
            log.warn("钉钉Webhook URL未配置，跳过钉钉告警");
            return;
        }

        try {
            String alertLevel = stats.getAlertLevel().getName();
            String emoji = getAlertEmoji(alertLevel);

            String title = String.format("%s 布隆过滤器告警", emoji);
            String content = buildDingtalkContent(stats, emoji, alertLevel);

            Map<String, Object> payload = buildDingtalkPayload(title, content);
            HttpEntity<Map<String, Object>> request = buildHttpRequest(payload);

            restTemplate.postForObject(dingtalkWebhookUrl, request, String.class);
            log.info("✅ 钉钉告警发送成功");

        } catch (Exception e) {
            log.error("❌ 钉钉告警发送失败", e);
        }
    }

    /**
     * 构建钉钉消息内容
     */
    private String buildDingtalkContent(BloomFilterConfig.BloomFilterStats stats, String emoji, String alertLevel) {
        return String.format(
                "### %s 布隆过滤器告警\n\n" +
                "**告警级别**: %s\n\n" +
                "**使用率**: %.1f%%\n\n" +
                "**数据量**: %,d / %,d\n\n" +
                "**保留天数**: %d天\n\n" +
                "**最后重建**: %s\n\n" +
                "**建议操作**: %s\n\n" +
                "> 监控时间: %s",
                emoji, alertLevel,
                stats.getUsageRatio() * 100,
                stats.getLoadedUrlCount(), stats.getExpectedInsertions(),
                stats.getDataRetentionDays(),
                stats.getLastRebuildTime(),
                stats.getAlertRecommendation(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    /**
     * 构建钉钉消息载荷
     */
    private Map<String, Object> buildDingtalkPayload(String title, String content) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("msgtype", "markdown");

        Map<String, String> markdown = new HashMap<>();
        markdown.put("title", title);
        markdown.put("text", content);
        payload.put("markdown", markdown);

        return payload;
    }

    /**
     * 发送企业微信告警
     *
     * @param stats 布隆过滤器统计信息
     */
    public void sendWechatAlert(BloomFilterConfig.BloomFilterStats stats) {
        if (wechatWebhookUrl.isEmpty()) {
            log.warn("企业微信Webhook URL未配置，跳过企业微信告警");
            return;
        }

        try {
            String alertLevel = stats.getAlertLevel().getName();
            String emoji = getAlertEmoji(alertLevel);

            String content = buildWechatContent(stats, emoji, alertLevel);
            Map<String, Object> payload = buildWechatPayload(content);
            HttpEntity<Map<String, Object>> request = buildHttpRequest(payload);

            restTemplate.postForObject(wechatWebhookUrl, request, String.class);
            log.info("✅ 企业微信告警发送成功");

        } catch (Exception e) {
            log.error("❌ 企业微信告警发送失败", e);
        }
    }

    /**
     * 构建企业微信消息内容
     */
    private String buildWechatContent(BloomFilterConfig.BloomFilterStats stats, String emoji, String alertLevel) {
        return String.format(
                "%s **布隆过滤器告警**\n" +
                "告警级别: %s\n" +
                "使用率: %.1f%%\n" +
                "数据量: %,d / %,d\n" +
                "保留天数: %d天\n" +
                "最后重建: %s\n" +
                "建议操作: %s\n" +
                "监控时间: %s",
                emoji, alertLevel,
                stats.getUsageRatio() * 100,
                stats.getLoadedUrlCount(), stats.getExpectedInsertions(),
                stats.getDataRetentionDays(),
                stats.getLastRebuildTime(),
                stats.getAlertRecommendation(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    /**
     * 构建企业微信消息载荷
     */
    private Map<String, Object> buildWechatPayload(String content) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("msgtype", "markdown");

        Map<String, String> markdown = new HashMap<>();
        markdown.put("content", content);
        payload.put("markdown", markdown);

        return payload;
    }

    /**
     * 发送邮件告警（需要集成邮件服务）
     *
     * @param stats 布隆过滤器统计信息
     */
    public void sendEmailAlert(BloomFilterConfig.BloomFilterStats stats) {
        try {
            log.info("📧 准备发送邮件告警 - 布隆过滤器状态: {}", stats.getAlertLevel().getName());

            String subject = String.format("🚨 布隆过滤器告警 - %s", stats.getAlertLevel().getName());
            String content = buildEmailContent(stats);

            // TODO: 集成实际的邮件服务
            // 例如：Spring Boot Mail Starter
            // mailService.sendAlert(subject, content, recipients);

            log.info("✅ 邮件告警准备完成（需要配置邮件服务）");
            log.debug("邮件内容: {}", content);

        } catch (Exception e) {
            log.error("❌ 邮件告警处理失败", e);
        }
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(BloomFilterConfig.BloomFilterStats stats) {
        return String.format(
                "布隆过滤器状态告警\n\n" +
                "告警级别: %s\n" +
                "使用率: %.1f%%\n" +
                "已加载URL数量: %,d\n" +
                "预期容量: %,d\n" +
                "数据保留天数: %d天\n" +
                "最后重建时间: %s\n\n" +
                "建议操作:\n%s\n\n" +
                "监控时间: %s\n\n" +
                "---\n" +
                "此邮件由AI社区后台监控系统自动发送",
                stats.getAlertLevel().getName(),
                stats.getUsageRatio() * 100,
                stats.getLoadedUrlCount(),
                stats.getExpectedInsertions(),
                stats.getDataRetentionDays(),
                stats.getLastRebuildTime(),
                stats.getAlertRecommendation(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    /**
     * 构建HTTP请求
     */
    private HttpEntity<Map<String, Object>> buildHttpRequest(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
    }

    /**
     * 根据告警级别获取对应的emoji
     *
     * @param alertLevel 告警级别
     * @return emoji表情
     */
    private String getAlertEmoji(String alertLevel) {
        switch (alertLevel) {
            case "危险": return "🚨";
            case "警告": return "⚠️";
            case "注意": return "ℹ️";
            default: return "✅";
        }
    }

    /**
     * 发送测试告警
     */
    public void sendTestAlert() {
        log.info("🧪 发送测试告警");

        // 创建测试统计数据
        BloomFilterConfig.BloomFilterStats testStats = new BloomFilterConfig.BloomFilterStats(
                50000, 0.01, 35000, LocalDateTime.now(), 7,
                BloomFilterConfig.AlertLevel.WARNING, "这是一个测试告警"
        );

        // 发送各种类型的告警
        sendDingtalkAlert(testStats);
        sendWechatAlert(testStats);
        sendEmailAlert(testStats);

        log.info("✅ 测试告警发送完成");
    }
}
