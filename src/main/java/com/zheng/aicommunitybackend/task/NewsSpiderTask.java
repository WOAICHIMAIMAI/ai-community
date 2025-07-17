package com.zheng.aicommunitybackend.task;

import com.google.common.hash.BloomFilter;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.zheng.aicommunitybackend.domain.entity.HotNews;
import com.zheng.aicommunitybackend.mapper.HotNewsMapper;
import com.zheng.aicommunitybackend.properties.SpiderProperties;
import com.zheng.aicommunitybackend.service.impl.ContentSimilarityService;
import com.zheng.aicommunitybackend.util.SimHashUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
    
    // 结巴分词器实例
    private final JiebaSegmenter jiebaSegmenter = new JiebaSegmenter();

    /**
     * 组件初始化后预热结巴分词器
     */
    @PostConstruct
    public void init() {
        // 预热结巴分词器，避免首次使用时性能问题
        warmupJiebaSegmenter();
        log.info("结巴分词器预热完成");
    }
    
    /**
     * 预热结巴分词器
     * 通过处理一些常见的文本样本来加载词典和模型
     */
    private void warmupJiebaSegmenter() {
        try {
            // 使用一些常见的财经文本预热
            String[] samples = {
                "中国经济增长率达到6.5%，超出市场预期",
                "央行宣布降低存款准备金率，释放流动性超过1万亿元",
                "科技股大涨，带动市场走高",
                "新能源汽车产业政策出台，利好相关企业",
                "人工智能技术在金融领域的应用不断深入",
                "国际原油价格上涨，能源板块表现活跃",
                "数字经济成为经济增长新动能，相关政策不断完善"
            };
            
            for (String sample : samples) {
                // 进行分词处理，加载词典
                List<String> segments = jiebaSegmenter.sentenceProcess(sample);
                // 日志级别设为debug，避免产生太多输出
                if (log.isDebugEnabled()) {
                    log.debug("预热分词结果: {}", segments);
                }
            }
        } catch (Exception e) {
            log.warn("结巴分词器预热过程中发生异常", e);
            // 预热失败不影响系统运行
        }
    }

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
        if (plainContent.length() < 15) { // 降低最小长度要求，允许更短的内容（原为20）
            return false;
        }
        
        // 检查内容是否包含特定的无效内容模式，但如果内容长度足够，也允许通过
        if (plainContent.contains("下载新浪财经APP") && plainContent.length() < 40) {
            return false;
        }
        
        // 摘要不能为默认值，但如果有内容，即使摘要是默认值也可以接受
        if ((summary == null || summary.isEmpty() || summary.equals("暂无摘要")) && plainContent.length() < 60) {
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
            // 添加随机延迟（0-120秒）
            int randomDelay = java.util.concurrent.ThreadLocalRandom.current().nextInt(120000);
            log.info("华尔街见闻爬虫将在{}秒后开始执行...", randomDelay / 1000);
            try {
                Thread.sleep(randomDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("华尔街见闻爬虫执行被中断");
                return;
            }
            
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
            // 添加随机延迟（0-120秒）
            int randomDelay = java.util.concurrent.ThreadLocalRandom.current().nextInt(120000);
            log.info("财新网爬虫将在{}秒后开始执行...", randomDelay / 1000);
            try {
                Thread.sleep(randomDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("财新网爬虫执行被中断");
                return;
            }
            
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
    @Scheduled(cron = "0 11 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void crawlFinanceNewsFromSina() {
        try {
  /*          // 添加随机延迟（0-120秒）
            int randomDelay = java.util.concurrent.ThreadLocalRandom.current().nextInt(120000);
            log.info("新浪财经爬虫将在{}秒后开始执行...", randomDelay / 1000);
            try {
                Thread.sleep(randomDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("新浪财经爬虫执行被中断");
                return;
            }*/
            
            log.info("开始抓取新浪财经新闻...");
            // 基础URL
            String mainUrl = "https://finance.sina.com.cn/";
            
            // 定义多个需要爬取的子页面，增加数据源
            List<String> urlsToFetch = new ArrayList<>();
            urlsToFetch.add(mainUrl);
            urlsToFetch.add("https://finance.sina.com.cn/stock/");  // 股票页面
            urlsToFetch.add("https://finance.sina.com.cn/roll/");   // 滚动新闻
            urlsToFetch.add("https://finance.sina.com.cn/china/");  // 国内财经
            
            List<Element> allFilteredElements = new ArrayList<>();
            
            // 遍历并爬取每个URL
            for (String url : urlsToFetch) {
                try {
                    log.info("正在抓取URL: {}", url);
                    
                    // 使用Jsoup连接网站并获取HTML文档
                    Document doc = Jsoup.connect(url)
                            .userAgent(spiderProperties.getUserAgent())
                            .timeout(spiderProperties.getTimeout())
                            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .header("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                            .get();
                    
                    log.info("抓取页面标题: {}", doc.title());
                    
                    // 获取所有链接
                    Elements newsElements = doc.select("a[href]");
                    log.debug("发现链接元素数量: {}", newsElements.size());
                    
                    // 过滤链接，放宽筛选条件
                    List<Element> filteredElements = new ArrayList<>();
                    for (Element element : newsElements) {
                        String href = element.attr("href");
                        String text = element.text().trim();
                        
                        // 扩大关键词范围，包含更多财经相关内容
                        if (href != null && !href.isEmpty() && text.length() > 4  // 降低文本长度要求
                                && !href.contains("#") && !href.contains("javascript:")
                                && (href.contains("finance") || href.contains("money") 
                                || href.contains("stock") || href.contains("business")
                                || href.contains("economy") || href.contains("china")
                                || href.contains("forex") || href.contains("fund")
                                || href.contains("industry") || href.contains("bank"))) {
                            filteredElements.add(element);
                        }
                    }
                    
                    log.info("页面 {} 过滤后的新闻链接数量: {}", url, filteredElements.size());
                    allFilteredElements.addAll(filteredElements);
                } catch (Exception e) {
                    log.warn("抓取URL {}失败: {}", url, e.getMessage());
                    // 继续处理下一个URL，不影响整体流程
                }
            }
            
            log.info("总共过滤后的新闻链接数量: {}", allFilteredElements.size());
            if (allFilteredElements.isEmpty()) {
                log.warn("未找到符合条件的新浪财经新闻链接");
                return;
            }
            
            // 去重，确保不处理重复的URL
            Map<String, Element> uniqueElements = new HashMap<>();
            for (Element element : allFilteredElements) {
                String href = element.attr("abs:href");
                if (!uniqueElements.containsKey(href) && href.startsWith("http")) {
                    uniqueElements.put(href, element);
                }
            }
            
            List<HotNews> newsList = new ArrayList<>();
            
            // 增加处理的链接数量上限
            int maxLinks = Math.min(uniqueElements.size(), 100);  // 从30增加到100
            
            List<String> urls = new ArrayList<>(uniqueElements.keySet());
            for (int i = 0; i < maxLinks; i++) {
                String sourceUrl = urls.get(i);
                Element newsElement = uniqueElements.get(sourceUrl);
                try {
                    HotNews news = new HotNews();
                    
                    // 设置默认值，确保必需字段不为空
                    news.setContent("<p>暂无内容</p>");
                    news.setSummary("暂无摘要");
                    
                    // 抓取标题和URL
                    String title = newsElement.text().trim();
                    
                    if (!title.isEmpty() && sourceUrl.startsWith("http")) {
                        news.setTitle(title);
                        news.setSourceUrl(sourceUrl);
                        log.debug("找到文章: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                        
                        // 先检查URL是否已存在
                        if (isNewsUrlExists(sourceUrl)) {
                            log.info("文章URL已存在，跳过: {}", sourceUrl);
                            continue;
                        }
                        
                        // 添加重试机制
                        boolean detailFetched = false;
                        int maxRetries = 3;
                        for (int retry = 0; retry < maxRetries && !detailFetched; retry++) {
                            try {
                                // 获取详情页面抓取更多信息
                                Document detailDoc = Jsoup.connect(sourceUrl)
                                        .userAgent(spiderProperties.getUserAgent())
                                        .timeout(spiderProperties.getTimeout() * 2)  // 增加超时时间
                                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                                        .header("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                                        .header("Referer", mainUrl)
                                        .get();
                                
                                // 抓取内容 - 使用更多的选择器，增加匹配概率
                                Element contentElement = detailDoc.selectFirst("article, .article, .content, .article-content, #artibody, .main-content, .article-text, #content, .news-text");
                                if (contentElement != null) {
                                    // 只提取文本内容并处理成简单的HTML格式
                                    String content = extractContentText(contentElement);
                                    news.setContent(content);
                                    
                                    // 生成摘要，取内容前100个字符
                                    String text = contentElement.text().trim();
                                    news.setSummary(text.length() > 100 ? text.substring(0, 100) + "..." : text);
                                    
                                    log.debug("成功抓取内容，长度: {}", content.length());
                                    detailFetched = true;
                                } else {
                                    // 如果找不到内容，设置默认内容
                                    log.warn("未找到内容元素: {}", sourceUrl);
                                    news.setContent("<p>" + title + "</p>");
                                    news.setSummary(title);
                                    detailFetched = true;  // 即使没找到内容，也算是处理完成
                                }
                                
                                // 抓取封面图，尝试多种选择器
                                Element imgElement = detailDoc.selectFirst(".article-img img, .main-pic img, article img:first-child, .content img:first-child, img");
                                if (imgElement != null) {
                                    news.setCoverImage(imgElement.attr("abs:src"));
                                }
                                
                                // 抓取发布时间，使用更多选择器
                                Element timeElement = detailDoc.selectFirst("time, .time, .date, .publish-time, .article-time, .pubtime, .release-time");
                                if (timeElement != null) {
                                    String timeText = timeElement.text().trim();
                                    try {
                                        Date publishTime = parseDate(timeText);
                                        if (publishTime != null) {
                                            news.setPublishTime(publishTime);
                                            log.debug("解析到发布时间: {}", publishTime);
                                        } else {
                                            news.setPublishTime(new Date());
                                        }
                                    } catch (Exception e) {
                                        news.setPublishTime(new Date());
                                        log.warn("解析发布时间失败: {}", timeText);
                                    }
                                } else {
                                    news.setPublishTime(new Date());
                                }
                                
                            } catch (Exception e) {
                                if (retry == maxRetries - 1) {
                                    log.warn("获取详情页多次重试失败: {}, 错误: {}", sourceUrl, e.getMessage());
                                    // 即使获取详情页失败，我们仍然可以创建一个基础的新闻记录
                                    news.setSummary(title);
                                    news.setContent("<p>" + title + "</p>");
                                    news.setPublishTime(new Date());
                                } else {
                                    log.debug("获取详情页失败，准备重试: {}/{}, URL: {}", retry+1, maxRetries, sourceUrl);
                                    try {
                                        Thread.sleep(1000);  // 重试前等待1秒
                                    } catch (InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                            }
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
                        
                        // 生成内容指纹
                        contentSimilarityService.generateContentHash(news);
                        
                        newsList.add(news);
                    }
                } catch (Exception e) {
                    log.error("解析新浪财经单条新闻出错: {}", e.getMessage(), e);
                }
            }
            
            log.info("成功解析新浪财经新闻数量: {}", newsList.size());
            
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
                        
                        // 验证新闻内容是否有效，放宽验证条件
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
                        
                        // 再次检查URL是否已存在于数据库（可能在处理过程中被其他进程添加）
                        if (isNewsUrlExists(news.getSourceUrl())) {
                            log.info("数据库中已存在该URL，跳过: {} - URL: {}", news.getTitle(), news.getSourceUrl());
                            continue;
                        }
                        
                        // 确保内容哈希已生成
                        if (news.getContentHash() == null) {
                            contentSimilarityService.generateContentHash(news);
                        }
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
                        log.error("处理新闻记录失败: {}, 错误: {}", newsList.get(i).getTitle(), e.getMessage(), e);
                    }
                }
                
                if (validNewsList.isEmpty()) {
                    log.warn("没有有效的新闻需要保存到数据库");
                    return;
                }
                
                log.info("开始保存{}条有效新闻到数据库", validNewsList.size());
                
                // 批量保存有效新闻并更新布隆过滤器
                int successCount = batchSaveNewsAndUpdateBloomFilter(validNewsList);
                
                log.info("成功抓取并保存{}条新浪财经新闻，过滤无效内容{}条，批次内重复URL{}条，相似内容{}条，总抓取: {}", 
                        successCount, filteredCount, duplicateUrlCount, similarContentCount, newsList.size());
            } else {
                log.warn("未抓取到任何新浪财经新闻");
            }
        } catch (Exception e) {
            log.error("新浪财经爬虫执行过程中发生错误", e);
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
     * 抓取所有财经新闻源的新闻
     * @return 抓取到的新闻总数
     */
    public int crawlAllFinanceNews() {
        // 记录抓取前数据库中的新闻数量
        Long beforeCount = hotNewsMapper.selectCount(null);
        
        log.info("开始批量抓取所有财经新闻源...");
        
        // 手动触发爬取任务
        crawlFinanceNewsFromWallStreetCn();
        crawlFinanceNewsFromCaixin();
        crawlFinanceNewsFromSina();
        
        // 计算新增的新闻数量
        Long afterCount = hotNewsMapper.selectCount(null);
        int addedCount = afterCount.intValue() - beforeCount.intValue();
        
        log.info("批量抓取完成，共新增{}条新闻", addedCount);
        
        return addedCount;
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
     * 批量保存新闻到数据库，并更新布隆过滤器和缓存
     * 使用虚拟线程提升性能
     * @param newsList 新闻对象列表
     * @return 保存成功的数量
     */
    private int batchSaveNewsAndUpdateBloomFilter(List<HotNews> newsList) {
        if (newsList == null || newsList.isEmpty()) {
            return 0;
        }
        
        // 使用原子整数进行并发计数
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        // 批次大小
        int batchSize = 15;
        int size = newsList.size();
        // 计算需要创建的批次数
        int batchCount = (int) Math.ceil((double) size / batchSize);
        
        // 使用CountDownLatch等待所有任务完成
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(batchCount);
        
        // 限制并发数量，避免数据库连接池耗尽
        // 假设每批任务需要1个连接，保留一些连接给其他操作
        int maxConcurrentBatches = 10; // 根据实际连接池大小调整
        java.util.concurrent.Semaphore semaphore = new java.util.concurrent.Semaphore(maxConcurrentBatches);
        
        log.info("开始使用虚拟线程批量处理 {} 条新闻，共 {} 个批次", size, batchCount);
        
        // 分批处理
        for (int i = 0; i < size; i += batchSize) {
            int start = i;
            int end = Math.min(i + batchSize, size);
            List<HotNews> batch = new ArrayList<>(newsList.subList(start, end));
            final int batchNumber = i / batchSize + 1;
            
            // 使用虚拟线程处理每个批次
            try {
                // 获取信号量，限制并发数
                semaphore.acquire();
                
                // 启动虚拟线程
                Thread.startVirtualThread(() -> {
                    try {
                        log.debug("批次 {}/{} 开始处理 {} 条新闻", batchNumber, batchCount, batch.size());
                        int batchSuccess = processBatchWithTransaction(batch);
                        successCount.addAndGet(batchSuccess);
                        log.debug("批次 {}/{} 处理完成，成功插入 {} 条", batchNumber, batchCount, batchSuccess);
                    } catch (Exception e) {
                        log.error("批次 {} 处理失败: {}", batchNumber, e.getMessage());
                    } finally {
                        // 释放信号量
                        semaphore.release();
                        // 完成一个批次
                        latch.countDown();
                    }
                });
            } catch (InterruptedException e) {
                log.error("线程中断异常: {}", e.getMessage());
                Thread.currentThread().interrupt();
                // 即使发生中断，也要确保countDown
                latch.countDown();
            }
        }
        
        try {
            // 等待所有批次完成
            boolean allDone = latch.await(5, java.util.concurrent.TimeUnit.MINUTES);
            if (!allDone) {
                log.warn("批量处理超时，部分批次可能未完成");
            }
        } catch (InterruptedException e) {
            log.error("等待批次完成时发生中断: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        
        int totalSuccess = successCount.get();
        log.info("虚拟线程批量处理完成，共成功插入 {} 条新闻", totalSuccess);
        return totalSuccess;
    }
    
    /**
     * 在事务中处理单个批次
     * @param batch 批次数据
     * @return 成功处理的记录数
     */
    @Transactional(rollbackFor = Exception.class)
    protected int processBatchWithTransaction(List<HotNews> batch) {
        if (batch.isEmpty()) {
            return 0;
        }
        
        int batchSuccess = 0;
        try {
            // 批量处理该批次中的所有记录
            for (HotNews news : batch) {
                try {
                    // 确保内容哈希不为空
                    if (news.getContentHash() == null) {
                        contentSimilarityService.generateContentHash(news);
                    }
                    
                    // 1. 先插入数据库
                    hotNewsMapper.insert(news);
                    
                    // 2. 数据库插入成功后，再更新布隆过滤器和缓存
                    urlBloomFilter.put(news.getSourceUrl());
                    contentSimilarityService.cacheContentHash(news.getContentHash());
                    
                    log.debug("成功保存新闻: ID={}, 标题={}, URL={}", 
                            news.getId(), news.getTitle(), news.getSourceUrl());
                    
                    batchSuccess++;
                } catch (Exception e) {
                    // 记录具体哪条数据失败
                    log.error("单条新闻处理失败: {} - URL: {}, 错误: {}", 
                            news.getTitle(), news.getSourceUrl(), e.getMessage(), e);
                    // 继续处理下一条，不影响整个批次
                }
            }
            
            // 如果该批次全部失败，抛出异常触发回滚
            if (batchSuccess == 0 && !batch.isEmpty()) {
                throw new RuntimeException("批次中所有记录处理失败");
            }
            
            return batchSuccess;
        } catch (Exception e) {
            // 批次级别异常，整个批次回滚
            log.error("批次处理发生严重错误，执行回滚: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 清理缓存并重新爬取新浪财经新闻
     * 这个方法可以在怀疑缓存与数据库不一致时调用
     * @return 重新抓取的新闻数量
     */
    public int resetAndCrawlSinaNews() {
        try {
            log.info("开始清理缓存并重新爬取新浪财经新闻...");
            
            // 记录清理前的新闻数量
            Long beforeCount = hotNewsMapper.selectCount(null);
            
            // 清理内容哈希缓存
            contentSimilarityService.clearContentHashCache();
            
            // 重新抓取新浪财经新闻
            crawlFinanceNewsFromSina();
            
            // 计算新增的新闻数量
            Long afterCount = hotNewsMapper.selectCount(null);
            int addedCount = afterCount.intValue() - beforeCount.intValue();
            
            log.info("清理缓存并重新抓取完成，新增了{}条新闻", addedCount);
            return addedCount;
        } catch (Exception e) {
            log.error("清理缓存并重新抓取新浪财经新闻时出错", e);
            return -1;
        }
    }
}


