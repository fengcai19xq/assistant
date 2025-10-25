package com.assistant.web.controller;

import com.assistant.common.dto.BaseResponse;
import com.assistant.common.dto.SearchRequest;
import com.assistant.common.dto.SearchResult;
import com.assistant.core.service.OptimizedSearchService;
import com.assistant.core.service.IndexOptimizationService;
import com.assistant.core.service.FileIndexService;
import com.assistant.web.health.AssistantHealthIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 优化版助手控制器
 * 集成高性能搜索和索引优化功能
 */
@RestController
@RequestMapping("/api/v3")
public class OptimizedAssistantController {

    private static final Logger logger = LoggerFactory.getLogger(OptimizedAssistantController.class);

    @Autowired
    private OptimizedSearchService optimizedSearchService;

    @Autowired
    private IndexOptimizationService indexOptimizationService;

    @Autowired
    private FileIndexService fileIndexService;

    @Autowired
    private AssistantHealthIndicator healthIndicator;

    /**
     * 获取系统状态
     */
    @GetMapping("/status")
    public Mono<BaseResponse<Map<String, Object>>> getSystemStatus() {
        return Mono.fromCallable(() -> {
            Map<String, Object> status = new HashMap<>();
            status.put("service", "Optimized Assistant Service v3.0");
            status.put("indexedFiles", fileIndexService.getIndexedFileCount());
            status.put("totalFiles", fileIndexService.getTotalFileCount());
            status.put("health", healthIndicator.getHealthStatus());
            
            // 添加优化服务统计
            Map<String, Object> cacheStats = optimizedSearchService.getCacheStats();
            status.put("cacheStats", cacheStats);
            
            // 添加索引统计
            Map<String, Object> indexStats = indexOptimizationService.getIndexStats();
            status.put("indexStats", indexStats);
            
            return BaseResponse.success("系统状态正常", status);
        }).onErrorResume(e -> {
            logger.error("获取系统状态失败", e);
            return Mono.just(BaseResponse.error("获取系统状态失败: " + e.getMessage()));
        });
    }

    /**
     * 高性能搜索
     */
    @PostMapping("/search/optimized")
    public Mono<BaseResponse<List<SearchResult>>> optimizedSearch(@RequestBody SearchRequest request) {
        return Mono.fromCallable(() -> {
            logger.info("执行优化搜索: {}", request.getQuery());
            long startTime = System.currentTimeMillis();
            
            List<SearchResult> results = optimizedSearchService.searchFiles(request);
            
            long endTime = System.currentTimeMillis();
            logger.info("优化搜索完成，耗时: {} ms，返回 {} 个结果", 
                endTime - startTime, results.size());
            
            return BaseResponse.success("搜索完成", results);
        }).onErrorResume(e -> {
            logger.error("优化搜索失败", e);
            return Mono.just(BaseResponse.error("搜索失败: " + e.getMessage()));
        });
    }

    /**
     * 异步搜索
     */
    @PostMapping("/search/async")
    public Mono<BaseResponse<List<SearchResult>>> asyncSearch(@RequestBody SearchRequest request) {
        return Mono.fromFuture(() -> {
            logger.info("执行异步搜索: {}", request.getQuery());
            
            return optimizedSearchService.searchFilesAsync(request)
                .thenApply(results -> {
                    logger.info("异步搜索完成，返回 {} 个结果", results.size());
                    return BaseResponse.success("异步搜索完成", results);
                });
        }).onErrorResume(e -> {
            logger.error("异步搜索失败", e);
            return Mono.just(BaseResponse.error("异步搜索失败: " + e.getMessage()));
        });
    }

    /**
     * 流式搜索
     */
    @PostMapping("/search/stream")
    public Flux<SearchResult> streamSearch(@RequestBody SearchRequest request) {
        return Mono.fromCallable(() -> {
            logger.info("执行流式搜索: {}", request.getQuery());
            return optimizedSearchService.searchFiles(request);
        }).flatMapMany(Flux::fromIterable)
        .doOnNext(result -> logger.debug("流式搜索结果: {}", result.getFileName()))
        .doOnComplete(() -> logger.info("流式搜索完成"));
    }

    /**
     * 获取索引统计信息
     */
    @GetMapping("/index/stats")
    public Mono<BaseResponse<Map<String, Object>>> getIndexStats() {
        return Mono.fromCallable(() -> {
            Map<String, Object> stats = indexOptimizationService.getIndexStats();
            Map<String, Object> performanceMetrics = indexOptimizationService.getPerformanceMetrics();
            
            Map<String, Object> result = new HashMap<>();
            result.put("indexStats", stats);
            result.put("performanceMetrics", performanceMetrics);
            
            return BaseResponse.success("索引统计信息", result);
        }).onErrorResume(e -> {
            logger.error("获取索引统计信息失败", e);
            return Mono.just(BaseResponse.error("获取索引统计信息失败: " + e.getMessage()));
        });
    }

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/cache/stats")
    public Mono<BaseResponse<Map<String, Object>>> getCacheStats() {
        return Mono.fromCallable(() -> {
            Map<String, Object> cacheStats = optimizedSearchService.getCacheStats();
            return BaseResponse.success("缓存统计信息", cacheStats);
        }).onErrorResume(e -> {
            logger.error("获取缓存统计信息失败", e);
            return Mono.just(BaseResponse.error("获取缓存统计信息失败: " + e.getMessage()));
        });
    }

    /**
     * 清理缓存
     */
    @PostMapping("/cache/clear")
    public Mono<BaseResponse<String>> clearCache() {
        return Mono.fromRunnable(() -> {
            optimizedSearchService.clearExpiredCache();
            logger.info("缓存清理完成");
        }).then(Mono.just(BaseResponse.success("缓存清理完成"))).onErrorResume(e -> {
            logger.error("清理缓存失败", e);
            return Mono.just(BaseResponse.error("清理缓存失败: " + e.getMessage()));
        });
    }

    /**
     * 触发索引优化
     */
    @PostMapping("/index/optimize")
    public Mono<BaseResponse<String>> optimizeIndex() {
        return Mono.fromRunnable(() -> {
            indexOptimizationService.triggerIndexOptimization();
            logger.info("索引优化已触发");
        }).then(Mono.just(BaseResponse.success("索引优化已触发"))).onErrorResume(e -> {
            logger.error("触发索引优化失败", e);
            return Mono.just(BaseResponse.error("触发索引优化失败: " + e.getMessage()));
        });
    }

    /**
     * 重建索引
     */
    @PostMapping("/index/rebuild")
    public Mono<BaseResponse<String>> rebuildIndex() {
        return Mono.fromFuture(() -> {
            logger.info("开始重建索引");
            return indexOptimizationService.rebuildIndexAsync();
        }).then(Mono.just(BaseResponse.success("索引重建已启动"))).onErrorResume(e -> {
            logger.error("重建索引失败", e);
            return Mono.just(BaseResponse.error("重建索引失败: " + e.getMessage()));
        });
    }

    /**
     * 增量更新索引
     */
    @PostMapping("/index/update")
    public Mono<BaseResponse<String>> updateIndex() {
        return Mono.fromFuture(() -> {
            logger.info("开始增量更新索引");
            return indexOptimizationService.updateIndexIncremental();
        }).then(Mono.just(BaseResponse.success("索引增量更新已启动"))).onErrorResume(e -> {
            logger.error("增量更新索引失败", e);
            return Mono.just(BaseResponse.error("增量更新索引失败: " + e.getMessage()));
        });
    }

    /**
     * 获取性能指标
     */
    @GetMapping("/performance/metrics")
    public Mono<BaseResponse<Map<String, Object>>> getPerformanceMetrics() {
        return Mono.fromCallable(() -> {
            Map<String, Object> metrics = indexOptimizationService.getPerformanceMetrics();
            Map<String, Object> cacheStats = optimizedSearchService.getCacheStats();
            
            Map<String, Object> result = new HashMap<>();
            result.put("performanceMetrics", metrics);
            result.put("cacheStats", cacheStats);
            
            return BaseResponse.success("性能指标", result);
        }).onErrorResume(e -> {
            logger.error("获取性能指标失败", e);
            return Mono.just(BaseResponse.error("获取性能指标失败: " + e.getMessage()));
        });
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> healthCheck() {
        return Mono.fromCallable(() -> {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", System.currentTimeMillis());
            health.put("service", "Optimized Assistant Service v3.0");
            
            // 添加详细健康信息
            Map<String, Object> detailedHealth = healthIndicator.getHealthStatus();
            health.put("details", detailedHealth);
            
            return ResponseEntity.ok(health);
        }).onErrorResume(e -> {
            logger.error("健康检查失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "DOWN");
            error.put("error", e.getMessage());
            return Mono.just(ResponseEntity.status(500).body(error));
        });
    }
}
