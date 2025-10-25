package com.assistant.storage.service;

import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 持久化向量存储服务
 * 基于RocksDB的高性能向量数据存储
 */
@Service
public class PersistentVectorStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(PersistentVectorStorageService.class);
    
    @Value("${assistant.storage.data-dir:${user.home}/.file-assistant/data}")
    private String dataDir;
    
    @Value("${assistant.storage.rocksdb.max-background-jobs:4}")
    private int maxBackgroundJobs;
    
    @Value("${assistant.storage.rocksdb.write-buffer-size:67108864}")
    private long writeBufferSize;
    
    @Value("${assistant.storage.rocksdb.max-write-buffer-number:3}")
    private int maxWriteBufferNumber;
    
    private RocksDB rocksDB;
    private Options options;
    private String dbPath;
    
    // 内存缓存
    private Map<String, float[]> vectorCache = new ConcurrentHashMap<>();
    private Map<String, String> metadataCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initialize() {
        try {
            // 创建数据库目录
            dbPath = Paths.get(dataDir, "vectors").toString();
            Files.createDirectories(Paths.get(dbPath));
            
            // 配置RocksDB选项
            options = new Options();
            options.setCreateIfMissing(true);
            options.setMaxBackgroundJobs(maxBackgroundJobs);
            options.setWriteBufferSize(writeBufferSize);
            options.setMaxWriteBufferNumber(maxWriteBufferNumber);
            options.setCompressionType(CompressionType.LZ4_COMPRESSION);
            options.setLevel0FileNumCompactionTrigger(4);
            options.setLevel0StopWritesTrigger(20);
            options.setMaxBytesForLevelBase(268435456); // 256MB
            options.setTargetFileSizeBase(67108864); // 64MB
            
            // 打开数据库
            rocksDB = RocksDB.open(options, dbPath);
            
            // 加载现有数据到缓存
            loadDataToCache();
            
            logger.info("持久化向量存储服务初始化成功: {}", dbPath);
            
        } catch (Exception e) {
            logger.error("持久化向量存储服务初始化失败", e);
            // 回退到内存模式
            initializeMemoryMode();
        }
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            if (rocksDB != null) {
                rocksDB.close();
            }
            if (options != null) {
                options.close();
            }
            logger.info("持久化向量存储服务清理完成");
        } catch (Exception e) {
            logger.error("持久化向量存储服务清理失败", e);
        }
    }
    
    /**
     * 回退到内存模式
     */
    private void initializeMemoryMode() {
        try {
            logger.warn("RocksDB初始化失败，回退到内存模式");
            rocksDB = null;
            options = null;
            logger.info("向量存储服务初始化成功 (内存模式): {}", dataDir);
        } catch (Exception e) {
            logger.error("内存模式初始化失败", e);
        }
    }
    
    /**
     * 加载数据到缓存
     */
    private void loadDataToCache() {
        if (rocksDB == null) return;
        
        try {
            RocksIterator iterator = rocksDB.newIterator();
            int count = 0;
            
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                String key = new String(iterator.key());
                byte[] value = iterator.value();
                
                if (key.startsWith("vector:")) {
                    String id = key.substring(7);
                    float[] vector = deserializeVector(value);
                    vectorCache.put(id, vector);
                } else if (key.startsWith("metadata:")) {
                    String id = key.substring(9);
                    String metadata = new String(value);
                    metadataCache.put(id, metadata);
                }
                
                count++;
                if (count % 1000 == 0) {
                    logger.debug("已加载 {} 条记录到缓存", count);
                }
            }
            
            iterator.close();
            logger.info("数据加载完成，缓存大小: {} 个向量", vectorCache.size());
            
        } catch (Exception e) {
            logger.error("加载数据到缓存失败", e);
        }
    }
    
    /**
     * 存储向量
     */
    public void storeVector(String id, float[] vector, String metadata) {
        try {
            // 存储到缓存
            vectorCache.put(id, vector);
            metadataCache.put(id, metadata);
            
            // 存储到RocksDB
            if (rocksDB != null) {
                byte[] vectorBytes = serializeVector(vector);
                rocksDB.put(("vector:" + id).getBytes(), vectorBytes);
                rocksDB.put(("metadata:" + id).getBytes(), metadata.getBytes());
            }
            
            logger.debug("存储向量: {}", id);
        } catch (Exception e) {
            logger.error("存储向量失败: {}", id, e);
        }
    }
    
    /**
     * 批量存储向量
     */
    public void storeVectors(Map<String, float[]> vectors, Map<String, String> metadata) {
        try {
            // 存储到缓存
            vectorCache.putAll(vectors);
            metadataCache.putAll(metadata);
            
            // 批量存储到RocksDB
            if (rocksDB != null) {
                WriteBatch batch = new WriteBatch();
                
                for (Map.Entry<String, float[]> entry : vectors.entrySet()) {
                    String id = entry.getKey();
                    float[] vector = entry.getValue();
                    byte[] vectorBytes = serializeVector(vector);
                    batch.put(("vector:" + id).getBytes(), vectorBytes);
                }
                
                for (Map.Entry<String, String> entry : metadata.entrySet()) {
                    String id = entry.getKey();
                    String metadataValue = entry.getValue();
                    batch.put(("metadata:" + id).getBytes(), metadataValue.getBytes());
                }
                
                rocksDB.write(new WriteOptions(), batch);
                batch.close();
            }
            
            logger.info("批量存储向量: {} 个", vectors.size());
        } catch (Exception e) {
            logger.error("批量存储向量失败", e);
        }
    }
    
    /**
     * 获取向量
     */
    public float[] getVector(String id) {
        // 优先从缓存获取
        float[] vector = vectorCache.get(id);
        if (vector != null) {
            return vector;
        }
        
        // 从RocksDB获取
        if (rocksDB != null) {
            try {
                byte[] vectorBytes = rocksDB.get(("vector:" + id).getBytes());
                if (vectorBytes != null) {
                    vector = deserializeVector(vectorBytes);
                    vectorCache.put(id, vector); // 更新缓存
                    return vector;
                }
            } catch (Exception e) {
                logger.error("从RocksDB获取向量失败: {}", id, e);
            }
        }
        
        return null;
    }
    
    /**
     * 获取元数据
     */
    public String getMetadata(String id) {
        // 优先从缓存获取
        String metadata = metadataCache.get(id);
        if (metadata != null) {
            return metadata;
        }
        
        // 从RocksDB获取
        if (rocksDB != null) {
            try {
                byte[] metadataBytes = rocksDB.get(("metadata:" + id).getBytes());
                if (metadataBytes != null) {
                    metadata = new String(metadataBytes);
                    metadataCache.put(id, metadata); // 更新缓存
                    return metadata;
                }
            } catch (Exception e) {
                logger.error("从RocksDB获取元数据失败: {}", id, e);
            }
        }
        
        return null;
    }
    
    /**
     * 删除向量
     */
    public void removeVector(String id) {
        try {
            // 从缓存删除
            vectorCache.remove(id);
            metadataCache.remove(id);
            
            // 从RocksDB删除
            if (rocksDB != null) {
                rocksDB.delete(("vector:" + id).getBytes());
                rocksDB.delete(("metadata:" + id).getBytes());
            }
            
            logger.debug("删除向量: {}", id);
        } catch (Exception e) {
            logger.error("删除向量失败: {}", id, e);
        }
    }
    
    /**
     * 获取所有向量ID
     */
    public Set<String> getAllVectorIds() {
        return vectorCache.keySet();
    }
    
    /**
     * 清空所有向量
     */
    public void clearAllVectors() {
        try {
            // 清空缓存
            vectorCache.clear();
            metadataCache.clear();
            
            // 清空RocksDB
            if (rocksDB != null) {
                RocksIterator iterator = rocksDB.newIterator();
                WriteBatch batch = new WriteBatch();
                
                for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                    batch.delete(iterator.key());
                }
                
                rocksDB.write(new WriteOptions(), batch);
                batch.close();
                iterator.close();
            }
            
            logger.info("清空所有向量数据");
        } catch (Exception e) {
            logger.error("清空所有向量失败", e);
        }
    }
    
    /**
     * 获取存储统计信息
     */
    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", vectorCache.size());
        stats.put("totalVectors", getAllVectorIds().size());
        stats.put("storageType", rocksDB != null ? "RocksDB" : "Memory");
        stats.put("dbPath", dbPath);
        
        if (rocksDB != null) {
            try {
                String statsString = rocksDB.getProperty("rocksdb.stats");
                stats.put("rocksdbStats", statsString);
            } catch (Exception e) {
                logger.warn("获取RocksDB统计信息失败", e);
            }
        }
        
        return stats;
    }
    
    /**
     * 获取索引大小
     */
    public int getIndexSize() {
        return vectorCache.size();
    }
    
    /**
     * 序列化向量
     */
    private byte[] serializeVector(float[] vector) {
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
     * 反序列化向量
     */
    private float[] deserializeVector(byte[] bytes) {
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
     * 检查RocksDB是否可用
     */
    public boolean isRocksDBAvailable() {
        return rocksDB != null;
    }
    
    /**
     * 压缩数据库
     */
    public void compactDatabase() {
        if (rocksDB != null) {
            try {
                rocksDB.compactRange();
                logger.info("数据库压缩完成");
            } catch (Exception e) {
                logger.error("数据库压缩失败", e);
            }
        }
    }
}
