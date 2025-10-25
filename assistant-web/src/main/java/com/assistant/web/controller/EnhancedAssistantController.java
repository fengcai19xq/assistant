package com.assistant.web.controller;

import com.assistant.common.dto.BaseResponse;
import com.assistant.common.dto.SearchRequest;
import com.assistant.common.dto.SearchResult;
import com.assistant.core.service.FileIndexService;
import com.assistant.core.service.SearchService;
import com.assistant.ai.service.VectorSearchService;
import com.assistant.storage.service.VectorStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 增强版助手控制器
 * 基于WebFlux响应式架构
 */
@RestController
@RequestMapping("/assistant/api/v2")
@CrossOrigin(origins = "*")
public class EnhancedAssistantController {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedAssistantController.class);
    
    @Autowired
    private FileIndexService fileIndexService;
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private VectorSearchService vectorSearchService;
    
    @Autowired
    private VectorStorageService vectorStorageService;
    
    /**
     * 系统状态
     */
    @GetMapping("/status")
    public Mono<BaseResponse<Map<String, Object>>> getSystemStatus() {
        return Mono.fromCallable(() -> {
            Map<String, Object> status = new HashMap<>();
            status.put("service", "File AI Assistant v2.0");
            status.put("status", "running");
            status.put("timestamp", System.currentTimeMillis());
            status.put("vectorIndexSize", vectorStorageService.getIndexSize());
            status.put("storageStats", vectorStorageService.getStorageStats());
            
            return BaseResponse.success(status);
        });
    }
    
    /**
     * 语义搜索
     */
    @PostMapping("/search/semantic")
    public Mono<BaseResponse<List<SearchResult>>> semanticSearch(@RequestBody SearchRequest request) {
        return Mono.fromCallable(() -> {
            logger.info("语义搜索请求: {}", request.getQuery());
            
            try {
                List<SearchResult> results = vectorSearchService.semanticSearch(
                    request.getQuery(), 
                    request.getPageSize() != null ? request.getPageSize() : 10
                ).get();
                
                return BaseResponse.success(results);
            } catch (Exception e) {
                logger.error("语义搜索失败", e);
                return BaseResponse.error("语义搜索失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 混合搜索 (关键词 + 语义)
     */
    @PostMapping("/search/hybrid")
    public Mono<BaseResponse<List<SearchResult>>> hybridSearch(@RequestBody SearchRequest request) {
        return Mono.fromCallable(() -> {
            logger.info("混合搜索请求: {}", request.getQuery());
            
            try {
                List<SearchResult> results = vectorSearchService.hybridSearch(
                    request.getQuery(), 
                    request.getPageSize() != null ? request.getPageSize() : 10
                ).get();
                
                return BaseResponse.success(results);
            } catch (Exception e) {
                logger.error("混合搜索失败", e);
                return BaseResponse.error("混合搜索失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 查找相似文档
     */
    @GetMapping("/search/similar/{documentId}")
    public Mono<BaseResponse<List<SearchResult>>> findSimilarDocuments(
            @PathVariable String documentId,
            @RequestParam(defaultValue = "5") int topK) {
        return Mono.fromCallable(() -> {
            logger.info("查找相似文档: {}", documentId);
            
            try {
                List<SearchResult> results = vectorSearchService.findSimilarDocuments(
                    documentId, topK
                ).get();
                
                return BaseResponse.success(results);
            } catch (Exception e) {
                logger.error("查找相似文档失败", e);
                return BaseResponse.error("查找相似文档失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 添加文件夹 (响应式)
     */
    @PostMapping("/folders")
    public Mono<BaseResponse<String>> addWatchFolder(
            @RequestParam String path,
            @RequestParam(defaultValue = "true") boolean recursive) {
        return Mono.fromCallable(() -> {
            logger.info("添加监控文件夹: {}", path);
            
            try {
                fileIndexService.addWatchFolder(path, recursive);
                return BaseResponse.success("文件夹添加成功");
            } catch (Exception e) {
                logger.error("添加文件夹失败", e);
                return BaseResponse.error("添加文件夹失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 获取监控文件夹列表
     */
    @GetMapping("/folders")
    public Flux<Map<String, Object>> getWatchFolders() {
        return Flux.fromIterable(fileIndexService.getWatchFolders())
            .map(folder -> {
                Map<String, Object> folderInfo = new HashMap<>();
                folderInfo.put("id", "unknown");
                folderInfo.put("path", "unknown");
                folderInfo.put("recursive", true);
                folderInfo.put("enabled", true);
                folderInfo.put("createdTime", System.currentTimeMillis());
                return folderInfo;
            });
    }
    
    /**
     * 删除监控文件夹
     */
    @DeleteMapping("/folders/{id}")
    public Mono<BaseResponse<String>> removeWatchFolder(@PathVariable Long id) {
        return Mono.fromCallable(() -> {
            logger.info("删除监控文件夹: {}", id);
            
            try {
                fileIndexService.removeWatchFolder(id);
                return BaseResponse.success("文件夹删除成功");
            } catch (Exception e) {
                logger.error("删除文件夹失败", e);
                return BaseResponse.error("删除文件夹失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 重建索引
     */
    @PostMapping("/index/rebuild")
    public Mono<BaseResponse<String>> rebuildIndex() {
        return Mono.fromCallable(() -> {
            logger.info("重建索引");
            
            try {
                fileIndexService.rebuildIndex();
                return BaseResponse.success("索引重建成功");
            } catch (Exception e) {
                logger.error("重建索引失败", e);
                return BaseResponse.error("重建索引失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 获取索引统计
     */
    @GetMapping("/index/stats")
    public Mono<BaseResponse<Map<String, Object>>> getIndexStats() {
        return Mono.fromCallable(() -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalFiles", fileIndexService.getTotalFileCount());
            stats.put("indexedFiles", fileIndexService.getIndexedFileCount());
            stats.put("vectorIndexSize", vectorStorageService.getIndexSize());
            stats.put("storageStats", vectorStorageService.getStorageStats());
            
            return BaseResponse.success(stats);
        });
    }
    
    /**
     * 清空所有数据
     */
    @DeleteMapping("/data/clear")
    public Mono<BaseResponse<String>> clearAllData() {
        return Mono.fromCallable(() -> {
            logger.info("清空所有数据");
            
            try {
                vectorStorageService.clearAllVectors();
                fileIndexService.clearAllIndexes();
                return BaseResponse.success("所有数据清空成功");
            } catch (Exception e) {
                logger.error("清空数据失败", e);
                return BaseResponse.error("清空数据失败: " + e.getMessage());
            }
        });
    }
}
