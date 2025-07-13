package com.zheng.aicommunitybackend.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 数据库迁移脚本执行器
 * 用于执行SQL脚本，添加新字段和索引
 */
@Slf4j
//@Component
@Order(10) // 设置较高优先级，确保在其他任务前执行
public class DatabaseMigrationRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            log.info("开始执行数据库迁移脚本...");
            
            // 执行content_hash_migration.sql脚本
            executeSqlScript("db/content_hash_migration.sql");
            
            log.info("数据库迁移脚本执行完成");
        } catch (Exception e) {
            log.error("执行数据库迁移脚本时出错", e);
        }
    }
    
    /**
     * 执行SQL脚本
     * @param scriptPath 脚本路径
     */
    private void executeSqlScript(String scriptPath) {
        try {
            // 读取SQL脚本内容
            ClassPathResource resource = new ClassPathResource(scriptPath);
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            String sqlScript = FileCopyUtils.copyToString(reader);
            
            // 分割SQL语句
            String[] sqlStatements = sqlScript.split(";");
            
            // 执行每条SQL语句
            for (String sql : sqlStatements) {
                if (sql.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    // 检查是否是ALTER TABLE语句，需要处理列已存在的情况
                    if (sql.trim().toUpperCase().startsWith("ALTER TABLE") && 
                        sql.trim().toUpperCase().contains("ADD COLUMN")) {
                        
                        // 提取表名和列名
                        String tableName = extractTableName(sql);
                        String columnName = extractColumnName(sql);
                        
                        // 检查列是否已存在
                        if (isColumnExists(tableName, columnName)) {
                            log.info("列 {} 已存在于表 {} 中，跳过", columnName, tableName);
                            continue;
                        }
                    }
                    
                    // 检查是否是CREATE INDEX语句，需要处理索引已存在的情况
                    if (sql.trim().toUpperCase().startsWith("CREATE INDEX")) {
                        String indexName = extractIndexName(sql);
                        if (isIndexExists(indexName)) {
                            log.info("索引 {} 已存在，跳过", indexName);
                            continue;
                        }
                    }
                    
                    // 执行SQL语句
                    jdbcTemplate.execute(sql);
                    log.info("成功执行SQL: {}", sql.trim());
                } catch (Exception e) {
                    // 记录错误但继续执行下一条语句
                    log.error("执行SQL语句出错: {}, 错误: {}", sql.trim(), e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("读取SQL脚本出错: {}", scriptPath, e);
        }
    }
    
    /**
     * 从ALTER TABLE语句中提取表名
     * @param sql SQL语句
     * @return 表名
     */
    private String extractTableName(String sql) {
        String[] parts = sql.trim().split("\\s+");
        if (parts.length > 2) {
            return parts[2];
        }
        return "";
    }
    
    /**
     * 从ALTER TABLE语句中提取列名
     * @param sql SQL语句
     * @return 列名
     */
    private String extractColumnName(String sql) {
        String[] parts = sql.trim().split("\\s+");
        if (parts.length > 4 && parts[3].equalsIgnoreCase("ADD")) {
            return parts[4].replace("COLUMN", "").trim();
        }
        return "";
    }
    
    /**
     * 从CREATE INDEX语句中提取索引名
     * @param sql SQL语句
     * @return 索引名
     */
    private String extractIndexName(String sql) {
        String[] parts = sql.trim().split("\\s+");
        if (parts.length > 2) {
            return parts[2];
        }
        return "";
    }
    
    /**
     * 检查列是否已存在
     * @param tableName 表名
     * @param columnName 列名
     * @return 是否存在
     */
    private boolean isColumnExists(String tableName, String columnName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.columns " +
                         "WHERE table_name = ? AND column_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName, columnName);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查列是否存在时出错", e);
            return false;
        }
    }
    
    /**
     * 检查索引是否已存在
     * @param indexName 索引名
     * @return 是否存在
     */
    private boolean isIndexExists(String indexName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.statistics " +
                         "WHERE index_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, indexName);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查索引是否存在时出错", e);
            return false;
        }
    }
} 