package com.assistant.core.service;

import com.assistant.core.entity.WatchFolder;
import com.assistant.core.mapper.WatchFolderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 监控文件夹服务
 */
@Service
public class WatchFolderService {
    
    private static final Logger logger = LoggerFactory.getLogger(WatchFolderService.class);
    
    @Autowired
    private WatchFolderMapper watchFolderMapper;
    
    @Autowired
    private FileIndexService fileIndexService;
    
    /**
     * 添加监控文件夹
     */
    public boolean addWatchFolder(String folderPath, boolean recursive) {
        try {
            // 验证文件夹是否存在
            Path path = Paths.get(folderPath);
            if (!Files.exists(path) || !Files.isDirectory(path)) {
                logger.warn("文件夹不存在或不是目录: {}", folderPath);
                return false;
            }
            
            // 检查是否已存在
            WatchFolder existing = watchFolderMapper.selectList(null)
                .stream()
                .filter(f -> f.getPath().equals(folderPath))
                .findFirst()
                .orElse(null);
            
            if (existing != null) {
                logger.info("文件夹已在监控列表中: {}", folderPath);
                return true;
            }
            
            // 创建新的监控文件夹记录
            WatchFolder watchFolder = new WatchFolder();
            watchFolder.setPath(folderPath);
            watchFolder.setRecursive(recursive);
            watchFolder.setEnabled(true);
            watchFolder.setCreatedTime(java.time.LocalDateTime.now().toString());
            watchFolder.setUpdatedTime(java.time.LocalDateTime.now().toString());
            
            watchFolderMapper.insert(watchFolder);
            
            // 立即开始索引
            int indexedCount = fileIndexService.indexFolder(folderPath, recursive);
            logger.info("添加监控文件夹成功: {}, 已索引 {} 个文件", folderPath, indexedCount);
            
            return true;
            
        } catch (Exception e) {
            logger.error("添加监控文件夹失败: {}", folderPath, e);
            return false;
        }
    }
    
    /**
     * 获取所有监控文件夹
     */
    public List<WatchFolder> getAllWatchFolders() {
        try {
            return watchFolderMapper.selectList(null);
        } catch (Exception e) {
            logger.error("获取监控文件夹列表失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 删除监控文件夹
     */
    public boolean removeWatchFolder(Long folderId) {
        try {
            WatchFolder watchFolder = watchFolderMapper.selectById(folderId);
            if (watchFolder == null) {
                logger.warn("监控文件夹不存在: {}", folderId);
                return false;
            }
            
            // 删除文件夹记录
            watchFolderMapper.deleteById(folderId);
            
            logger.info("删除监控文件夹成功: {}", watchFolder.getPath());
            return true;
            
        } catch (Exception e) {
            logger.error("删除监控文件夹失败: {}", folderId, e);
            return false;
        }
    }
    
    /**
     * 启用/禁用监控文件夹
     */
    public boolean toggleWatchFolder(Long folderId, boolean enabled) {
        try {
            WatchFolder watchFolder = watchFolderMapper.selectById(folderId);
            if (watchFolder == null) {
                logger.warn("监控文件夹不存在: {}", folderId);
                return false;
            }
            
            watchFolder.setEnabled(enabled);
            watchFolder.setUpdatedTime(java.time.LocalDateTime.now().toString());
            watchFolderMapper.updateById(watchFolder);
            
            logger.info("{}监控文件夹: {}", enabled ? "启用" : "禁用", watchFolder.getPath());
            return true;
            
        } catch (Exception e) {
            logger.error("切换监控文件夹状态失败: {}", folderId, e);
            return false;
        }
    }
    
    /**
     * 重新索引所有监控文件夹
     */
    public int reindexAllFolders() {
        logger.info("开始重新索引所有监控文件夹");
        
        int totalIndexed = 0;
        List<WatchFolder> watchFolders = getAllWatchFolders();
        
        for (WatchFolder watchFolder : watchFolders) {
            if (watchFolder.getEnabled()) {
                try {
                    int count = fileIndexService.indexFolder(watchFolder.getPath(), watchFolder.getRecursive());
                    totalIndexed += count;
                    logger.info("重新索引文件夹完成: {}, 索引了 {} 个文件", watchFolder.getPath(), count);
                } catch (Exception e) {
                    logger.error("重新索引文件夹失败: {}", watchFolder.getPath(), e);
                }
            }
        }
        
        logger.info("重新索引完成，总共索引了 {} 个文件", totalIndexed);
        return totalIndexed;
    }
}
