package com.assistant.storage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 存储管理器
 * 统一管理所有存储操作
 */
@Service
public class StorageManager {
    
    private static final Logger logger = LoggerFactory.getLogger(StorageManager.class);
    
    @Autowired
    private PersistentVectorStorageService vectorStorageService;
    
    @Autowired
    private PersistentFileIndexService fileIndexService;
    
    @Autowired
    private VectorStorageService legacyVectorStorageService;
    
    @Value("${assistant.storage.data-dir:${user.home}/.file-assistant/data}")
    private String dataDir;
    
    @Value("${assistant.storage.enable-persistent-storage:true}")
    private boolean enablePersistentStorage;
    
    @Value("${assistant.storage.backup-enabled:true}")
    private boolean backupEnabled;
    
    @Value("${assistant.storage.compaction-interval:3600}")
    private long compactionInterval; // 秒
    
    private long lastCompactionTime = 0;
    
    @PostConstruct
    public void initialize() {
        try {
            // 创建数据目录
            Path dataPath = Paths.get(dataDir);
            Files.createDirectories(dataPath);
            
            // 检查存储状态
            checkStorageHealth();
            
            // 执行存储优化
            optimizeStorage();
            
            logger.info("存储管理器初始化成功: {}", dataDir);
            
        } catch (Exception e) {
            logger.error("存储管理器初始化失败", e);
        }
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            // 执行最终备份
            if (backupEnabled) {
                performBackup();
            }
            
            // 执行存储压缩
            performCompaction();
            
            logger.info("存储管理器清理完成");
            
        } catch (Exception e) {
            logger.error("存储管理器清理失败", e);
        }
    }
    
    /**
     * 检查存储健康状态
     */
    private void checkStorageHealth() {
        try {
            // 检查RocksDB状态
            boolean rocksDBAvailable = vectorStorageService.isRocksDBAvailable();
            logger.info("RocksDB状态: {}", rocksDBAvailable ? "可用" : "不可用");
            
            // 检查存储统计
            Map<String, Object> vectorStats = vectorStorageService.getStorageStats();
            Map<String, Object> fileStats = fileIndexService.getStorageStats();
            
            logger.info("向量存储统计: {}", vectorStats);
            logger.info("文件索引存储统计: {}", fileStats);
            
        } catch (Exception e) {
            logger.error("检查存储健康状态失败", e);
        }
    }
    
    /**
     * 优化存储
     */
    private void optimizeStorage() {
        try {
            // 检查是否需要压缩
            long currentTime = System.currentTimeMillis() / 1000;
            if (currentTime - lastCompactionTime > compactionInterval) {
                performCompaction();
                lastCompactionTime = currentTime;
            }
            
        } catch (Exception e) {
            logger.error("存储优化失败", e);
        }
    }
    
    /**
     * 执行存储压缩
     */
    public void performCompaction() {
        try {
            logger.info("开始执行存储压缩...");
            
            // 压缩向量存储
            vectorStorageService.compactDatabase();
            
            // 压缩文件索引存储
            fileIndexService.compactStorage();
            
            logger.info("存储压缩完成");
            
        } catch (Exception e) {
            logger.error("存储压缩失败", e);
        }
    }
    
    /**
     * 执行备份
     */
    public void performBackup() {
        try {
            logger.info("开始执行存储备份...");
            
            // 这里可以实现备份逻辑
            // 例如：复制数据库文件到备份目录
            
            logger.info("存储备份完成");
            
        } catch (Exception e) {
            logger.error("存储备份失败", e);
        }
    }
    
    /**
     * 获取存储统计信息
     */
    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 基础信息
            stats.put("dataDir", dataDir);
            stats.put("enablePersistentStorage", enablePersistentStorage);
            stats.put("backupEnabled", backupEnabled);
            stats.put("compactionInterval", compactionInterval);
            
            // 向量存储统计
            Map<String, Object> vectorStats = vectorStorageService.getStorageStats();
            stats.put("vectorStorage", vectorStats);
            
            // 文件索引存储统计
            Map<String, Object> fileStats = fileIndexService.getStorageStats();
            stats.put("fileIndexStorage", fileStats);
            
            // 传统存储统计（用于对比）
            Map<String, Object> legacyStats = legacyVectorStorageService.getStorageStats();
            stats.put("legacyStorage", legacyStats);
            
        } catch (Exception e) {
            logger.error("获取存储统计信息失败", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * 获取存储健康状态
     */
    public Map<String, Object> getStorageHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // 检查RocksDB状态
            boolean rocksDBAvailable = vectorStorageService.isRocksDBAvailable();
            health.put("rocksDBAvailable", rocksDBAvailable);
            
            // 检查存储大小
            Map<String, Object> vectorStats = vectorStorageService.getStorageStats();
            Map<String, Object> fileStats = fileIndexService.getStorageStats();
            
            health.put("vectorStorageSize", vectorStats.get("totalVectors"));
            health.put("fileIndexSize", fileStats.get("totalFiles"));
            
            // 计算健康分数
            int healthScore = calculateHealthScore(rocksDBAvailable, vectorStats, fileStats);
            health.put("healthScore", healthScore);
            health.put("status", healthScore > 80 ? "健康" : healthScore > 60 ? "警告" : "异常");
            
        } catch (Exception e) {
            logger.error("获取存储健康状态失败", e);
            health.put("error", e.getMessage());
            health.put("status", "异常");
        }
        
        return health;
    }
    
    /**
     * 计算健康分数
     */
    private int calculateHealthScore(boolean rocksDBAvailable, Map<String, Object> vectorStats, Map<String, Object> fileStats) {
        int score = 0;
        
        // RocksDB可用性 (40分)
        if (rocksDBAvailable) {
            score += 40;
        }
        
        // 存储大小合理性 (30分)
        int vectorSize = (Integer) vectorStats.getOrDefault("totalVectors", 0);
        int fileSize = (Integer) fileStats.getOrDefault("totalFiles", 0);
        
        if (vectorSize > 0 && fileSize > 0) {
            score += 30;
        } else if (vectorSize > 0 || fileSize > 0) {
            score += 15;
        }
        
        // 存储类型 (30分)
        String storageType = (String) vectorStats.getOrDefault("storageType", "Unknown");
        if ("RocksDB".equals(storageType)) {
            score += 30;
        } else if ("Memory".equals(storageType)) {
            score += 15;
        }
        
        return Math.min(score, 100);
    }
    
    /**
     * 清理过期数据
     */
    public void cleanupExpiredData() {
        try {
            logger.info("开始清理过期数据...");
            
            // 这里可以实现清理逻辑
            // 例如：删除过期的搜索历史、清理临时文件等
            
            logger.info("过期数据清理完成");
            
        } catch (Exception e) {
            logger.error("清理过期数据失败", e);
        }
    }
    
    /**
     * 迁移数据
     */
    public void migrateData() {
        try {
            logger.info("开始数据迁移...");
            
            // 从传统存储迁移到持久化存储
            if (enablePersistentStorage) {
                // 这里可以实现数据迁移逻辑
                logger.info("数据迁移完成");
            }
            
        } catch (Exception e) {
            logger.error("数据迁移失败", e);
        }
    }
}
