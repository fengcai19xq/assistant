package com.assistant.core.service;

import com.assistant.common.constants.AssistantConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * AI模型下载服务
 */
@Service
public class ModelDownloadService {
    
    private static final Logger logger = LoggerFactory.getLogger(ModelDownloadService.class);
    
    private static final String MODEL_URL = "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/onnx/model.onnx";
    private static final String MODEL_NAME = "all-MiniLM-L6-v2.onnx";
    
    /**
     * 检查并下载模型文件
     */
    public void ensureModelExists() {
        try {
            Path modelPath = Paths.get(AssistantConstants.MODEL_PATH, AssistantConstants.EMBEDDING_MODEL_NAME);
            File modelFile = modelPath.toFile();
            
            if (modelFile.exists() && modelFile.length() > 0) {
                logger.info("模型文件已存在: {}", modelPath);
                return;
            }
            
            logger.info("开始下载AI模型: {}", MODEL_URL);
            downloadModel(modelPath);
            logger.info("AI模型下载完成: {}", modelPath);
            
        } catch (Exception e) {
            logger.error("下载AI模型失败", e);
            // 创建占位文件，避免重复下载
            createPlaceholderModel();
        }
    }
    
    /**
     * 下载模型文件
     */
    private void downloadModel(Path modelPath) throws Exception {
        // 确保目录存在
        Files.createDirectories(modelPath.getParent());
        
        URL url = new URL(MODEL_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(300000);
        
        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(modelPath.toFile())) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
                
                if (totalBytes % (1024 * 1024) == 0) { // 每MB打印一次进度
                    logger.info("已下载: {} MB", totalBytes / (1024 * 1024));
                }
            }
            
            logger.info("模型下载完成，总大小: {} MB", totalBytes / (1024 * 1024));
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * 创建占位模型文件（用于测试）
     */
    private void createPlaceholderModel() {
        try {
            Path modelPath = Paths.get(AssistantConstants.MODEL_PATH, AssistantConstants.EMBEDDING_MODEL_NAME);
            Files.createDirectories(modelPath.getParent());
            
            // 创建一个小的占位文件
            Files.write(modelPath, "placeholder model for testing".getBytes());
            logger.warn("创建了占位模型文件，实际使用时请下载真实模型");
            
        } catch (Exception e) {
            logger.error("创建占位模型文件失败", e);
        }
    }
    
    /**
     * 检查模型文件是否存在
     */
    public boolean isModelExists() {
        Path modelPath = Paths.get(AssistantConstants.MODEL_PATH, AssistantConstants.EMBEDDING_MODEL_NAME);
        File modelFile = modelPath.toFile();
        return modelFile.exists() && modelFile.length() > 0;
    }
    
    /**
     * 获取模型文件路径
     */
    public String getModelPath() {
        return Paths.get(AssistantConstants.MODEL_PATH, AssistantConstants.EMBEDDING_MODEL_NAME).toString();
    }
}
