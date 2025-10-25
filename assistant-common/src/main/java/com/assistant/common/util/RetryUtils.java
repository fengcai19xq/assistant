package com.assistant.common.util;

import com.assistant.common.exception.RetryableException;
import com.assistant.common.exception.SystemException;
import com.assistant.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * 重试工具类
 * 
 * @author assistant
 * @since 1.0.0
 */
public class RetryUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(RetryUtils.class);
    
    /**
     * 默认重试次数
     */
    private static final int DEFAULT_MAX_RETRIES = 3;
    
    /**
     * 默认重试间隔（毫秒）
     */
    private static final long DEFAULT_RETRY_INTERVAL = 1000;
    
    /**
     * 执行重试操作
     */
    public static <T> T retry(Supplier<T> operation) {
        return retry(operation, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL);
    }
    
    /**
     * 执行重试操作
     */
    public static <T> T retry(Supplier<T> operation, int maxRetries) {
        return retry(operation, maxRetries, DEFAULT_RETRY_INTERVAL);
    }
    
    /**
     * 执行重试操作
     */
    public static <T> T retry(Supplier<T> operation, int maxRetries, long retryInterval) {
        int currentRetries = 0;
        Exception lastException = null;
        
        while (currentRetries <= maxRetries) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                currentRetries++;
                
                if (currentRetries > maxRetries) {
                    logger.error("重试 {} 次后仍然失败", maxRetries, e);
                    break;
                }
                
                logger.warn("操作失败，第 {} 次重试，错误: {}", currentRetries, e.getMessage());
                
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SystemException(ErrorCode.SYSTEM_ERROR, "重试被中断", ie);
                }
            }
        }
        
        throw new SystemException(ErrorCode.SYSTEM_ERROR, 
                String.format("重试 %d 次后仍然失败", maxRetries), lastException);
    }
    
    /**
     * 执行重试操作（带条件）
     */
    public static <T> T retry(Supplier<T> operation, RetryCondition condition) {
        return retry(operation, condition, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL);
    }
    
    /**
     * 执行重试操作（带条件）
     */
    public static <T> T retry(Supplier<T> operation, RetryCondition condition, 
                             int maxRetries, long retryInterval) {
        int currentRetries = 0;
        Exception lastException = null;
        
        while (currentRetries <= maxRetries) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                currentRetries++;
                
                // 检查是否应该重试
                if (!condition.shouldRetry(e, currentRetries, maxRetries)) {
                    logger.error("不满足重试条件，停止重试", e);
                    break;
                }
                
                if (currentRetries > maxRetries) {
                    logger.error("重试 {} 次后仍然失败", maxRetries, e);
                    break;
                }
                
                logger.warn("操作失败，第 {} 次重试，错误: {}", currentRetries, e.getMessage());
                
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SystemException(ErrorCode.SYSTEM_ERROR, "重试被中断", ie);
                }
            }
        }
        
        throw new SystemException(ErrorCode.SYSTEM_ERROR, 
                String.format("重试 %d 次后仍然失败", maxRetries), lastException);
    }
    
    /**
     * 执行重试操作（异步）
     */
    public static <T> java.util.concurrent.CompletableFuture<T> retryAsync(
            Supplier<T> operation, int maxRetries, long retryInterval) {
        
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> 
                retry(operation, maxRetries, retryInterval));
    }
    
    /**
     * 重试条件接口
     */
    @FunctionalInterface
    public interface RetryCondition {
        /**
         * 判断是否应该重试
         */
        boolean shouldRetry(Exception exception, int currentRetries, int maxRetries);
    }
    
    /**
     * 默认重试条件：所有异常都重试
     */
    public static final RetryCondition DEFAULT_CONDITION = (exception, currentRetries, maxRetries) -> true;
    
    /**
     * 只对特定异常重试
     */
    public static RetryCondition onlyFor(Class<? extends Exception> exceptionType) {
        return (exception, currentRetries, maxRetries) -> 
                exceptionType.isAssignableFrom(exception.getClass());
    }
    
    /**
     * 排除特定异常
     */
    public static RetryCondition exclude(Class<? extends Exception> exceptionType) {
        return (exception, currentRetries, maxRetries) -> 
                !exceptionType.isAssignableFrom(exception.getClass());
    }
    
    /**
     * 组合重试条件
     */
    public static RetryCondition and(RetryCondition condition1, RetryCondition condition2) {
        return (exception, currentRetries, maxRetries) -> 
                condition1.shouldRetry(exception, currentRetries, maxRetries) && 
                condition2.shouldRetry(exception, currentRetries, maxRetries);
    }
    
    /**
     * 组合重试条件
     */
    public static RetryCondition or(RetryCondition condition1, RetryCondition condition2) {
        return (exception, currentRetries, maxRetries) -> 
                condition1.shouldRetry(exception, currentRetries, maxRetries) || 
                condition2.shouldRetry(exception, currentRetries, maxRetries);
    }
}
