package com.assistant.ai.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI嵌入服务 - 文本向量化
 * 使用all-MiniLM-L6-v2模型进行文本嵌入
 * 集成ONNX Runtime进行真实AI推理
 */
@Service
public class EmbeddingService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);
    
    @Autowired
    private ONNXEmbeddingService onnxEmbeddingService;
    
    @Value("${assistant.ai.embedding-model:models/all-MiniLM-L6-v2.onnx}")
    private String modelPath;
    
    @Value("${assistant.ai.embedding-dimension:384}")
    private int embeddingDimension;
    
    @Value("${assistant.ai.max-concurrent-inference:2}")
    private int maxConcurrentInference;
    
    private ExecutorService executorService;
    private Random random = new Random(42); // 固定种子确保一致性
    
    @PostConstruct
    public void initialize() {
        try {
            executorService = Executors.newFixedThreadPool(maxConcurrentInference);
            
            // 检查ONNX模型是否可用
            if (onnxEmbeddingService.isModelAvailable()) {
                logger.info("嵌入服务初始化成功 (ONNX模式): {}", modelPath);
            } else {
                logger.info("嵌入服务初始化成功 (模拟模式): {}", modelPath);
            }
        } catch (Exception e) {
            logger.error("嵌入服务初始化失败", e);
            throw new RuntimeException("嵌入服务初始化失败", e);
        }
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            if (executorService != null) {
                executorService.shutdown();
            }
        } catch (Exception e) {
            logger.error("嵌入服务清理失败", e);
        }
    }
    
    /**
     * 生成文本嵌入向量 (优先使用ONNX模型)
     */
    public CompletableFuture<float[]> generateEmbedding(String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (StringUtils.isBlank(text)) {
                    return new float[embeddingDimension];
                }
                
                // 优先使用ONNX模型
                if (onnxEmbeddingService.isModelAvailable()) {
                    return onnxEmbeddingService.generateEmbedding(text).get();
                } else {
                    // 回退到模拟实现
                    String processedText = preprocessText(text);
                    float[] embedding = generateMockEmbedding(processedText);
                    
                    logger.debug("生成嵌入向量 (模拟模式): text={}, dimension={}", 
                        processedText.substring(0, Math.min(50, processedText.length())), 
                        embedding.length);
                    
                    return embedding;
                }
                
            } catch (Exception e) {
                logger.error("生成嵌入向量失败: {}", text, e);
                return new float[embeddingDimension];
            }
        }, executorService);
    }
    
    /**
     * 批量生成文本嵌入向量
     */
    public CompletableFuture<List<float[]>> generateEmbeddings(List<String> texts) {
        return CompletableFuture.supplyAsync(() -> {
            // 优先使用ONNX模型的批量处理
            if (onnxEmbeddingService.isModelAvailable()) {
                try {
                    return onnxEmbeddingService.generateEmbeddings(texts).get();
                } catch (Exception e) {
                    logger.warn("ONNX批量处理失败，回退到逐个处理", e);
                }
            }
            
            // 回退到逐个处理
            List<float[]> embeddings = new ArrayList<>();
            for (String text : texts) {
                try {
                    float[] embedding = generateEmbedding(text).get();
                    embeddings.add(embedding);
                } catch (Exception e) {
                    logger.error("批量生成嵌入向量失败", e);
                    embeddings.add(new float[embeddingDimension]);
                }
            }
            return embeddings;
        }, executorService);
    }
    
    /**
     * 计算向量相似度
     */
    public double calculateSimilarity(float[] vector1, float[] vector2) {
        // 优先使用ONNX服务的相似度计算
        if (onnxEmbeddingService.isModelAvailable()) {
            return onnxEmbeddingService.calculateSimilarity(vector1, vector2);
        }
        
        // 回退到本地计算
        if (vector1 == null || vector2 == null || vector1.length != vector2.length) {
            return 0.0;
        }
        
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
        if (StringUtils.isBlank(text)) {
            return "";
        }
        
        // 清理文本
        String cleaned = text.trim()
            .replaceAll("\\s+", " ")  // 合并多个空格
            .replaceAll("[\\x00-\\x1F\\x7F]", "");  // 移除控制字符
        
        // 限制长度
        if (cleaned.length() > 512) {
            cleaned = cleaned.substring(0, 512);
        }
        
        return cleaned;
    }
    
    /**
     * 获取嵌入维度
     */
    public int getEmbeddingDimension() {
        return embeddingDimension;
    }
    
    /**
     * 生成模拟嵌入向量
     * 基于文本内容生成确定性的向量
     */
    private float[] generateMockEmbedding(String text) {
        float[] embedding = new float[embeddingDimension];
        
        // 基于文本内容生成确定性向量
        int hash = text.hashCode();
        Random textRandom = new Random(hash);
        
        for (int i = 0; i < embeddingDimension; i++) {
            embedding[i] = (float) (textRandom.nextGaussian() * 0.1);
        }
        
        // 归一化向量
        float norm = 0.0f;
        for (float value : embedding) {
            norm += value * value;
        }
        norm = (float) Math.sqrt(norm);
        
        if (norm > 0) {
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] /= norm;
            }
        }
        
        return embedding;
    }
    
    /**
     * 获取模型信息
     */
    public Map<String, Object> getModelInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("modelPath", modelPath);
        info.put("embeddingDimension", embeddingDimension);
        info.put("maxConcurrentInference", maxConcurrentInference);
        
        // 获取ONNX模型信息
        if (onnxEmbeddingService != null) {
            Map<String, Object> onnxInfo = onnxEmbeddingService.getModelInfo();
            info.putAll(onnxInfo);
        }
        
        return info;
    }
}
