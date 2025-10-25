package com.assistant.web.exception;

import com.assistant.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;

/**
 * 异常指标收集器
 * 
 * @author assistant
 * @since 1.0.0
 */
@Component
public class ExceptionMetrics {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionMetrics.class);
    
    /**
     * 异常统计
     */
    private final Map<String, AtomicLong> exceptionCounts = new ConcurrentHashMap<>();
    
    /**
     * 错误码统计
     */
    private final Map<String, AtomicLong> errorCodeCounts = new ConcurrentHashMap<>();
    
    /**
     * 最后异常时间
     */
    private final Map<String, LocalDateTime> lastExceptionTimes = new ConcurrentHashMap<>();
    
    /**
     * 记录异常
     */
    public void recordException(String exceptionType, String errorCode) {
        // 记录异常类型统计
        exceptionCounts.computeIfAbsent(exceptionType, k -> new AtomicLong(0)).incrementAndGet();
        
        // 记录错误码统计
        errorCodeCounts.computeIfAbsent(errorCode, k -> new AtomicLong(0)).incrementAndGet();
        
        // 记录最后异常时间
        lastExceptionTimes.put(exceptionType, LocalDateTime.now());
        
        logger.debug("记录异常: type={}, errorCode={}", exceptionType, errorCode);
    }
    
    /**
     * 获取异常统计
     */
    public Map<String, Long> getExceptionCounts() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        exceptionCounts.forEach((key, value) -> result.put(key, value.get()));
        return result;
    }
    
    /**
     * 获取错误码统计
     */
    public Map<String, Long> getErrorCodeCounts() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        errorCodeCounts.forEach((key, value) -> result.put(key, value.get()));
        return result;
    }
    
    /**
     * 获取最后异常时间
     */
    public Map<String, LocalDateTime> getLastExceptionTimes() {
        return new ConcurrentHashMap<>(lastExceptionTimes);
    }
    
    /**
     * 获取异常统计摘要
     */
    public ExceptionSummary getExceptionSummary() {
        ExceptionSummary summary = new ExceptionSummary();
        summary.setTotalExceptions(getTotalExceptionCount());
        summary.setExceptionCounts(getExceptionCounts());
        summary.setErrorCodeCounts(getErrorCodeCounts());
        summary.setLastExceptionTimes(getLastExceptionTimes());
        summary.setTimestamp(LocalDateTime.now());
        return summary;
    }
    
    /**
     * 获取总异常数
     */
    public long getTotalExceptionCount() {
        return exceptionCounts.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
    }
    
    /**
     * 获取指定异常类型的计数
     */
    public long getExceptionCount(String exceptionType) {
        return exceptionCounts.getOrDefault(exceptionType, new AtomicLong(0)).get();
    }
    
    /**
     * 获取指定错误码的计数
     */
    public long getErrorCodeCount(String errorCode) {
        return errorCodeCounts.getOrDefault(errorCode, new AtomicLong(0)).get();
    }
    
    /**
     * 重置统计
     */
    public void reset() {
        exceptionCounts.clear();
        errorCodeCounts.clear();
        lastExceptionTimes.clear();
        logger.info("异常统计已重置");
    }
    
    /**
     * 异常统计摘要
     */
    public static class ExceptionSummary {
        private long totalExceptions;
        private Map<String, Long> exceptionCounts;
        private Map<String, Long> errorCodeCounts;
        private Map<String, LocalDateTime> lastExceptionTimes;
        private LocalDateTime timestamp;
        
        // Getters and Setters
        public long getTotalExceptions() {
            return totalExceptions;
        }
        
        public void setTotalExceptions(long totalExceptions) {
            this.totalExceptions = totalExceptions;
        }
        
        public Map<String, Long> getExceptionCounts() {
            return exceptionCounts;
        }
        
        public void setExceptionCounts(Map<String, Long> exceptionCounts) {
            this.exceptionCounts = exceptionCounts;
        }
        
        public Map<String, Long> getErrorCodeCounts() {
            return errorCodeCounts;
        }
        
        public void setErrorCodeCounts(Map<String, Long> errorCodeCounts) {
            this.errorCodeCounts = errorCodeCounts;
        }
        
        public Map<String, LocalDateTime> getLastExceptionTimes() {
            return lastExceptionTimes;
        }
        
        public void setLastExceptionTimes(Map<String, LocalDateTime> lastExceptionTimes) {
            this.lastExceptionTimes = lastExceptionTimes;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
        
        @Override
        public String toString() {
            return String.format("ExceptionSummary{totalExceptions=%d, exceptionCounts=%s, " +
                            "errorCodeCounts=%s, lastExceptionTimes=%s, timestamp=%s}",
                    totalExceptions, exceptionCounts, errorCodeCounts, lastExceptionTimes, timestamp);
        }
    }
}
