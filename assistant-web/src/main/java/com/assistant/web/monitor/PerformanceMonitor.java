package com.assistant.web.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控器
 * 监控系统性能指标
 */
@Component
public class PerformanceMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    
    private final MeterRegistry meterRegistry;
    
    // 计数器
    private final Counter searchCounter;
    private final Counter indexCounter;
    private final Counter errorCounter;
    
    // 计时器
    private final Timer searchTimer;
    private final Timer indexTimer;
    private final Timer embeddingTimer;
    
    // 原子计数器
    private final AtomicLong totalSearches = new AtomicLong(0);
    private final AtomicLong totalIndexedFiles = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    
    @Autowired
    public PerformanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // 初始化计数器
        this.searchCounter = Counter.builder("assistant.searches.total")
            .description("Total number of searches")
            .register(meterRegistry);
            
        this.indexCounter = Counter.builder("assistant.indexes.total")
            .description("Total number of indexed files")
            .register(meterRegistry);
            
        this.errorCounter = Counter.builder("assistant.errors.total")
            .description("Total number of errors")
            .register(meterRegistry);
        
        // 初始化计时器
        this.searchTimer = Timer.builder("assistant.search.duration")
            .description("Search operation duration")
            .register(meterRegistry);
            
        this.indexTimer = Timer.builder("assistant.index.duration")
            .description("Index operation duration")
            .register(meterRegistry);
            
        this.embeddingTimer = Timer.builder("assistant.embedding.duration")
            .description("Embedding generation duration")
            .register(meterRegistry);
    }
    
    /**
     * 记录搜索操作
     */
    public void recordSearch(String query, long durationMs) {
        searchCounter.increment();
        totalSearches.incrementAndGet();
        searchTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        logger.debug("搜索记录: query={}, duration={}ms", query, durationMs);
    }
    
    /**
     * 记录索引操作
     */
    public void recordIndex(String filePath, long durationMs) {
        indexCounter.increment();
        totalIndexedFiles.incrementAndGet();
        indexTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        logger.debug("索引记录: file={}, duration={}ms", filePath, durationMs);
    }
    
    /**
     * 记录嵌入生成
     */
    public void recordEmbedding(String text, long durationMs) {
        embeddingTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        logger.debug("嵌入记录: text={}, duration={}ms", text.substring(0, Math.min(50, text.length())), durationMs);
    }
    
    /**
     * 记录错误
     */
    public void recordError(String operation, String error) {
        errorCounter.increment();
        totalErrors.incrementAndGet();
        
        logger.error("错误记录: operation={}, error={}", operation, error);
    }
    
    /**
     * 获取性能统计
     */
    public java.util.Map<String, Object> getPerformanceStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        stats.put("totalSearches", totalSearches.get());
        stats.put("totalIndexedFiles", totalIndexedFiles.get());
        stats.put("totalErrors", totalErrors.get());
        
        // 平均搜索时间
        double avgSearchTime = searchTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
        stats.put("avgSearchTimeMs", String.format("%.2f", avgSearchTime));
        
        // 平均索引时间
        double avgIndexTime = indexTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
        stats.put("avgIndexTimeMs", String.format("%.2f", avgIndexTime));
        
        // 平均嵌入时间
        double avgEmbeddingTime = embeddingTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
        stats.put("avgEmbeddingTimeMs", String.format("%.2f", avgEmbeddingTime));
        
        return stats;
    }
    
    /**
     * 重置统计
     */
    public void resetStats() {
        totalSearches.set(0);
        totalIndexedFiles.set(0);
        totalErrors.set(0);
        
        logger.info("性能统计已重置");
    }
}
