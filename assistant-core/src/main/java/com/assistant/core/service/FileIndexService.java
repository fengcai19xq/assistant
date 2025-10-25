package com.assistant.core.service;

import com.assistant.common.dto.FileInfo;
import com.assistant.core.entity.FileIndex;
import com.assistant.core.mapper.FileIndexMapper;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.LocalDateTime;

/**
 * 文件索引服务
 */
@Service
public class FileIndexService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileIndexService.class);
    
    @Autowired
    private FileIndexMapper fileIndexMapper;
    
    @Autowired
    private ModelDownloadService modelDownloadService;
    
    @Autowired
    private AIEmbeddingService aiEmbeddingService;
    
    private final Tika tika = new Tika();
    
    /**
     * 索引指定文件夹
     */
    public int indexFolder(String folderPath, boolean recursive) {
        logger.info("开始索引文件夹: {}, 递归: {}", folderPath, recursive);
        
        Path path = Paths.get(folderPath);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            logger.warn("文件夹不存在或不是目录: {}", folderPath);
            return 0;
        }
        
        int indexedCount = 0;
        try {
            indexedCount = indexDirectory(path, recursive);
            logger.info("文件夹索引完成，共索引 {} 个文件", indexedCount);
        } catch (Exception e) {
            logger.error("索引文件夹失败: {}", folderPath, e);
        }
        
        return indexedCount;
    }
    
    /**
     * 递归索引目录
     */
    private int indexDirectory(Path directory, boolean recursive) throws Exception {
        int count = 0;
        
        try (Stream<Path> stream = Files.list(directory)) {
            for (Path path : stream.collect(Collectors.toList())) {
                if (Files.isDirectory(path)) {
                    if (recursive) {
                        count += indexDirectory(path, true);
                    }
                } else if (Files.isRegularFile(path)) {
                    if (indexFile(path)) {
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    /**
     * 索引单个文件
     */
    public boolean indexFile(Path filePath) {
        try {
            // 检查文件是否应该被索引
            if (!shouldIndex(filePath)) {
                return false;
            }
            
            // 检查文件是否已存在
            FileIndex existing = fileIndexMapper.selectByFilePath(filePath.toString());
            if (existing != null) {
                // 检查文件是否已修改
                File file = filePath.toFile();
                try {
                    // 解析时间戳，处理微秒精度
                    String lastModifiedStr = existing.getLastModified();
                    if (lastModifiedStr.length() > 26) {
                        // 截断微秒部分，只保留到毫秒
                        lastModifiedStr = lastModifiedStr.substring(0, 26);
                    }
                    if (file.lastModified() <= java.time.Instant.parse(lastModifiedStr).toEpochMilli()) {
                        return false; // 文件未修改，跳过
                    }
                } catch (Exception e) {
                    // 如果解析失败，强制重新索引
                    logger.warn("解析时间戳失败，强制重新索引: " + existing.getLastModified(), e);
                }
            }
            
            // 提取文件内容
            String content = extractFileContent(filePath);
            if (content == null || content.trim().isEmpty()) {
                return false;
            }
            
            // 创建文件索引记录
            FileIndex fileIndex = new FileIndex();
            fileIndex.setFilePath(filePath.toString());
            fileIndex.setFileName(filePath.getFileName().toString());
            fileIndex.setFileSize(Files.size(filePath));
            fileIndex.setFileType(getFileType(filePath.getFileName().toString()));
            fileIndex.setLastModified(LocalDateTime.now().toString());
            fileIndex.setContent(content);
            fileIndex.setSummary(generateSummary(content));
            
            // 生成AI向量
            if (aiEmbeddingService.isModelAvailable()) {
                try {
                    float[] embedding = aiEmbeddingService.generateEmbedding(content);
                    if (embedding != null) {
                        // 将float数组转换为byte数组存储
                        byte[] vectorData = new byte[embedding.length * 4];
                        for (int i = 0; i < embedding.length; i++) {
                            int intBits = Float.floatToIntBits(embedding[i]);
                            vectorData[i * 4] = (byte) (intBits >> 24);
                            vectorData[i * 4 + 1] = (byte) (intBits >> 16);
                            vectorData[i * 4 + 2] = (byte) (intBits >> 8);
                            vectorData[i * 4 + 3] = (byte) intBits;
                        }
                        fileIndex.setVectorData(vectorData);
                        logger.debug("生成AI向量成功，维度: {}", embedding.length);
                    }
                } catch (Exception e) {
                    logger.warn("生成AI向量失败: {}", filePath, e);
                }
            }
            
            // 保存或更新索引
            if (existing != null) {
                fileIndex.setId(existing.getId());
                fileIndexMapper.updateById(fileIndex);
            } else {
                fileIndexMapper.insert(fileIndex);
            }
            
            logger.debug("成功索引文件: {}", filePath);
            return true;
            
        } catch (Exception e) {
            logger.error("索引文件失败: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * 提取文件内容
     */
    private String extractFileContent(Path filePath) {
        try {
            // 使用Apache Tika提取文本内容
            String content = tika.parseToString(filePath.toFile());
            
            // 限制内容长度，避免数据库过大
            if (content.length() > 100000) { // 100KB限制
                content = content.substring(0, 100000) + "...";
            }
            
            return content.trim();
            
        } catch (Exception e) {
            logger.warn("提取文件内容失败: {}", filePath, e);
            return null;
        }
    }
    
    /**
     * 生成文件摘要
     */
    private String generateSummary(String content) {
        if (content == null || content.length() <= 200) {
            return content;
        }
        
        // 简单截取前200个字符作为摘要
        String summary = content.substring(0, 200);
        if (content.length() > 200) {
            summary += "...";
        }
        
        return summary;
    }
    
    /**
     * 删除文件索引
     */
    public boolean deleteFileIndex(String filePath) {
        try {
            FileIndex existing = fileIndexMapper.selectByFilePath(filePath);
            if (existing != null) {
                fileIndexMapper.deleteById(existing.getId());
                logger.debug("删除文件索引: {}", filePath);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("删除文件索引失败: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * 获取文件信息
     */
    public FileInfo getFileInfo(String filePath) {
        try {
            FileIndex fileIndex = fileIndexMapper.selectByFilePath(filePath);
            if (fileIndex == null) {
                return null;
            }
            
            FileInfo fileInfo = new FileInfo();
            fileInfo.setId(fileIndex.getId());
            fileInfo.setFilePath(fileIndex.getFilePath());
            fileInfo.setFileName(fileIndex.getFileName());
            fileInfo.setFileSize(fileIndex.getFileSize());
            fileInfo.setFileType(fileIndex.getFileType());
            fileInfo.setLastModified(fileIndex.getLastModified());
            fileInfo.setIndexedTime(fileIndex.getIndexedTime());
            fileInfo.setFolderId(fileIndex.getFolderId());
            fileInfo.setContent(fileIndex.getContent());
            fileInfo.setSummary(fileIndex.getSummary());
            
            return fileInfo;
            
        } catch (Exception e) {
            logger.error("获取文件信息失败: {}", filePath, e);
            return null;
        }
    }
    
    /**
     * 获取所有文件列表
     */
    public List<FileInfo> getAllFiles() {
        try {
            List<FileIndex> fileIndexes = fileIndexMapper.selectList(null);
            List<FileInfo> fileInfos = new ArrayList<>();
            
            for (FileIndex fileIndex : fileIndexes) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setId(fileIndex.getId());
                fileInfo.setFilePath(fileIndex.getFilePath());
                fileInfo.setFileName(fileIndex.getFileName());
                fileInfo.setFileSize(fileIndex.getFileSize());
                fileInfo.setFileType(fileIndex.getFileType());
                fileInfo.setLastModified(fileIndex.getLastModified());
                fileInfo.setIndexedTime(fileIndex.getIndexedTime());
                fileInfo.setFolderId(fileIndex.getFolderId());
                fileInfo.setContent(fileIndex.getContent());
                fileInfo.setSummary(fileIndex.getSummary());
                
                fileInfos.add(fileInfo);
            }
            
            return fileInfos;
            
        } catch (Exception e) {
            logger.error("获取所有文件列表失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 添加监控文件夹
     */
    public void addWatchFolder(String path, boolean recursive) {
        // 实现添加监控文件夹逻辑
        logger.info("添加监控文件夹: {}, 递归: {}", path, recursive);
        indexFolder(path, recursive);
    }
    
    /**
     * 获取监控文件夹列表
     */
    public List<Object> getWatchFolders() {
        // 返回监控文件夹列表
        return new ArrayList<>();
    }
    
    /**
     * 删除监控文件夹
     */
    public void removeWatchFolder(Long id) {
        logger.info("删除监控文件夹: {}", id);
    }
    
    /**
     * 重建索引
     */
    public void rebuildIndex() {
        logger.info("重建索引");
        // 实现重建索引逻辑
    }
    
    /**
     * 获取总文件数
     */
    public int getTotalFileCount() {
        try {
            return fileIndexMapper.selectCount(null).intValue();
        } catch (Exception e) {
            logger.error("获取总文件数失败", e);
            return 0;
        }
    }
    
    /**
     * 获取已索引文件数
     */
    public int getIndexedFileCount() {
        return getTotalFileCount();
    }
    
    /**
     * 清空所有索引
     */
    public void clearAllIndexes() {
        try {
            fileIndexMapper.delete(null);
            logger.info("清空所有索引");
        } catch (Exception e) {
            logger.error("清空所有索引失败", e);
        }
    }
    
    /**
     * 检查文件是否应该被索引
     */
    private boolean shouldIndex(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        
        // 跳过隐藏文件和系统文件
        if (fileName.startsWith(".") || fileName.startsWith("~")) {
            return false;
        }
        
        // 跳过临时文件
        if (fileName.endsWith(".tmp") || fileName.endsWith(".temp")) {
            return false;
        }
        
        // 跳过二进制文件（简单检查）
        String[] binaryExtensions = {".exe", ".dll", ".so", ".dylib", ".bin", ".jar", ".war", ".zip", ".tar", ".gz", ".rar", ".7z"};
        for (String ext : binaryExtensions) {
            if (fileName.endsWith(ext)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取文件类型
     */
    private String getFileType(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "unknown";
    }
}
