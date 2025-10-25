package com.assistant.storage.service;

import com.assistant.core.entity.FileIndex;
import com.assistant.core.mapper.FileIndexMapper;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 持久化文件索引存储服务
 * 结合SQLite和RocksDB的混合存储方案
 */
@Service
public class PersistentFileIndexService {
    
    private static final Logger logger = LoggerFactory.getLogger(PersistentFileIndexService.class);
    
    @Autowired
    private FileIndexMapper fileIndexMapper;
    
    @Autowired
    private PersistentVectorStorageService vectorStorageService;
    
    @Value("${assistant.storage.data-dir:${user.home}/.file-assistant/data}")
    private String dataDir;
    
    @Value("${assistant.storage.enable-vector-cache:true}")
    private boolean enableVectorCache;
    
    @Value("${assistant.storage.cache-size:10000}")
    private int cacheSize;
    
    // 文件索引缓存
    private Map<String, FileIndex> fileIndexCache = new ConcurrentHashMap<>();
    private Map<String, Long> lastModifiedCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initialize() {
        try {
            // 创建数据目录
            Path dataPath = Paths.get(dataDir);
            Files.createDirectories(dataPath);
            
            // 加载文件索引到缓存
            loadFileIndexToCache();
            
            logger.info("持久化文件索引存储服务初始化成功: {}", dataDir);
            
        } catch (Exception e) {
            logger.error("持久化文件索引存储服务初始化失败", e);
        }
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            // 保存缓存数据
            saveCacheToDatabase();
            logger.info("持久化文件索引存储服务清理完成");
        } catch (Exception e) {
            logger.error("持久化文件索引存储服务清理失败", e);
        }
    }
    
    /**
     * 加载文件索引到缓存
     */
    private void loadFileIndexToCache() {
        try {
            List<FileIndex> allFiles = fileIndexMapper.selectAllWithoutVectorData();
            
            for (FileIndex file : allFiles) {
                fileIndexCache.put(file.getFilePath(), file);
                lastModifiedCache.put(file.getFilePath(), 
                    file.getLastModified() != null ? Long.parseLong(file.getLastModified()) : 0L);
            }
            
            logger.info("文件索引缓存加载完成: {} 个文件", fileIndexCache.size());
            
        } catch (Exception e) {
            logger.error("加载文件索引到缓存失败", e);
        }
    }
    
    /**
     * 保存缓存到数据库
     */
    private void saveCacheToDatabase() {
        try {
            // 这里可以实现增量保存逻辑
            logger.debug("保存缓存数据到数据库");
        } catch (Exception e) {
            logger.error("保存缓存数据失败", e);
        }
    }
    
    /**
     * 存储文件索引
     */
    public void storeFileIndex(FileIndex fileIndex) {
        try {
            // 存储到数据库
            if (fileIndex.getId() == null) {
                fileIndexMapper.insert(fileIndex);
            } else {
                fileIndexMapper.updateById(fileIndex);
            }
            
            // 更新缓存
            fileIndexCache.put(fileIndex.getFilePath(), fileIndex);
            lastModifiedCache.put(fileIndex.getFilePath(), 
                fileIndex.getLastModified() != null ? Long.parseLong(fileIndex.getLastModified()) : 0L);
            
            // 存储向量数据
            if (fileIndex.getVectorData() != null && enableVectorCache) {
                float[] vector = deserializeVectorData(fileIndex.getVectorData());
                String metadata = String.format("file:%s:type:%s:size:%d", 
                    fileIndex.getFilePath(), fileIndex.getFileType(), fileIndex.getFileSize());
                vectorStorageService.storeVector(fileIndex.getFilePath(), vector, metadata);
            }
            
            logger.debug("存储文件索引: {}", fileIndex.getFilePath());
            
        } catch (Exception e) {
            logger.error("存储文件索引失败: {}", fileIndex.getFilePath(), e);
        }
    }
    
    /**
     * 批量存储文件索引
     */
    public void storeFileIndexes(List<FileIndex> fileIndexes) {
        try {
            for (FileIndex fileIndex : fileIndexes) {
                storeFileIndex(fileIndex);
            }
            
            logger.info("批量存储文件索引: {} 个", fileIndexes.size());
            
        } catch (Exception e) {
            logger.error("批量存储文件索引失败", e);
        }
    }
    
    /**
     * 获取文件索引
     */
    public FileIndex getFileIndex(String filePath) {
        // 优先从缓存获取
        FileIndex fileIndex = fileIndexCache.get(filePath);
        if (fileIndex != null) {
            return fileIndex;
        }
        
        // 从数据库获取
        try {
            fileIndex = fileIndexMapper.selectByFilePath(filePath);
            if (fileIndex != null) {
                fileIndexCache.put(filePath, fileIndex);
                lastModifiedCache.put(filePath, 
                    fileIndex.getLastModified() != null ? Long.parseLong(fileIndex.getLastModified()) : 0L);
            }
            return fileIndex;
        } catch (Exception e) {
            logger.error("获取文件索引失败: {}", filePath, e);
            return null;
        }
    }
    
    /**
     * 删除文件索引
     */
    public void removeFileIndex(String filePath) {
        try {
            // 从数据库删除
            fileIndexMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FileIndex>()
                .eq("file_path", filePath));
            
            // 从缓存删除
            fileIndexCache.remove(filePath);
            lastModifiedCache.remove(filePath);
            
            // 删除向量数据
            vectorStorageService.removeVector(filePath);
            
            logger.debug("删除文件索引: {}", filePath);
            
        } catch (Exception e) {
            logger.error("删除文件索引失败: {}", filePath, e);
        }
    }
    
    /**
     * 更新文件索引
     */
    public void updateFileIndex(FileIndex fileIndex) {
        try {
            // 更新数据库
            fileIndexMapper.updateById(fileIndex);
            
            // 更新缓存
            fileIndexCache.put(fileIndex.getFilePath(), fileIndex);
            lastModifiedCache.put(fileIndex.getFilePath(), 
                fileIndex.getLastModified() != null ? Long.parseLong(fileIndex.getLastModified()) : 0L);
            
            // 更新向量数据
            if (fileIndex.getVectorData() != null && enableVectorCache) {
                float[] vector = deserializeVectorData(fileIndex.getVectorData());
                String metadata = String.format("file:%s:type:%s:size:%d", 
                    fileIndex.getFilePath(), fileIndex.getFileType(), fileIndex.getFileSize());
                vectorStorageService.storeVector(fileIndex.getFilePath(), vector, metadata);
            }
            
            logger.debug("更新文件索引: {}", fileIndex.getFilePath());
            
        } catch (Exception e) {
            logger.error("更新文件索引失败: {}", fileIndex.getFilePath(), e);
        }
    }
    
    /**
     * 检查文件是否需要重新索引
     */
    public boolean needsReindexing(String filePath, long lastModified) {
        Long cachedLastModified = lastModifiedCache.get(filePath);
        return cachedLastModified == null || cachedLastModified < lastModified;
    }
    
    /**
     * 获取所有文件路径
     */
    public Set<String> getAllFilePaths() {
        return fileIndexCache.keySet();
    }
    
    /**
     * 清空所有文件索引
     */
    public void clearAllFileIndexes() {
        try {
            // 清空数据库
            fileIndexMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
            
            // 清空缓存
            fileIndexCache.clear();
            lastModifiedCache.clear();
            
            // 清空向量数据
            vectorStorageService.clearAllVectors();
            
            logger.info("清空所有文件索引");
            
        } catch (Exception e) {
            logger.error("清空所有文件索引失败", e);
        }
    }
    
    /**
     * 获取存储统计信息
     */
    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("fileIndexCacheSize", fileIndexCache.size());
        stats.put("totalFiles", getAllFilePaths().size());
        stats.put("enableVectorCache", enableVectorCache);
        stats.put("cacheSize", cacheSize);
        
        // 获取向量存储统计
        Map<String, Object> vectorStats = vectorStorageService.getStorageStats();
        stats.putAll(vectorStats);
        
        return stats;
    }
    
    /**
     * 序列化向量数据
     */
    private byte[] serializeVectorData(float[] vector) {
        byte[] bytes = new byte[vector.length * 4];
        for (int i = 0; i < vector.length; i++) {
            int bits = Float.floatToIntBits(vector[i]);
            bytes[i * 4] = (byte) (bits >> 24);
            bytes[i * 4 + 1] = (byte) (bits >> 16);
            bytes[i * 4 + 2] = (byte) (bits >> 8);
            bytes[i * 4 + 3] = (byte) bits;
        }
        return bytes;
    }
    
    /**
     * 反序列化向量数据
     */
    private float[] deserializeVectorData(byte[] bytes) {
        float[] vector = new float[bytes.length / 4];
        for (int i = 0; i < vector.length; i++) {
            int bits = ((bytes[i * 4] & 0xFF) << 24) |
                      ((bytes[i * 4 + 1] & 0xFF) << 16) |
                      ((bytes[i * 4 + 2] & 0xFF) << 8) |
                      (bytes[i * 4 + 3] & 0xFF);
            vector[i] = Float.intBitsToFloat(bits);
        }
        return vector;
    }
    
    /**
     * 压缩存储
     */
    public void compactStorage() {
        try {
            // 压缩向量存储
            vectorStorageService.compactDatabase();
            
            // 这里可以添加SQLite的VACUUM操作
            logger.info("存储压缩完成");
            
        } catch (Exception e) {
            logger.error("存储压缩失败", e);
        }
    }
}
