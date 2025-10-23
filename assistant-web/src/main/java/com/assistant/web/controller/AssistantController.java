package com.assistant.web.controller;

import com.assistant.common.dto.BaseResponse;
import com.assistant.common.dto.SearchRequest;
import com.assistant.common.dto.SearchResult;
import com.assistant.core.entity.WatchFolder;
import com.assistant.core.service.FileIndexService;
import com.assistant.core.service.SearchService;
import com.assistant.core.service.WatchFolderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 助手API控制器
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class AssistantController {
    
    private static final Logger logger = LoggerFactory.getLogger(AssistantController.class);
    
    @Autowired
    private WatchFolderService watchFolderService;
    
    @Autowired
    private FileIndexService fileIndexService;
    
    @Autowired
    private SearchService searchService;
    
    /**
     * 添加监控文件夹
     */
    @PostMapping("/folders")
    public BaseResponse<Void> addWatchFolder(@RequestParam String path, 
                                           @RequestParam(defaultValue = "true") boolean recursive) {
        try {
            boolean success = watchFolderService.addWatchFolder(path, recursive);
            if (success) {
                return BaseResponse.<Void>success();
            } else {
                return BaseResponse.error("文件夹添加失败");
            }
        } catch (Exception e) {
            logger.error("添加监控文件夹失败", e);
            return BaseResponse.error("添加文件夹失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有监控文件夹
     */
    @GetMapping("/folders")
    public BaseResponse<List<WatchFolder>> getWatchFolders() {
        try {
            List<WatchFolder> folders = watchFolderService.getAllWatchFolders();
            return BaseResponse.success(folders);
        } catch (Exception e) {
            logger.error("获取监控文件夹失败", e);
            return BaseResponse.error("获取文件夹列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除监控文件夹
     */
    @DeleteMapping("/folders/{id}")
    public BaseResponse<Void> removeWatchFolder(@PathVariable Long id) {
        try {
            boolean success = watchFolderService.removeWatchFolder(id);
            if (success) {
                return BaseResponse.<Void>success();
            } else {
                return BaseResponse.error("文件夹删除失败");
            }
        } catch (Exception e) {
            logger.error("删除监控文件夹失败", e);
            return BaseResponse.error("删除文件夹失败: " + e.getMessage());
        }
    }
    
    /**
     * 重新索引所有文件夹
     */
    @PostMapping("/folders/reindex")
    public BaseResponse<Integer> reindexAllFolders() {
        try {
            int count = watchFolderService.reindexAllFolders();
            return BaseResponse.success("重新索引完成，共索引 " + count + " 个文件", count);
        } catch (Exception e) {
            logger.error("重新索引失败", e);
            return BaseResponse.error("重新索引失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索文件
     */
    @PostMapping("/search")
    public BaseResponse<List<SearchResult>> searchFiles(@Valid @RequestBody SearchRequest request) {
        try {
            List<SearchResult> results = searchService.searchFiles(request);
            return BaseResponse.success("搜索完成", results);
        } catch (Exception e) {
            logger.error("搜索失败", e);
            return BaseResponse.error("搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取搜索历史
     */
    @GetMapping("/search/history")
    public BaseResponse<List<com.assistant.core.entity.SearchHistory>> getSearchHistory(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<com.assistant.core.entity.SearchHistory> history = searchService.getSearchHistory(limit);
            return BaseResponse.success(history);
        } catch (Exception e) {
            logger.error("获取搜索历史失败", e);
            return BaseResponse.error("获取搜索历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 清空搜索历史
     */
    @DeleteMapping("/search/history")
    public BaseResponse<Void> clearSearchHistory() {
        try {
            boolean success = searchService.clearSearchHistory();
            if (success) {
                return BaseResponse.<Void>success();
            } else {
                return BaseResponse.error("清空搜索历史失败");
            }
        } catch (Exception e) {
            logger.error("清空搜索历史失败", e);
            return BaseResponse.error("清空搜索历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有文件列表
     */
    @GetMapping("/files")
    public BaseResponse<List<com.assistant.common.dto.FileInfo>> getAllFiles() {
        try {
            List<com.assistant.common.dto.FileInfo> files = fileIndexService.getAllFiles();
            return BaseResponse.success(files);
        } catch (Exception e) {
            logger.error("获取文件列表失败", e);
            return BaseResponse.error("获取文件列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取系统状态
     */
    @GetMapping("/status")
    public BaseResponse<Object> getSystemStatus() {
        try {
            // 获取基本统计信息
            List<WatchFolder> folders = watchFolderService.getAllWatchFolders();
            List<com.assistant.common.dto.FileInfo> files = fileIndexService.getAllFiles();
            
            java.util.Map<String, Object> status = new java.util.HashMap<>();
            status.put("folders", folders.size());
            status.put("files", files.size());
            status.put("timestamp", java.time.LocalDateTime.now());
            
            return BaseResponse.success("系统状态正常", status);
        } catch (Exception e) {
            logger.error("获取系统状态失败", e);
            return BaseResponse.error("获取系统状态失败: " + e.getMessage());
        }
    }
}
