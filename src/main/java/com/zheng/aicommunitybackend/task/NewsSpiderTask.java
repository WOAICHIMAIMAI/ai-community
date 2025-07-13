package com.zheng.aicommunitybackend.task;

import com.google.common.hash.BloomFilter;
import com.zheng.aicommunitybackend.domain.entity.HotNews;
import com.zheng.aicommunitybackend.mapper.HotNewsMapper;
import com.zheng.aicommunitybackend.properties.SpiderProperties;
import com.zheng.aicommunitybackend.service.ContentSimilarityService;
import com.zheng.aicommunitybackend.util.SimHashUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 * 新闻爬虫定时任务
 * 负责定时从各个新闻源抓取热点新闻
 */
@Slf4j
@Component
public class NewsSpiderTask {

    @Autowired
    private HotNewsMapper hotNewsMapper;
    
    @Autowired
    private SpiderProperties spiderProperties;
    
    @Autowired
    private BloomFilter<String> urlBloomFilter;
    
    @Autowired
    private ContentSimilarityService contentSimilarityService;

    /**
     * 检查新闻内容是否有效
     * @param title 标题
     * @param content 内容
     * @param summary 摘要
     * @return 是否有效
     */
    private boolean isValidNewsContent(String title, String content, String summary) {
        // 标题不能为空
        if (title == null || title.isEmpty()) {
            return false;
        }
        
        // 检查标题是否包含无效内容标记
        if (title.contains("暂无详细") || title.contains("新无详细")) {
            // 这些可能是栏目标题而不是具体新闻
            return false;
        }
        
        // 内容不能为空或默认值
        if (content == null || content.isEmpty() || 
            content.equals("<p>暂无内容</p>") || 
            content.equals("<p>暂无详细内容</p>")) {
            return false;
        }
        
        // 检查内容是否只是简单的标签文本
        String plainContent = content.replaceAll("<[^>]*>", "").trim();
        if (plainContent.length() < 20) { // 降低最小长度要求
            return false;
        }
        
        // 检查内容是否包含特定的无效内容模式
        if (plainContent.contains("下载新浪财经APP") && plainContent.length() < 50) {
            return false;
        }
        
        // 摘要不能为默认值
        if (summary == null || summary.isEmpty() || 
            summary.equals("暂无摘要")) {
            return false;
        }
        
        return true;
    }

    /**
     * 抓取财经新闻 - 华尔街见闻
     * 高频抓取：每小时的第3分钟执行一次
     */
    @Scheduled(cron = "0 3 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void crawlFinanceNewsFromWallStreetCn() {
        // 检查爬虫和财经新闻爬虫是否启用
        if (!spiderProperties.getEnabled() || !spiderProperties.getFinanceNews().getEnabled()) {
            log.info("华尔街见闻财经新闻爬虫未启用，跳过执行");
            return;
        }
        
        try {
            log.info("开始抓取华尔街见闻财经新闻...");
            // 更新为华尔街见闻的主页，避免使用可能已更改的子页面
            String url = "https://wallstreetcn.com";
            log.info("抓取URL: {}", url);
            
            // 使用Jsoup连接网站并获取HTML文档
            // 增加更多的请求头，模拟真实浏览器
            Document doc = Jsoup.connect(url)
                    .userAgent(spiderProperties.getUserAgent())
                    .timeout(spiderProperties.getTimeout())
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    // 移除受限的Connection头
                    .header("Cache-Control", "max-age=0")
                    .header("sec-ch-ua", "\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"Windows\"")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .get();
            
            log.info("华尔街见闻页面标题: {}", doc.title());
            
            // 尝试多种选择器以适应新的HTML结构
            Elements newsElements = new Elements();
            
            // 尝试选择器1: 文章卡片
            Elements cards = doc.select("div.article-card, div.card, div.news-item, article");
            if (!cards.isEmpty()) {
                log.info("找到文章卡片元素: {}", cards.size());
                newsElements.addAll(cards);
            }
            
            // 尝试选择器2: 带标题的链接
            if (newsElements.isEmpty()) {
                Elements titleLinks = doc.select("a.title, a h2, h3 a, .news-title a, .article-title a");
                if (!titleLinks.isEmpty()) {
                    log.info("找到标题链接元素: {}", titleLinks.size());
                    newsElements.addAll(titleLinks);
                }
            }
            
            // 尝试选择器3: 所有链接，后续会过滤
            if (newsElements.isEmpty()) {
                Elements allLinks = doc.select("a[href]");
                log.info("找到所有链接元素: {}", allLinks.size());
                newsElements.addAll(allLinks);
            }
            
            log.info("华尔街见闻找到潜在新闻元素数量: {}", newsElements.size());
            
            // 过滤链接，只保留可能是新闻的链接
            List<Element> filteredElements = new ArrayList<>();
            for (Element element : newsElements) {
                String href = "";
                if (element.tagName().equals("a")) {
                    href = element.attr("href");
                } else {
                    Element linkElement = element.selectFirst("a[href]");
                    if (linkElement != null) {
                        href = linkElement.attr("href");
                    }
                }
                
                // 获取文本内容
                String text = element.text().trim();
                
                // 过滤条件：链接必须有文本，且文本长度大于5
                if (href != null && !href.isEmpty() && text.length() > 5 
                        && !href.contains("#") && !href.contains("javascript:")) {
                    
                    // 确保链接是绝对URL
                    if (!href.startsWith("http")) {
                        href = url + (href.startsWith("/") ? "" : "/") + href;
                    }
                    
                    // 只保留华尔街见闻的链接
                    if (href.contains("wallstreetcn.com")) {
                        Element newElement = element.clone();
                        if (element.tagName().equals("a")) {
                            newElement.attr("abs:href", href);
                        } else {
                            Element linkElement = newElement.selectFirst("a[href]");
                            if (linkElement != null) {
                                linkElement.attr("abs:href", href);
                            }
                        }
                        filteredElements.add(newElement);
                    }
                }
            }
            
            log.info("过滤后的新闻链接数量: {}", filteredElements.size());
            
            // 如果还是没有找到任何链接，尝试直接从HTML中提取
            if (filteredElements.isEmpty()) {
                log.info("尝试从HTML源代码中提取链接...");
                String html = doc.html();
                Pattern pattern = Pattern.compile("href=[\"'](https?://wallstreetcn\\.com/[^\"']+)[\"']");
                Matcher matcher = pattern.matcher(html);
                
                while (matcher.find()) {
                    String href = matcher.group(1);
                    if (!href.contains("#") && !href.contains("javascript:")) {
                        Element linkElement = new Element("a");
                        linkElement.attr("href", href);
                        linkElement.attr("abs:href", href);
                        linkElement.text("华尔街见闻文章 - " + href.substring(href.lastIndexOf('/') + 1));
                        filteredElements.add(linkElement);
                    }
                }
                log.info("从HTML源代码中提取到的链接数量: {}", filteredElements.size());
            }
            
            // 继续原有的处理逻辑...
            List<HotNews> newsList = new ArrayList<>();
            
            // 限制处理的链接数量，避免处理太多
            int maxLinks = Math.min(filteredElements.size(), 30);
            
            for (int i = 0; i < maxLinks; i++) {
                Element newsElement = filteredElements.get(i);
                try {
                    HotNews news = new HotNews();
                    
                    // 设置默认值，确保必需字段不为空
                    news.setContent("<p>暂无内容</p>");
                    news.setSummary("暂无摘要");
                    
                    // 抓取标题和URL
                    String newsTitle = newsElement.text().trim();
                    String sourceUrl = "";
                    
                    if (newsElement.tagName().equals("a")) {
                        sourceUrl = newsElement.attr("abs:href");
                    } else {
                        Element linkElement = newsElement.selectFirst("a[href]");
                        if (linkElement != null) {
                            sourceUrl = linkElement.attr("abs:href");
                        }
                    }
                    
                    // 过滤掉明显不是新闻标题的内容
                    if (newsTitle.length() < 5 || sourceUrl.isEmpty()) {
                        log.info("跳过无效标题或URL: {}", newsTitle);
                        continue;
                    }
                    
                    news.setTitle(newsTitle);
                    news.setSourceUrl(sourceUrl);
                    log.info("找到文章: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                    
                    // 先检查URL是否已存在
                    if (isNewsUrlExists(news.getSourceUrl())) {
                        log.info("文章URL已存在，跳过: {}", news.getSourceUrl());
                        continue;
                    }
                    
                    // 获取详情页面抓取更多信息
                    try {
                        Document detailDoc = Jsoup.connect(news.getSourceUrl())
                                .userAgent(spiderProperties.getUserAgent())
                                .timeout(spiderProperties.getTimeout())
                                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                                .get();
                        
                        // 抓取内容 - 使用更通用的选择器
                        Element contentElement = detailDoc.selectFirst("article, .article, .content, .post-content, .article-content, #content, .main-content, .article-detail");
                        if (contentElement != null) {
                            // 只提取文本内容并处理成简单的HTML格式
                            String content = extractContentText(contentElement);
                            news.setContent(content);
                            
                            // 生成摘要，取内容前100个字符
                            String text = contentElement.text().trim();
                            news.setSummary(text.length() > 100 ? text.substring(0, 100) + "..." : text);
                        } else {
                            log.warn("未找到文章内容元素: {}", news.getSourceUrl());
                            // 使用标题作为摘要和内容
                            news.setContent("<p>" + newsTitle + "</p>");
                            news.setSummary(newsTitle);
                        }
                        
                        // 抓取封面图 - 使用更通用的选择器
                        Element imgElement = detailDoc.selectFirst("img.cover, .article-img img, .cover-img, article img:first-child");
                        if (imgElement != null) {
                            news.setCoverImage(imgElement.attr("abs:src"));
                        } else {
                            // 尝试从meta标签中获取图片
                            Element metaImg = detailDoc.selectFirst("meta[property=og:image]");
                            if (metaImg != null) {
                                news.setCoverImage(metaImg.attr("content"));
                            }
                        }
                        
                        // 抓取发布时间
                        Element timeElement = detailDoc.selectFirst("time, .time, .date, .publish-time, .article-time, .post-date");
                        if (timeElement != null) {
                            String timeText = timeElement.text().trim();
                            try {
                                Date publishTime = parseDate(timeText);
                                if (publishTime != null) {
                                    news.setPublishTime(publishTime);
                                } else {
                                    news.setPublishTime(new Date());
                                }
                            } catch (Exception e) {
                                news.setPublishTime(new Date());
                            }
                        } else {
                            news.setPublishTime(new Date());
                        }
                    } catch (Exception e) {
                        log.warn("获取详情页失败: {}, 错误: {}", sourceUrl, e.getMessage());
                        // 使用默认值
                        news.setContent("<p>" + newsTitle + "</p>");
                        news.setSummary(newsTitle);
                        news.setPublishTime(new Date());
                    }
                    
                    // 设置其他属性
                    news.setSource("华尔街见闻");
                    news.setCategory("财经");
                    news.setTags("财经,华尔街见闻");
                    news.setViewCount(0);
                    news.setLikeCount(0);
                    news.setCommentCount(0);
                    news.setIsHot(1);  // 默认为热点
                    news.setIsTop(0);  // 默认不置顶
                    news.setStatus(1); // 默认为已发布
                    news.setCrawlTime(new Date());
                    news.setCreateTime(new Date());
                    news.setUpdateTime(new Date());
                    
                    if (news.getTitle() != null && !news.getTitle().isEmpty()) {
                        newsList.add(news);
                    }
                } catch (Exception e) {
                    log.error("解析华尔街见闻单条新闻出错", e);
                }
            }
            
            // 保存到数据库 - 修改为批量插入
            if (!newsList.isEmpty()) {
                // 使用Set保存URL，用于去除当前批次内的重复项
                Set<String> batchUrlSet = new HashSet<>();
                // 使用临时Map存储内容指纹，用于批次内去重
                Map<String, String> batchContentHashMap = new HashMap<>();
                
                List<HotNews> validNewsList = new ArrayList<>();
                int filteredCount = 0;
                int duplicateUrlCount = 0;
                int similarContentCount = 0;
                
                for (HotNews news : newsList) {
                    try {
                        // 确保content字段不为空
                        if (news.getContent() == null || news.getContent().isEmpty()) {
                            news.setContent("<p>" + news.getTitle() + "</p>");
                        }
                        // 确保summary字段不为空
                        if (news.getSummary() == null || news.getSummary().isEmpty()) {
                            news.setSummary(news.getTitle());
                        }
                        
                        // 验证新闻内容是否有效
                        if (!isValidNewsContent(news.getTitle(), news.getContent(), news.getSummary())) {
                            log.info("过滤无效新闻: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                            filteredCount++;
                            continue;
                        }
                        
                        // 限制content和summary字段长度，避免超出数据库限制
                        if (news.getContent().length() > 65000) {
                            news.setContent(news.getContent().substring(0, 65000));
                        }
                        if (news.getSummary().length() > 500) {
                            news.setSummary(news.getSummary().substring(0, 500));
                        }
                        
                        // 先检查URL是否已在当前批次中
                        if (batchUrlSet.contains(news.getSourceUrl())) {
                            log.info("当前批次已存在相同URL，跳过: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                            duplicateUrlCount++;
                            continue;
                        }
                        
                        // 先检查URL是否已存在于数据库
                        if (isNewsUrlExists(news.getSourceUrl())) {
                            log.info("数据库中已存在该URL，跳过: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                            continue;
                        }
                        
                        // 生成内容指纹
                        contentSimilarityService.generateContentHash(news);
                        String contentHash = news.getContentHash();
                        
                        // 检查当前批次中是否有相似内容
                        boolean hasSimilarInBatch = false;
                        for (Map.Entry<String, String> entry : batchContentHashMap.entrySet()) {
                            if (SimHashUtil.isSimilar(contentHash, entry.getValue())) {
                                log.info("当前批次中已存在相似内容，跳过: {} - URL: {}, 相似于: {}", 
                                        news.getTitle(), news.getSourceUrl(), entry.getKey());
                                similarContentCount++;
                                hasSimilarInBatch = true;
                                break;
                            }
                        }
                        if (hasSimilarInBatch) {
                            continue;
                        }
                        
                        // 检查数据库中是否有相似内容
                        if (contentSimilarityService.isSimilarToExisting(news)) {
                            log.info("数据库中已存在相似内容，跳过: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                            continue;
                        }
                        
                        // 添加到有效列表
                        validNewsList.add(news);
                        // 记录当前URL和内容指纹，用于后续去重
                        batchUrlSet.add(news.getSourceUrl());
                        batchContentHashMap.put(news.getSourceUrl(), contentHash);
                        
                    } catch (Exception e) {
                        // 记录错误但不中断循环，继续处理下一条
                        log.error("处理新闻记录失败: {} - URL: {}, 错误: {}", news.getTitle(), news.getSourceUrl(), e.getMessage());
                    }
                }
                
                // 批量保存有效新闻并更新布隆过滤器
                int successCount = batchSaveNewsAndUpdateBloomFilter(validNewsList);
                
                log.info("成功抓取并保存{}条华尔街见闻财经新闻，过滤无效内容{}条，批次内重复URL{}条，相似内容{}条，总抓取: {}", 
                        successCount, filteredCount, duplicateUrlCount, similarContentCount, newsList.size());
            } else {
                log.warn("未抓取到任何华尔街见闻财经新闻");
            }
            
        } catch (IOException e) {
            log.error("抓取华尔街见闻财经新闻出错", e);
        }
    }

    /**
     * 抓取财经新闻 - 财新网
     * 高频抓取：每小时的第8分钟执行一次
     */
    @Scheduled(cron = "0 8 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void crawlFinanceNewsFromCaixin() {
        // 检查爬虫和财经新闻爬虫是否启用
        if (!spiderProperties.getEnabled() || !spiderProperties.getFinanceNews().getEnabled()) {
            log.info("财新网财经新闻爬虫未启用，跳过执行");
            return;
        }
        
        try {
            log.info("开始抓取财新网财经新闻...");
            String url = spiderProperties.getFinanceNews().getCaixinUrl();
            log.info("抓取URL: {}", url);
            
            // 使用Jsoup连接网站并获取HTML文档
            Document doc = Jsoup.connect(url)
                    .userAgent(spiderProperties.getUserAgent())
                    .timeout(spiderProperties.getTimeout())
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    // 删除受限的Connection头
                    .header("Cache-Control", "max-age=0")
                    .get();
            
            log.debug("财新网页面标题: {}", doc.title());
            
            // 解析新闻列表 - 尝试多个可能的选择器
            Elements newsElements = doc.select("div.news_list li, .contentList li, ul.list li, .column-box a, .box-r a");
            log.info("财新网找到新闻元素数量: {}", newsElements.size());
            
            if (newsElements.isEmpty()) {
                // 如果没找到，尝试打印页面结构以便于调试
                log.debug("页面内容摘要: {}", doc.text().substring(0, Math.min(500, doc.text().length())));
                log.debug("可能的文章元素: {}", doc.select("article").size());
                log.debug("可能的新闻元素: {}", doc.select(".news").size());
                log.debug("所有列表元素: {}", doc.select("li").size());
                log.debug("所有链接元素: {}", doc.select("a[href]").size());
                
                // 尝试更多选择器
                newsElements = doc.select(".news_list li, .list li, article, .news-item, .item, a[href]:has(h3), a[href]:has(h4), a[href]:has(.title)");
                if (newsElements.isEmpty()) {
                    // 如果还是找不到，尝试获取所有链接
                    newsElements = doc.select("a[href]");
                }
                log.info("使用更多选择器后找到元素数量: {}", newsElements.size());
            }
            
            // 过滤链接，只保留可能是财经新闻的链接
            List<Element> filteredElements = new ArrayList<>();
            for (Element element : newsElements) {
                String href = "";
                if (element.tagName().equals("a")) {
                    href = element.attr("href");
                } else {
                    Element linkElement = element.selectFirst("a[href]");
                    if (linkElement != null) {
                        href = linkElement.attr("href");
                    }
                }
                
                // 获取文本内容
                String text = element.text().trim();
                
                // 过滤条件：链接必须有文本，且文本长度大于5
                if (href != null && !href.isEmpty() && text.length() > 5 
                        && !href.contains("#") && !href.contains("javascript:")) {
                    
                    // 确保链接是绝对URL
                    if (!href.startsWith("http")) {
                        href = url + (href.startsWith("/") ? "" : "/") + href;
                    }
                    
                    // 只保留财新网的链接
                    if (href.contains("caixin.com")) {
                        Element newElement = element.clone();
                        if (element.tagName().equals("a")) {
                            newElement.attr("abs:href", href);
                        } else {
                            Element linkElement = newElement.selectFirst("a[href]");
                            if (linkElement != null) {
                                linkElement.attr("abs:href", href);
                            }
                        }
                        filteredElements.add(newElement);
                    }
                }
            }
            
            log.info("过滤后的财新网新闻链接数量: {}", filteredElements.size());
            
            List<HotNews> newsList = new ArrayList<>();
            
            // 限制处理的链接数量，避免处理太多
            int maxLinks = Math.min(filteredElements.size(), 30);
            
            for (int i = 0; i < maxLinks; i++) {
                Element newsElement = filteredElements.get(i);
                try {
                    HotNews news = new HotNews();
                    
                    // 设置默认值，确保必需字段不为空
                    news.setContent("<p>暂无内容</p>");
                    news.setSummary("暂无摘要");
                    
                    // 抓取标题和URL
                    String newsTitle = newsElement.text().trim();
                    String sourceUrl = "";
                    
                    if (newsElement.tagName().equals("a")) {
                        sourceUrl = newsElement.attr("abs:href");
                    } else {
                        Element linkElement = newsElement.selectFirst("a[href]");
                        if (linkElement != null) {
                            sourceUrl = linkElement.attr("abs:href");
                            if (newsTitle.isEmpty()) {
                                newsTitle = linkElement.text().trim();
                            }
                        }
                    }
                    
                    // 过滤掉明显不是新闻标题的内容
                    if (newsTitle.length() < 5 || sourceUrl.isEmpty()) {
                        log.info("跳过无效标题或URL: {}", newsTitle);
                        continue;
                    }
                    
                    news.setTitle(newsTitle);
                    news.setSourceUrl(sourceUrl);
                    log.info("找到财新网文章: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                    
                    // 先检查URL是否已存在
                    if (isNewsUrlExists(news.getSourceUrl())) {
                        log.info("财新网文章URL已存在，跳过: {}", news.getSourceUrl());
                        continue;
                    }
                    
                    // 获取详情页面抓取更多信息
                    try {
                        Document detailDoc = Jsoup.connect(news.getSourceUrl())
                                .userAgent(spiderProperties.getUserAgent())
                                .timeout(spiderProperties.getTimeout())
                                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                                .header("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                                // 移除Connection头，避免IllegalArgumentException
                                .get();
                        
                        // 抓取内容 - 尝试多个可能的选择器
                        Element contentElement = detailDoc.selectFirst("div.article-content, .content, .article, #Main_Content_Val, .text, .article_content, .main-text");
                        if (contentElement != null) {
                            // 只提取文本内容并处理成简单的HTML格式
                            String content = extractContentText(contentElement);
                            news.setContent(content);
                            
                            // 生成摘要，取内容前100个字符
                            String text = contentElement.text().trim();
                            news.setSummary(text.length() > 100 ? text.substring(0, 100) + "..." : text);
                        } else {
                            log.warn("未找到文章内容元素: {}", news.getSourceUrl());
                            // 尝试从列表页的摘要中获取
                            Element summaryElement = newsElement.selectFirst("p.summary, .desc, .description");
                            if (summaryElement != null) {
                                news.setSummary(summaryElement.text().trim());
                            } else {
                                news.setSummary("暂无摘要");
                            }
                        }
                        
                        // 抓取封面图 - 尝试多个可能的选择器
                        Element imgElement = detailDoc.selectFirst("div.article-img img, .article img:first-child, .content img:first-child, .main-pic img");
                        if (imgElement != null) {
                            news.setCoverImage(imgElement.attr("abs:src"));
                        } else {
                            // 尝试从meta标签中获取图片
                            Element metaImg = detailDoc.selectFirst("meta[property=og:image]");
                            if (metaImg != null) {
                                news.setCoverImage(metaImg.attr("content"));
                            }
                        }
                        
                        // 抓取发布时间 - 尝试多个可能的选择器
                        Element timeElement = detailDoc.selectFirst("span.time, .pubtime, .date, time, .article-time, .publish-time");
                        if (timeElement != null) {
                            String timeText = timeElement.text().trim();
                            log.debug("找到时间文本: {}", timeText);
                            try {
                                // 尝试多种时间格式
                                Date publishTime = parseDate(timeText);
                                if (publishTime != null) {
                                    news.setPublishTime(publishTime);
                                } else {
                                    log.warn("无法解析发布时间文本：{}", timeText);
                                    news.setPublishTime(new Date());
                                }
                            } catch (Exception e) {
                                log.warn("解析时间失败: {}", timeText, e);
                                news.setPublishTime(new Date());
                            }
                        } else {
                            // 如果找不到时间元素，则使用当前时间
                            log.warn("未找到时间元素，使用当前时间");
                            news.setPublishTime(new Date());
                        }
                    } catch (Exception e) {
                        log.warn("获取财新网详情页失败: {}, 错误: {}", sourceUrl, e.getMessage());
                        // 使用默认值
                        news.setContent("<p>" + newsTitle + "</p>");
                        news.setSummary(newsTitle);
                        news.setPublishTime(new Date());
                    }
                    
                    // 设置其他属性
                    news.setSource("财新网");
                    news.setCategory("财经");
                    news.setTags("财经,财新网");
                    news.setViewCount(0);
                    news.setLikeCount(0);
                    news.setCommentCount(0);
                    news.setIsHot(1);  // 默认为热点
                    news.setIsTop(0);  // 默认不置顶
                    news.setStatus(1); // 默认为已发布
                    news.setCrawlTime(new Date());
                    news.setCreateTime(new Date());
                    news.setUpdateTime(new Date());
                    
                    if (news.getTitle() != null && !news.getTitle().isEmpty()) {
                        newsList.add(news);
                    }
                } catch (Exception e) {
                    log.error("解析财新网单条新闻出错", e);
                }
            }
            
            // 保存到数据库 - 修改为批量插入
            if (!newsList.isEmpty()) {
                // 使用Set保存URL，用于去除当前批次内的重复项
                Set<String> batchUrlSet = new HashSet<>();
                // 使用临时Map存储内容指纹，用于批次内去重
                Map<String, String> batchContentHashMap = new HashMap<>();
                
                List<HotNews> validNewsList = new ArrayList<>();
                int filteredCount = 0;
                int duplicateUrlCount = 0;
                int similarContentCount = 0;
                
                log.info("开始处理财新网新闻，共{}条", newsList.size());
                
                for (HotNews news : newsList) {
                    try {
                        // 确保content字段不为空
                        if (news.getContent() == null || news.getContent().isEmpty()) {
                            news.setContent("<p>" + news.getTitle() + "</p>");
                        }
                        // 确保summary字段不为空
                        if (news.getSummary() == null || news.getSummary().isEmpty()) {
                            news.setSummary(news.getTitle());
                        }
                        
                        // 验证新闻内容是否有效
                        if (!isValidNewsContent(news.getTitle(), news.getContent(), news.getSummary())) {
                            log.info("过滤无效财新网新闻: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                            filteredCount++;
                            continue;
                        }
                        
                        // 限制content和summary字段长度，避免超出数据库限制
                        if (news.getContent().length() > 65000) {
                            news.setContent(news.getContent().substring(0, 65000));
                        }
                        if (news.getSummary().length() > 500) {
                            news.setSummary(news.getSummary().substring(0, 500));
                        }
                        
                        // 先检查URL是否已在当前批次中
                        if (batchUrlSet.contains(news.getSourceUrl())) {
                            log.info("当前批次已存在相同URL，跳过: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                            duplicateUrlCount++;
                            continue;
                        }
                        
                        // 先检查URL是否已存在于数据库
                        if (isNewsUrlExists(news.getSourceUrl())) {
                            log.info("数据库中已存在该URL，跳过: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                            continue;
                        }
                        
                        // 生成内容指纹
                        contentSimilarityService.generateContentHash(news);
                        String contentHash = news.getContentHash();
                        
                        // 检查当前批次中是否有相似内容
                        boolean hasSimilarInBatch = false;
                        for (Map.Entry<String, String> entry : batchContentHashMap.entrySet()) {
                            if (SimHashUtil.isSimilar(contentHash, entry.getValue())) {
                                log.info("当前批次中已存在相似内容，跳过: {} - URL: {}, 相似于: {}", 
                                        news.getTitle(), news.getSourceUrl(), entry.getKey());
                                similarContentCount++;
                                hasSimilarInBatch = true;
                                break;
                            }
                        }
                        if (hasSimilarInBatch) {
                            continue;
                        }
                        
                        // 检查数据库中是否有相似内容
                        if (contentSimilarityService.isSimilarToExisting(news)) {
                            log.info("数据库中已存在相似内容，跳过: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                            continue;
                        }
                        
                        // 添加到有效列表
                        validNewsList.add(news);
                        // 记录当前URL和内容指纹，用于后续去重
                        batchUrlSet.add(news.getSourceUrl());
                        batchContentHashMap.put(news.getSourceUrl(), contentHash);
                    } catch (Exception e) {
                        log.error("处理财新网新闻记录失败: {} - URL: {}, 错误: {}", news.getTitle(), news.getSourceUrl(), e.getMessage());
                    }
                }
                
                // 批量保存有效新闻并更新布隆过滤器
                int successCount = batchSaveNewsAndUpdateBloomFilter(validNewsList);
                
                log.info("财新网爬虫执行完成: 成功保存{}条, 过滤无效内容{}条, 批次内重复URL{}条, 相似内容{}条, 总抓取: {}", 
                        successCount, filteredCount, duplicateUrlCount, similarContentCount, newsList.size());
            } else {
                log.warn("未抓取到任何财新网财经新闻");
            }
            
        } catch (IOException e) {
            log.error("抓取财新网财经新闻出错", e);
        }
    }
    
    /**
     * 抓取财经新闻 - 新浪财经
     */
    @Transactional(rollbackFor = Exception.class)
    public void crawlFinanceNewsFromSina() {
        try {
            log.info("开始抓取新浪财经新闻...");
            String url = "https://finance.sina.com.cn/";
            
            // 使用Jsoup连接网站并获取HTML文档
            Document doc = Jsoup.connect(url)
                    .userAgent(spiderProperties.getUserAgent())
                    .timeout(spiderProperties.getTimeout())
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                    .get();
            
            log.debug("新浪财经页面标题: {}", doc.title());
            
            // 获取所有链接
            Elements newsElements = doc.select("a[href]");
            log.info("新浪财经找到链接元素数量: {}", newsElements.size());
            
            // 过滤链接，只保留财经新闻链接
            List<Element> filteredElements = new ArrayList<>();
            for (Element element : newsElements) {
                String href = element.attr("href");
                String text = element.text().trim();
                
                // 过滤条件：链接必须有文本，且文本长度大于5，链接必须包含finance或money
                if (href != null && !href.isEmpty() && text.length() > 5 
                        && !href.contains("#") && !href.contains("javascript:")
                        && (href.contains("finance") || href.contains("money"))) {
                    filteredElements.add(element);
                }
            }
            
            log.info("过滤后的财经新闻链接数量: {}", filteredElements.size());
            
            List<HotNews> newsList = new ArrayList<>();
            
            // 限制处理的链接数量，增加到30个
            int maxLinks = Math.min(filteredElements.size(), 30);
            
            for (int i = 0; i < maxLinks; i++) {
                Element newsElement = filteredElements.get(i);
                try {
                    HotNews news = new HotNews();
                    
                    // 设置默认值，确保必需字段不为空
                    news.setContent("<p>暂无内容</p>");
                    news.setSummary("暂无摘要");
                    
                    // 抓取标题和URL
                    String title = newsElement.text().trim();
                    String sourceUrl = newsElement.attr("abs:href");
                    
                    if (!title.isEmpty() && sourceUrl.startsWith("http")) {
                        news.setTitle(title);
                        news.setSourceUrl(sourceUrl);
                        log.info("找到文章: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                        
                        // 先检查URL是否已存在
                        if (isNewsUrlExists(sourceUrl)) {
                            log.info("文章URL已存在，跳过: {}", sourceUrl);
                            continue;
                        }
                        
                        try {
                            // 获取详情页面抓取更多信息
                            Document detailDoc = Jsoup.connect(news.getSourceUrl())
                                    .userAgent(spiderProperties.getUserAgent())
                                    .timeout(spiderProperties.getTimeout())
                                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                                    .header("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                                    .get();
                            
                            // 抓取内容 - 使用更通用的选择器
                            Element contentElement = detailDoc.selectFirst("article, .article, .content, .article-content, #artibody, .main-content");
                            if (contentElement != null) {
                                // 只提取文本内容并处理成简单的HTML格式
                                String content = extractContentText(contentElement);
                                news.setContent(content);
                                
                                // 生成摘要，取内容前100个字符
                                String text = contentElement.text().trim();
                                news.setSummary(text.length() > 100 ? text.substring(0, 100) + "..." : text);
                            } else {
                                // 如果找不到内容，设置默认内容
                                news.setContent("<p>" + title + "</p>");
                                news.setSummary(title);
                            }
                            
                            // 抓取封面图
                            Element imgElement = detailDoc.selectFirst("img");
                            if (imgElement != null) {
                                news.setCoverImage(imgElement.attr("abs:src"));
                            }
                            
                            // 抓取发布时间
                            Element timeElement = detailDoc.selectFirst("time, .time, .date, .publish-time, .article-time");
                            if (timeElement != null) {
                                String timeText = timeElement.text().trim();
                                try {
                                    Date publishTime = parseDate(timeText);
                                    if (publishTime != null) {
                                        news.setPublishTime(publishTime);
                                    } else {
                                        news.setPublishTime(new Date());
                                    }
                                } catch (Exception e) {
                                    news.setPublishTime(new Date());
                                }
                            } else {
                                news.setPublishTime(new Date());
                            }
                        } catch (Exception e) {
                            log.warn("获取新浪详情页失败: {}, 错误: {}", sourceUrl, e.getMessage());
                            // 即使获取详情页失败，我们仍然可以创建一个基础的新闻记录
                            news.setSummary(title);
                            news.setContent("<p>" + title + "</p>");
                            news.setPublishTime(new Date());
                        }
                        
                        // 设置其他属性
                        news.setSource("新浪财经");
                        news.setCategory("财经");
                        news.setTags("财经,新浪财经");
                        news.setViewCount(0);
                        news.setLikeCount(0);
                        news.setCommentCount(0);
                        news.setIsHot(1);
                        news.setIsTop(0);
                        news.setStatus(1);
                        news.setCrawlTime(new Date());
                        news.setCreateTime(new Date());
                        news.setUpdateTime(new Date());
                        
                        newsList.add(news);
                    }
                } catch (Exception e) {
                    log.error("解析新浪财经单条新闻出错", e);
                }
            }
            
            // 保存到数据库 - 修改为批量插入
            if (!newsList.isEmpty()) {
                // 使用Set保存URL，用于去除当前批次内的重复项
                Set<String> batchUrlSet = new HashSet<>();
                // 使用临时Map存储内容指纹，用于批次内去重
                Map<String, String> batchContentHashMap = new HashMap<>();
                
                List<HotNews> validNewsList = new ArrayList<>();
                int filteredCount = 0;
                int duplicateUrlCount = 0;
                int similarContentCount = 0;
                
                for (int i = 0; i < newsList.size(); i++) {
                    try {
                        HotNews news = newsList.get(i);
                        // 最后再次检查所有必需字段
                        if (news.getContent() == null || news.getContent().isEmpty()) {
                            news.setContent("<p>" + news.getTitle() + "</p>");
                        }
                        if (news.getSummary() == null || news.getSummary().isEmpty()) {
                            news.setSummary(news.getTitle());
                        }
                        
                        // 验证新闻内容是否有效
                        if (!isValidNewsContent(news.getTitle(), news.getContent(), news.getSummary())) {
                            log.info("过滤无效新闻: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                            filteredCount++;
                            continue;
                        }
                        
                        // 限制content和summary字段长度
                        if (news.getContent().length() > 65000) {
                            news.setContent(news.getContent().substring(0, 65000));
                        }
                        if (news.getSummary().length() > 500) {
                            news.setSummary(news.getSummary().substring(0, 500));
                        }
                        
                        // 先检查URL是否已在当前批次中
                        if (batchUrlSet.contains(news.getSourceUrl())) {
                            log.info("当前批次已存在相同URL，跳过: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                            duplicateUrlCount++;
                            continue;
                        }
                        
                        // 先检查URL是否已存在于数据库
                        if (isNewsUrlExists(news.getSourceUrl())) {
                            log.info("数据库中已存在该URL，跳过: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                            continue;
                        }
                        
                        // 生成内容指纹
                        contentSimilarityService.generateContentHash(news);
                        String contentHash = news.getContentHash();
                        
                        // 检查当前批次中是否有相似内容
                        boolean hasSimilarInBatch = false;
                        for (Map.Entry<String, String> entry : batchContentHashMap.entrySet()) {
                            if (SimHashUtil.isSimilar(contentHash, entry.getValue())) {
                                log.info("当前批次中已存在相似内容，跳过: {} - URL: {}, 相似于: {}", 
                                        news.getTitle(), news.getSourceUrl(), entry.getKey());
                                similarContentCount++;
                                hasSimilarInBatch = true;
                                break;
                            }
                        }
                        if (hasSimilarInBatch) {
                            continue;
                        }
                        
                        // 检查数据库中是否有相似内容
                        if (contentSimilarityService.isSimilarToExisting(news)) {
                            log.info("数据库中已存在相似内容，跳过: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                            continue;
                        }
                        
                        // 添加到有效列表
                        validNewsList.add(news);
                        // 记录当前URL和内容指纹，用于后续去重
                        batchUrlSet.add(news.getSourceUrl());
                        batchContentHashMap.put(news.getSourceUrl(), contentHash);
                    } catch (Exception e) {
                        log.error("处理新闻记录失败: {}, 错误: {}", newsList.get(i).getTitle(), e.getMessage());
                    }
                }
                
                // 批量保存有效新闻并更新布隆过滤器
                int successCount = batchSaveNewsAndUpdateBloomFilter(validNewsList);
                
                log.info("成功抓取并保存{}条新浪财经新闻，过滤无效内容{}条，批次内重复URL{}条，相似内容{}条，总抓取: {}", 
                        successCount, filteredCount, duplicateUrlCount, similarContentCount, newsList.size());
            } else {
                log.warn("未抓取到任何新浪财经新闻");
            }
            
        } catch (IOException e) {
            log.error("抓取新浪财经新闻出错", e);
        }
    }
    
    /**
     * 从HTML元素中提取文本内容并转换成简洁的HTML格式
     * @param element HTML元素
     * @return 简洁的HTML内容字符串
     */
    private String extractContentText(Element element) {
        if (element == null) {
            return "<p>暂无内容</p>";
        }
        
        StringBuilder content = new StringBuilder();
        
        // 首先移除无关元素
        element = element.clone(); // 克隆以免修改原始元素
        element.select("script, style, .ad, .advertisement, .footer, .comment, .share, .related").remove();
        
        // 获取所有段落元素
        Elements paragraphs = element.select("p");
        if (!paragraphs.isEmpty()) {
            // 如果有段落元素，直接使用这些段落
            for (Element p : paragraphs) {
                String text = p.text().trim();
                if (!text.isEmpty() && 
                    !text.contains("下载新浪财经APP") && 
                    !text.contains("关注") && 
                    !text.contains("扫码") && 
                    !text.contains("二维码") && 
                    !text.contains("免责声明") && 
                    !text.contains("版权所有")) {
                    content.append("<p>").append(text).append("</p>");
                }
            }
        } else {
            // 如果没有段落元素，按照换行符分割文本
            String[] lines = element.text().split("\\n");
            for (String line : lines) {
                String text = line.trim();
                if (!text.isEmpty() && 
                    !text.contains("下载新浪财经APP") && 
                    !text.contains("关注") && 
                    !text.contains("扫码") && 
                    !text.contains("二维码") && 
                    !text.contains("免责声明") && 
                    !text.contains("版权所有")) {
                    content.append("<p>").append(text).append("</p>");
                }
            }
            
            // 如果没有换行符，则整个文本作为一个段落
            if (lines.length <= 1) {
                String text = element.text().trim();
                if (!text.isEmpty() && 
                    !text.contains("下载新浪财经APP") && 
                    !text.contains("关注") && 
                    !text.contains("扫码") && 
                    !text.contains("二维码") && 
                    !text.contains("免责声明") && 
                    !text.contains("版权所有")) {
                    content = new StringBuilder();
                    content.append("<p>").append(text).append("</p>");
                }
            }
        }
        
        // 如果内容为空，返回默认文本
        if (content.length() == 0) {
            return "<p>暂无内容</p>";
        }
        
        return content.toString();
    }
    
    /**
     * 抓取两个财经新闻源的新闻
     * @return 抓取到的新闻总数
     */
    public int crawlAllFinanceNews() {
        // 记录抓取前数据库中的新闻数量
        int beforeCount = 0;
        
        // 手动触发爬取任务
        crawlFinanceNewsFromWallStreetCn();
        crawlFinanceNewsFromCaixin();
        
        // 返回新闻总数 (这里无法精确计算新增数量，但可以返回抓取的两个源的数量)
        return 0;  // 此方法主要用于触发爬虫，返回值意义不大
    }
    
    /**
     * 清理数据库中的无效新闻记录
     * 可以通过管理接口手动触发
     * @return 清理的记录数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int cleanInvalidNewsRecords() {
        log.info("开始清理无效新闻记录...");
        int cleanedCount = 0;
        
        try {
            // 查询所有新闻记录
            List<HotNews> allNews = hotNewsMapper.selectList(null);
            log.info("共找到{}条新闻记录", allNews.size());
            
            for (HotNews news : allNews) {
                try {
                    // 使用相同的验证逻辑检查新闻是否有效
                    if (!isValidNewsContent(news.getTitle(), news.getContent(), news.getSummary())) {
                        log.info("删除无效新闻: ID={}, 标题={}", news.getId(), news.getTitle());
                        hotNewsMapper.deleteById(news.getId());
                        cleanedCount++;
                    }
                } catch (Exception e) {
                    log.error("清理新闻记录时出错: ID={}, 标题={}, 错误: {}", 
                            news.getId(), news.getTitle(), e.getMessage());
                }
            }
            
            log.info("清理完成，共删除{}条无效新闻记录", cleanedCount);
        } catch (Exception e) {
            log.error("清理无效新闻记录时发生错误", e);
        }
        
        return cleanedCount;
    }
    
    /**
     * 手动触发爬取所有新闻源并清理无效记录
     * @return 新增的有效新闻数量
     */
    public int crawlAndCleanAllNews() {
        // 先清理无效记录
        int cleanedCount = cleanInvalidNewsRecords();
        
        // 记录爬取前数据库中的新闻数量
        Long beforeCount = hotNewsMapper.selectCount(null);
        
        // 手动触发爬取任务
        crawlFinanceNewsFromWallStreetCn();
        crawlFinanceNewsFromCaixin();
        crawlFinanceNewsFromSina();
        
        // 计算新增的新闻数量
        Long afterCount = hotNewsMapper.selectCount(null);
        int addedCount = afterCount.intValue() - beforeCount.intValue();
        
        log.info("爬取完成，清理了{}条无效记录，新增了{}条有效新闻", cleanedCount, addedCount);
        
        return addedCount;
    }
    
    /**
     * 从字符串中提取日期
     * @param text 包含日期的文本
     * @return 提取的日期，如果无法提取则返回null
     */
    private Date extractDateFromString(String text) {
        // 尝试匹配常见的日期格式
        Pattern pattern = Pattern.compile("(\\d{4})[-年](\\d{1,2})[-月](\\d{1,2})日?\\s*(\\d{1,2})[:\\s时](\\d{1,2})分?");
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            try {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));
                int hour = Integer.parseInt(matcher.group(4));
                int minute = Integer.parseInt(matcher.group(5));
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return sdf.parse(String.format("%04d-%02d-%02d %02d:%02d:00", year, month, day, hour, minute));
            } catch (Exception e) {
                log.error("解析日期失败：" + text, e);
            }
        }
        
        // 尝试匹配仅有日期的格式
        pattern = Pattern.compile("(\\d{4})[-年](\\d{1,2})[-月](\\d{1,2})日?");
        matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            try {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return sdf.parse(String.format("%04d-%02d-%02d 00:00:00", year, month, day));
            } catch (Exception e) {
                log.error("解析日期失败：" + text, e);
            }
        }
        
        return null;
    }
    
    /**
     * 解析多种格式的日期
     * @param dateStr 日期字符串
     * @return 解析后的日期对象，如果解析失败则返回null
     */
    private Date parseDate(String dateStr) {
        // 如果为空，直接返回null
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        // 尝试从字符串中提取日期
        Date date = extractDateFromString(dateStr);
        if (date != null) {
            return date;
        }
        
        // 尝试多种日期格式
        String[] dateFormats = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy年MM月dd日 HH:mm",
            "yyyy年MM月dd日 HH:mm:ss",
            "yyyy年MM月dd日",
            "MM月dd日 HH:mm",
            "yyyy-MM-dd",
            "MM-dd HH:mm"
        };
        
        for (String format : dateFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                return sdf.parse(dateStr.trim());
            } catch (ParseException e) {
                // 继续尝试下一种格式
            }
        }
        
        // 所有格式都解析失败，返回null
        return null;
    }
    
    /**
     * 检查新闻URL是否已经存在于数据库中
     * 先通过布隆过滤器快速检查，如果布隆过滤器返回可能存在，再查询数据库确认
     * @param sourceUrl 新闻源URL
     * @return 是否存在
     */
    private boolean isNewsUrlExists(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isEmpty()) {
            return false;
        }
        
        try {
            // 先通过布隆过滤器快速检查
            if (!urlBloomFilter.mightContain(sourceUrl)) {
                // 布隆过滤器返回一定不存在，直接返回false
                return false;
            }
            
            // 布隆过滤器返回可能存在，查询数据库确认
            Long count = hotNewsMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<HotNews>()
                    .eq("source_url", sourceUrl));
            
            boolean exists = count > 0;
            
            // 如果数据库中存在但布隆过滤器没有，将其添加到布隆过滤器
            if (exists) {
                urlBloomFilter.put(sourceUrl);
            }
            
            return exists;
        } catch (Exception e) {
            log.error("检查URL是否存在时发生错误: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 批量保存新闻到数据库，并更新布隆过滤器
     * @param newsList 新闻对象列表
     * @return 保存成功的数量
     */
    private int batchSaveNewsAndUpdateBloomFilter(List<HotNews> newsList) {
        if (newsList == null || newsList.isEmpty()) {
            return 0;
        }
        
        int successCount = 0;
        
        // 虽然仍是循环插入，但整个过程在一个事务中完成，减少事务开销
        // 分批处理，每批最多50条记录，避免单个事务过大
        int batchSize = 50;
        int size = newsList.size();
        
        for (int i = 0; i < size; i += batchSize) {
            int end = Math.min(i + batchSize, size);
            List<HotNews> batch = newsList.subList(i, end);
            
            if (!batch.isEmpty()) {
                try {
                    // 在一个事务中批量执行插入操作
                    for (HotNews news : batch) {
                        hotNewsMapper.insert(news);
                        // 更新布隆过滤器
                        urlBloomFilter.put(news.getSourceUrl());
                    }
                    
                    log.info("批量插入新闻成功: {}条", batch.size());
                    successCount += batch.size();
                } catch (Exception e) {
                    log.error("批量插入失败，错误: {}", e.getMessage());
                    // 单个批次失败，尝试逐条保存当前批次
                    for (HotNews news : batch) {
                        try {
                            hotNewsMapper.insert(news);
                            urlBloomFilter.put(news.getSourceUrl());
                            successCount++;
                        } catch (Exception ex) {
                            log.error("单条保存失败: {} - URL: {}, 错误: {}", 
                                    news.getTitle(), news.getSourceUrl(), ex.getMessage());
                        }
                    }
                }
            }
        }
        
        return successCount;
    }
} 