package com.assistant.storage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 向量存储服务
 * 基于内存的向量数据存储 (简化版)
 */
@Service
public class VectorStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(VectorStorageService.class);
    
    @Value("${assistant.storage.data-dir:${user.home}/.file-assistant/data}")
    private String dataDir;
    
    // 内存存储
    private Map<String, float[]> vectorCache = new ConcurrentHashMap<>();
    private Map<String, String> metadataCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initialize() {
        try {
            logger.info("向量存储服务初始化成功 (内存模式): {}", dataDir);
        } catch (Exception e) {
            logger.error("向量存储服务初始化失败", e);
            throw new RuntimeException("向量存储服务初始化失败", e);
        }
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            // 清理内存数据
            vectorCache.clear();
            metadataCache.clear();
            logger.info("向量存储服务清理完成");
        } catch (Exception e) {
            logger.error("向量存储服务清理失败", e);
        }
    }
    
    /**
     * 存储向量
     */
    public void storeVector(String id, float[] vector, String metadata) {
        try {
            // 直接存储到内存
            vectorCache.put(id, vector);
            metadataCache.put(id, metadata);
            
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
            // 直接存储到内存
            vectorCache.putAll(vectors);
            metadataCache.putAll(metadata);
            
            logger.info("批量存储向量: {} 个", vectors.size());
        } catch (Exception e) {
            logger.error("批量存储向量失败", e);
        }
    }
    
    /**
     * 获取向量
     */
    public float[] getVector(String id) {
        return vectorCache.get(id);
    }
    
    /**
     * 获取元数据
     */
    public String getMetadata(String id) {
        return metadataCache.get(id);
    }
    
    /**
     * 删除向量
     */
    public void removeVector(String id) {
        try {
            vectorCache.remove(id);
            metadataCache.remove(id);
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
            vectorCache.clear();
            metadataCache.clear();
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
        return stats;
    }
    
    /**
     * 获取索引大小
     */
    public int getIndexSize() {
        return vectorCache.size();
    }
}
