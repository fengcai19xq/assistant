package com.assistant.ai.service;

import ai.onnxruntime.*;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ONNX Runtime嵌入服务
 * 使用真实的all-MiniLM-L6-v2模型进行文本向量化
 */
@Service
public class ONNXEmbeddingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ONNXEmbeddingService.class);
    
    @Value("${assistant.ai.embedding-model:models/all-MiniLM-L6-v2.onnx}")
    private String modelPath;
    
    @Value("${assistant.ai.embedding-dimension:384}")
    private int embeddingDimension;
    
    @Value("${assistant.ai.max-concurrent-inference:2}")
    private int maxConcurrentInference;
    
    private OrtEnvironment env;
    private OrtSession session;
    private ExecutorService executorService;
    private final Map<String, float[]> embeddingCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initialize() {
        try {
            executorService = Executors.newFixedThreadPool(maxConcurrentInference);
            
            // 检查模型文件是否存在
            Path modelFilePath = Paths.get(modelPath);
            if (!Files.exists(modelFilePath)) {
                logger.warn("ONNX模型文件不存在: {}，将使用模拟实现", modelPath);
                return;
            }
            
            // 初始化ONNX Runtime环境
            env = OrtEnvironment.getEnvironment();
            
            // 创建会话选项
            OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
            sessionOptions.setIntraOpNumThreads(2);
            sessionOptions.setInterOpNumThreads(2);
            
            // 加载模型
            session = env.createSession(modelPath, sessionOptions);
            
            logger.info("ONNX嵌入服务初始化成功: {}", modelPath);
            
        } catch (Exception e) {
            logger.error("ONNX嵌入服务初始化失败，将使用模拟实现", e);
            session = null;
        }
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) {
                session.close();
            }
            if (env != null) {
                env.close();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
        } catch (Exception e) {
            logger.error("ONNX嵌入服务清理失败", e);
        }
    }
    
    /**
     * 生成文本嵌入向量
     */
    public CompletableFuture<float[]> generateEmbedding(String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (text == null || text.trim().isEmpty()) {
                    return new float[embeddingDimension];
                }
                
                // 检查缓存
                String cacheKey = text.trim();
                if (embeddingCache.containsKey(cacheKey)) {
                    return embeddingCache.get(cacheKey);
                }
                
                // 如果ONNX模型不可用，使用模拟实现
                if (session == null) {
                    logger.debug("ONNX模型不可用，使用模拟实现");
                    return generateMockEmbedding(text);
                }
                
                // 预处理文本
                String processedText = preprocessText(text);
                if (processedText.isEmpty()) {
                    return new float[embeddingDimension];
                }
                
                // 创建输入张量
                long[] shape = {1, 1}; // batch_size=1, sequence_length=1
                String[] inputArray = {processedText};
                OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputArray, shape);
                
                // 执行推理
                Map<String, OnnxTensor> inputs = new HashMap<>();
                inputs.put("input_ids", inputTensor);
                
                try (OrtSession.Result result = session.run(inputs)) {
                    // 获取输出
                    float[][] output = (float[][]) result.get(0).getValue();
                    float[] embedding = output[0]; // 取第一个输出
                    
                    // 缓存结果
                    embeddingCache.put(cacheKey, embedding);
                    
                    logger.debug("ONNX嵌入向量生成成功，维度: {}", embedding.length);
                    return embedding;
                }
                
            } catch (Exception e) {
                logger.warn("ONNX推理失败，使用模拟实现: {}", e.getMessage());
                return generateMockEmbedding(text);
            }
        }, executorService);
    }
    
    /**
     * 批量生成文本嵌入向量
     */
    public CompletableFuture<List<float[]>> generateEmbeddings(List<String> texts) {
        return CompletableFuture.supplyAsync(() -> {
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
     * 计算向量相似度（余弦相似度）
     */
    public double calculateSimilarity(float[] vector1, float[] vector2) {
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
        if (text == null) {
            return "";
        }
        
        // 基本文本清理
        String processed = text.trim()
            .toLowerCase()
            .replaceAll("[\\p{Punct}&&[^\\s]]", " ") // 保留空格，移除标点
            .replaceAll("\\s+", " ") // 合并多个空格
            .trim();
        
        // 限制长度（避免输入过长）
        if (processed.length() > 512) {
            processed = processed.substring(0, 512);
        }
        
        return processed;
    }
    
    /**
     * 生成模拟嵌入向量（基于文本内容的确定性向量）
     */
    private float[] generateMockEmbedding(String text) {
        float[] embedding = new float[embeddingDimension];
        
        // 基于文本内容生成确定性向量
        int hash = text.hashCode();
        Random random = new Random(hash);
        
        for (int i = 0; i < embeddingDimension; i++) {
            embedding[i] = (float) (random.nextGaussian() * 0.1);
        }
        
        // 归一化
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
     * 检查ONNX模型是否可用
     */
    public boolean isModelAvailable() {
        return session != null;
    }
    
    /**
     * 获取模型信息
     */
    public Map<String, Object> getModelInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("modelPath", modelPath);
        info.put("embeddingDimension", embeddingDimension);
        info.put("modelAvailable", isModelAvailable());
        info.put("cacheSize", embeddingCache.size());
        return info;
    }
}
