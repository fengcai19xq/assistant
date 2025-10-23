package com.assistant.core.service;

import com.assistant.common.constants.AssistantConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 数据库初始化服务
 */
@Service
public class DatabaseInitService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ModelDownloadService modelDownloadService;
    
    @Autowired
    private AIEmbeddingService aiEmbeddingService;
    
    /**
     * 应用启动时初始化数据库
     */
    @PostConstruct
    public void initialize() {
        try {
            logger.info("开始初始化数据库...");
            
            // 创建必要的目录
            createDirectories();
            
            // 初始化数据库表结构
            initDatabaseSchema();
            
            // 下载AI模型
            modelDownloadService.ensureModelExists();
            
            // 初始化AI向量化服务
            aiEmbeddingService.initializeModel();
            
            logger.info("数据库初始化完成");
            
        } catch (Exception e) {
            logger.error("数据库初始化失败", e);
        }
    }
    
    /**
     * 创建必要的目录
     */
    private void createDirectories() throws IOException {
        Path dataDir = Paths.get(AssistantConstants.DB_PATH);
        Path indexDir = Paths.get(AssistantConstants.INDEX_PATH);
        Path modelDir = Paths.get(AssistantConstants.MODEL_PATH);
        Path logDir = Paths.get(System.getProperty("user.home") + "/.file-assistant/logs");
        
        Files.createDirectories(dataDir);
        Files.createDirectories(indexDir);
        Files.createDirectories(modelDir);
        Files.createDirectories(logDir);
        
        logger.info("创建目录完成: data={}, index={}, model={}, log={}", 
                   dataDir, indexDir, modelDir, logDir);
    }
    
    /**
     * 初始化数据库表结构
     */
    private void initDatabaseSchema() {
        try {
            // 读取SQL脚本
            ClassPathResource resource = new ClassPathResource("schema.sql");
            String sql = new String(org.apache.commons.io.IOUtils.toByteArray(resource.getInputStream()), StandardCharsets.UTF_8);
            
            // 按行分割并过滤空行和注释
            String[] lines = sql.split("\n");
            StringBuilder currentStatement = new StringBuilder();
            
            for (String line : lines) {
                line = line.trim();
                // 跳过空行和注释
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                
                currentStatement.append(line).append(" ");
                
                // 如果行以分号结尾，执行语句
                if (line.endsWith(";")) {
                    String statement = currentStatement.toString().trim();
                    if (!statement.isEmpty()) {
                        try {
                            jdbcTemplate.execute(statement);
                            logger.debug("执行SQL成功: {}", statement.substring(0, Math.min(50, statement.length())));
                        } catch (Exception e) {
                            logger.warn("执行SQL语句失败: {}", statement.substring(0, Math.min(50, statement.length())), e);
                            // 对于索引创建失败，我们继续执行，因为表可能已经存在
                            if (statement.toUpperCase().contains("CREATE INDEX")) {
                                logger.info("跳过索引创建，表可能已存在");
                            }
                        }
                    }
                    currentStatement = new StringBuilder();
                }
            }
            
            logger.info("数据库表结构初始化完成");
            
        } catch (IOException e) {
            logger.error("读取SQL脚本失败", e);
        } catch (Exception e) {
            logger.error("初始化数据库表结构失败", e);
        }
    }
    
    /**
     * 检查数据库连接
     */
    public boolean checkDatabaseConnection() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            logger.info("数据库连接正常");
            return true;
        } catch (Exception e) {
            logger.error("数据库连接失败", e);
            return false;
        }
    }
    
    /**
     * 获取数据库信息
     */
    public String getDatabaseInfo() {
        try {
            // 获取表数量
            Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sqlite_master WHERE type='table'", Integer.class);
            
            // 获取文件索引数量
            Integer fileCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM file_index", Integer.class);
            
            // 获取监控文件夹数量
            Integer folderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM watch_folders", Integer.class);
            
            return String.format("数据库信息 - 表数量: %d, 文件索引: %d, 监控文件夹: %d", 
                               tableCount, fileCount, folderCount);
            
        } catch (Exception e) {
            logger.error("获取数据库信息失败", e);
            return "数据库信息获取失败";
        }
    }
}
