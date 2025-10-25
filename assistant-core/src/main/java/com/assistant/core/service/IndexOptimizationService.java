package com.assistant.core.service;

import com.assistant.core.mapper.FileIndexMapper;
import com.assistant.core.entity.FileIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 索引优化服务
 * 负责索引的维护、优化和性能提升
 */
@Service
public class IndexOptimizationService {

    private static final Logger logger = LoggerFactory.getLogger(IndexOptimizationService.class);

    @Autowired
    private FileIndexMapper fileIndexMapper;

    @Autowired
    private FileIndexService fileIndexService;

    // 索引统计信息
    private final Map<String, Object> indexStats = new ConcurrentHashMap<>();
    
    // 性能监控
    private final Map<String, Long> performanceMetrics = new ConcurrentHashMap<>();

    /**
     * 异步重建索引
     */
    @Async
    public CompletableFuture<Void> rebuildIndexAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("开始异步重建索引");
                long startTime = System.currentTimeMillis();
                
                // 1. 清理旧索引
                clearOldIndexes();
                
                // 2. 重新扫描文件
                fileIndexService.rebuildIndex();
                
                // 3. 优化索引结构
                optimizeIndexStructure();
                
                // 4. 更新统计信息
                updateIndexStats();
                
                long endTime = System.currentTimeMillis();
                performanceMetrics.put("lastRebuildTime", endTime - startTime);
                
                logger.info("异步重建索引完成，耗时: {} ms", endTime - startTime);
                
            } catch (Exception e) {
                logger.error("异步重建索引失败", e);
            }
        });
    }

    /**
     * 增量索引更新
     */
    @Async
    public CompletableFuture<Void> updateIndexIncremental() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("开始增量索引更新");
                long startTime = System.currentTimeMillis();
                
                // 1. 检测文件变化
                List<String> changedFiles = detectFileChanges();
                
                // 2. 更新变化的文件
                for (String filePath : changedFiles) {
                    updateFileIndex(filePath);
                }
                
                // 3. 清理无效索引
                cleanupInvalidIndexes();
                
                long endTime = System.currentTimeMillis();
                performanceMetrics.put("lastIncrementalUpdateTime", endTime - startTime);
                
                logger.info("增量索引更新完成，更新 {} 个文件，耗时: {} ms", 
                    changedFiles.size(), endTime - startTime);
                
            } catch (Exception e) {
                logger.error("增量索引更新失败", e);
            }
        });
    }

    /**
     * 优化索引结构
     */
    public void optimizeIndexStructure() {
        try {
            logger.info("开始优化索引结构");
            
            // 1. 分析索引分布
            analyzeIndexDistribution();
            
            // 2. 优化数据库查询
            optimizeDatabaseQueries();
            
            // 3. 创建必要的索引
            createDatabaseIndexes();
            
            // 4. 更新统计信息
            updateDatabaseStats();
            
            logger.info("索引结构优化完成");
            
        } catch (Exception e) {
            logger.error("索引结构优化失败", e);
        }
    }

    /**
     * 分析索引分布
     */
    private void analyzeIndexDistribution() {
        try {
            // 获取文件类型分布
            List<Map<String, Object>> fileTypeDistribution = fileIndexMapper.getFileTypeDistribution();
            indexStats.put("fileTypeDistribution", fileTypeDistribution);
            
            // 获取文件大小分布
            List<Map<String, Object>> fileSizeDistribution = fileIndexMapper.getFileSizeDistribution();
            indexStats.put("fileSizeDistribution", fileSizeDistribution);
            
            // 获取最近修改时间分布
            List<Map<String, Object>> modificationTimeDistribution = fileIndexMapper.getModificationTimeDistribution();
            indexStats.put("modificationTimeDistribution", modificationTimeDistribution);
            
            logger.info("索引分布分析完成");
            
        } catch (Exception e) {
            logger.error("索引分布分析失败", e);
        }
    }

    /**
     * 优化数据库查询
     */
    private void optimizeDatabaseQueries() {
        try {
            // 分析慢查询
            List<String> slowQueries = fileIndexMapper.getSlowQueries();
            if (!slowQueries.isEmpty()) {
                logger.warn("发现 {} 个慢查询", slowQueries.size());
                for (String query : slowQueries) {
                    logger.warn("慢查询: {}", query);
                }
            }
            
            // 优化查询计划
            fileIndexMapper.optimizeQueryPlan();
            
            logger.info("数据库查询优化完成");
            
        } catch (Exception e) {
            logger.error("数据库查询优化失败", e);
        }
    }

    /**
     * 创建数据库索引
     */
    private void createDatabaseIndexes() {
        try {
            // 创建内容索引
            fileIndexMapper.createContentIndex();
            
            // 创建文件类型索引
            fileIndexMapper.createFileTypeIndex();
            
            // 创建修改时间索引
            fileIndexMapper.createModificationTimeIndex();
            
            // 创建文件大小索引
            fileIndexMapper.createFileSizeIndex();
            
            logger.info("数据库索引创建完成");
            
        } catch (Exception e) {
            logger.error("数据库索引创建失败", e);
        }
    }

    /**
     * 更新数据库统计信息
     */
    private void updateDatabaseStats() {
        try {
            fileIndexMapper.updateTableStats();
            logger.info("数据库统计信息更新完成");
        } catch (Exception e) {
            logger.error("数据库统计信息更新失败", e);
        }
    }

    /**
     * 检测文件变化
     */
    private List<String> detectFileChanges() {
        try {
            // 获取所有监控文件夹
            List<String> watchFolders = fileIndexService.getWatchFolders()
                .stream()
                .map(folder -> folder.toString())
                .collect(Collectors.toList());
            
            List<String> changedFiles = new ArrayList<>();
            
            // 检测每个文件夹的变化
            for (String folder : watchFolders) {
                List<String> folderChanges = detectFolderChanges(folder);
                changedFiles.addAll(folderChanges);
            }
            
            return changedFiles;
            
        } catch (Exception e) {
            logger.error("检测文件变化失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 检测单个文件夹的变化
     */
    private List<String> detectFolderChanges(String folderPath) {
        // 这里应该实现文件系统监控
        // 简化实现：返回空列表
        return new ArrayList<>();
    }

    /**
     * 更新单个文件的索引
     */
    private void updateFileIndex(String filePath) {
        try {
            // 检查文件是否存在
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                // 文件不存在，删除索引
                fileIndexMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FileIndex>()
                    .eq("file_path", filePath));
                return;
            }
            
            // 检查文件是否已修改
            FileIndex existingIndex = fileIndexMapper.selectByFilePath(filePath);
            if (existingIndex != null) {
                long lastModified = file.lastModified();
                if (existingIndex.getLastModified() != null && 
                    existingIndex.getLastModified().equals(String.valueOf(lastModified))) {
                    // 文件未修改，跳过
                    return;
                }
            }
            
            // 重新索引文件
            fileIndexService.indexFile(java.nio.file.Paths.get(filePath));
            
        } catch (Exception e) {
            logger.error("更新文件索引失败: {}", filePath, e);
        }
    }

    /**
     * 清理无效索引
     */
    private void cleanupInvalidIndexes() {
        try {
            // 删除不存在的文件索引
            int deletedCount = fileIndexMapper.deleteInvalidIndexes();
            logger.info("清理了 {} 个无效索引", deletedCount);
            
        } catch (Exception e) {
            logger.error("清理无效索引失败", e);
        }
    }

    /**
     * 清理旧索引
     */
    private void clearOldIndexes() {
        try {
            fileIndexMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
            logger.info("清理旧索引完成");
        } catch (Exception e) {
            logger.error("清理旧索引失败", e);
        }
    }

    /**
     * 更新索引统计信息
     */
    private void updateIndexStats() {
        try {
            // 总文件数
            long totalFiles = fileIndexMapper.selectCount(null);
            indexStats.put("totalFiles", totalFiles);
            
            // 已索引文件数
            long indexedFiles = fileIndexMapper.selectCount(null);
            indexStats.put("indexedFiles", indexedFiles);
            
            // 索引大小
            long indexSize = fileIndexMapper.getIndexSize();
            indexStats.put("indexSize", indexSize);
            
            // 最后更新时间
            indexStats.put("lastUpdateTime", System.currentTimeMillis());
            
            logger.info("索引统计信息更新完成");
            
        } catch (Exception e) {
            logger.error("更新索引统计信息失败", e);
        }
    }

    /**
     * 定时优化索引
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void scheduledIndexOptimization() {
        try {
            logger.info("开始定时索引优化");
            
            // 1. 增量更新
            updateIndexIncremental();
            
            // 2. 清理过期缓存
            // 这里可以调用OptimizedSearchService的清理方法
            
            // 3. 更新统计信息
            updateIndexStats();
            
            logger.info("定时索引优化完成");
            
        } catch (Exception e) {
            logger.error("定时索引优化失败", e);
        }
    }

    /**
     * 获取索引统计信息
     */
    public Map<String, Object> getIndexStats() {
        updateIndexStats();
        return new HashMap<>(indexStats);
    }

    /**
     * 获取性能指标
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("performanceMetrics", performanceMetrics);
        metrics.put("indexStats", indexStats);
        return metrics;
    }

    /**
     * 手动触发索引优化
     */
    public void triggerIndexOptimization() {
        try {
            logger.info("手动触发索引优化");
            
            // 异步执行优化
            rebuildIndexAsync();
            
        } catch (Exception e) {
            logger.error("手动触发索引优化失败", e);
        }
    }
}
