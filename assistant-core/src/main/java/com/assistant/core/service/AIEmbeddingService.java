package com.assistant.core.service;

import com.assistant.common.constants.AssistantConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ai.onnxruntime.*;

/**
 * AI向量化服务
 */
@Service
public class AIEmbeddingService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIEmbeddingService.class);
    
    @Autowired
    private ModelDownloadService modelDownloadService;
    
    private OrtEnvironment env;
    private OrtSession session;
    private final Map<String, float[]> embeddingCache = new ConcurrentHashMap<>();
    
    /**
     * 初始化AI模型
     */
    public void initializeModel() {
        try {
            if (!modelDownloadService.isModelExists()) {
                logger.warn("AI模型文件不存在，跳过向量化功能");
                return;
            }
            
            String modelPath = modelDownloadService.getModelPath();
            logger.info("初始化AI模型: {}", modelPath);
            
            // 创建ONNX Runtime环境
            env = OrtEnvironment.getEnvironment();
            
            // 创建会话选项
            OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
            sessionOptions.setIntraOpNumThreads(2); // 限制线程数
            sessionOptions.setInterOpNumThreads(1);
            
            // 加载模型
            session = env.createSession(modelPath, sessionOptions);
            
            logger.info("AI模型初始化成功");
            
        } catch (Exception e) {
            logger.warn("AI模型初始化失败，将使用文本搜索: {}", e.getMessage());
            session = null;
        }
    }
    
    /**
     * 生成文本向量
     */
    public float[] generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        // 检查缓存
        String cacheKey = text.trim();
        if (embeddingCache.containsKey(cacheKey)) {
            return embeddingCache.get(cacheKey);
        }
        
        // 如果AI模型不可用，使用简单文本向量化
        if (session == null) {
            logger.debug("AI模型不可用，使用简单文本向量化");
            return generateSimpleEmbedding(text);
        }
        
        try {
            // 预处理文本
            String processedText = preprocessText(text);
            if (processedText.isEmpty()) {
                return null;
            }
            
            // 创建输入张量
            long[] shape = {1, 1}; // batch_size=1, sequence_length=1
            String[] inputArray = {processedText};
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputArray, shape);
            
            // 执行推理
            Map<String, OnnxTensor> inputs = new java.util.HashMap<>();
            inputs.put("input_ids", inputTensor);
            try (OrtSession.Result result = session.run(inputs)) {
                
                // 获取输出
                float[][] output = (float[][]) result.get(0).getValue();
                float[] embedding = output[0]; // 取第一个（也是唯一一个）输出
                
                // 缓存结果
                embeddingCache.put(cacheKey, embedding);
                
                logger.debug("生成向量成功，维度: {}", embedding.length);
                return embedding;
            }
            
        } catch (Exception e) {
            logger.warn("AI模型推理失败，使用简单文本向量化: {}", e.getMessage());
            return generateSimpleEmbedding(text);
        }
    }
    
    /**
     * 生成简单的文本向量（基于词频和TF-IDF）
     */
    private float[] generateSimpleEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        // 简单的文本预处理
        String processedText = text.toLowerCase()
            .replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s]", " ")
            .replaceAll("\\s+", " ")
            .trim();
        
        if (processedText.isEmpty()) {
            return null;
        }
        
        // 创建固定维度的向量（384维，与all-MiniLM-L6-v2相同）
        float[] embedding = new float[384];
        
        // 基于字符和词汇的简单特征
        String[] words = processedText.split("\\s+");
        int wordCount = words.length;
        int charCount = processedText.length();
        
        // 填充向量：使用文本特征
        for (int i = 0; i < embedding.length; i++) {
            if (i < words.length) {
                // 基于词汇的哈希值
                embedding[i] = (float) Math.sin(words[i].hashCode() * 0.01);
            } else if (i < 100) {
                // 基于字符频率
                embedding[i] = (float) Math.sin(charCount * 0.001 * i);
            } else {
                // 基于文本长度的其他特征
                embedding[i] = (float) Math.cos(wordCount * 0.01 * i);
            }
        }
        
        // 归一化向量
        return normalizeVector(embedding);
    }
    
    /**
     * 计算向量相似度
     */
    public double calculateSimilarity(float[] vector1, float[] vector2) {
        if (vector1 == null || vector2 == null || vector1.length != vector2.length) {
            return 0.0;
        }
        
        // 计算余弦相似度
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * 预处理文本
     */
    private String preprocessText(String text) {
        if (text == null) {
            return "";
        }
        
        // 清理文本
        String cleaned = text.trim()
            .replaceAll("\\s+", " ") // 合并多个空格
            .replaceAll("[\\r\\n]+", " ") // 替换换行符
            .replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", ""); // 移除特殊字符
        
        // 限制长度
        if (cleaned.length() > 512) {
            cleaned = cleaned.substring(0, 512);
        }
        
        return cleaned;
    }
    
    /**
     * 检查模型是否可用
     */
    public boolean isModelAvailable() {
        return session != null;
    }
    
    /**
     * 关闭资源
     */
    public void shutdown() {
        try {
            if (session != null) {
                session.close();
            }
            if (env != null) {
                env.close();
            }
            embeddingCache.clear();
            logger.info("AI向量化服务已关闭");
        } catch (Exception e) {
            logger.error("关闭AI向量化服务失败", e);
        }
    }
    
    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        return embeddingCache.size();
    }
    
    /**
     * 归一化向量
     */
    private float[] normalizeVector(float[] vector) {
        if (vector == null || vector.length == 0) {
            return vector;
        }
        
        double sum = 0;
        for (float v : vector) {
            sum += v * v;
        }
        double magnitude = Math.sqrt(sum);
        if (magnitude == 0) {
            return Arrays.copyOf(vector, vector.length); // 避免除以零
        }

        float[] normalizedVector = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalizedVector[i] = (float) (vector[i] / magnitude);
        }
        return normalizedVector;
    }
    
    /**
     * 清空缓存
     */
    public void clearCache() {
        embeddingCache.clear();
        logger.info("向量缓存已清空");
    }
}
