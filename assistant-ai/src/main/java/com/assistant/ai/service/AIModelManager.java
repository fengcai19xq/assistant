package com.assistant.ai.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * AI模型管理器
 * 负责模型下载、初始化和状态管理
 */
@Service
public class AIModelManager {
    
    private static final Logger logger = LoggerFactory.getLogger(AIModelManager.class);
    
    @Autowired
    private ONNXEmbeddingService onnxEmbeddingService;
    
    @Autowired
    private EmbeddingService embeddingService;
    
    @Value("${assistant.ai.embedding-model:models/all-MiniLM-L6-v2.onnx}")
    private String modelPath;
    
    @Value("${assistant.ai.auto-download-model:true}")
    private boolean autoDownloadModel;
    
    @Value("${assistant.ai.model-download-timeout:300000}")
    private long modelDownloadTimeout;
    
    private boolean modelInitialized = false;
    private String modelStatus = "unknown";
    
    @PostConstruct
    public void initialize() {
        try {
            logger.info("AI模型管理器初始化开始...");
            
            // 检查模型文件
            Path modelFilePath = Paths.get(modelPath);
            boolean modelExists = Files.exists(modelFilePath) && Files.size(modelFilePath) > 1024; // 至少1KB
            
            if (!modelExists && autoDownloadModel) {
                logger.info("模型文件不存在，开始自动下载...");
                downloadModel();
            }
            
            // 检查ONNX模型是否可用
            if (onnxEmbeddingService.isModelAvailable()) {
                modelStatus = "onnx_available";
                modelInitialized = true;
                logger.info("AI模型管理器初始化成功 (ONNX模式)");
            } else {
                modelStatus = "mock_mode";
                modelInitialized = true;
                logger.info("AI模型管理器初始化成功 (模拟模式)");
            }
            
        } catch (Exception e) {
            logger.error("AI模型管理器初始化失败", e);
            modelStatus = "error";
        }
    }
    
    /**
     * 下载模型文件
     */
    private void downloadModel() {
        try {
            // 这里可以集成真实的模型下载逻辑
            // 目前使用占位文件
            Path modelFilePath = Paths.get(modelPath);
            Files.createDirectories(modelFilePath.getParent());
            
            // 创建一个占位文件
            String placeholderContent = "ONNX Model Placeholder - " + System.currentTimeMillis();
            Files.write(modelFilePath, placeholderContent.getBytes());
            
            logger.info("模型占位文件创建完成: {}", modelPath);
            
        } catch (Exception e) {
            logger.error("模型下载失败", e);
        }
    }
    
    /**
     * 获取模型状态
     */
    public Map<String, Object> getModelStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("modelPath", modelPath);
        status.put("modelStatus", modelStatus);
        status.put("modelInitialized", modelInitialized);
        status.put("autoDownloadModel", autoDownloadModel);
        
        // 获取嵌入服务信息
        if (embeddingService != null) {
            status.put("embeddingService", embeddingService.getModelInfo());
        }
        
        // 获取ONNX服务信息
        if (onnxEmbeddingService != null) {
            status.put("onnxService", onnxEmbeddingService.getModelInfo());
        }
        
        return status;
    }
    
    /**
     * 检查模型是否可用
     */
    public boolean isModelAvailable() {
        return modelInitialized && ("onnx_available".equals(modelStatus) || "mock_mode".equals(modelStatus));
    }
    
    /**
     * 获取模型类型
     */
    public String getModelType() {
        if ("onnx_available".equals(modelStatus)) {
            return "ONNX";
        } else if ("mock_mode".equals(modelStatus)) {
            return "Mock";
        } else {
            return "Unknown";
        }
    }
    
    /**
     * 重新初始化模型
     */
    public void reinitializeModel() {
        try {
            logger.info("重新初始化AI模型...");
            modelInitialized = false;
            modelStatus = "reinitializing";
            
            // 重新检查ONNX模型
            if (onnxEmbeddingService.isModelAvailable()) {
                modelStatus = "onnx_available";
                modelInitialized = true;
                logger.info("模型重新初始化成功 (ONNX模式)");
            } else {
                modelStatus = "mock_mode";
                modelInitialized = true;
                logger.info("模型重新初始化成功 (模拟模式)");
            }
            
        } catch (Exception e) {
            logger.error("模型重新初始化失败", e);
            modelStatus = "error";
        }
    }
}
