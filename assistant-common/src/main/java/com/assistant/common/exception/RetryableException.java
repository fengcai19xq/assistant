package com.assistant.common.exception;

/**
 * 可重试异常类
 * 
 * @author assistant
 * @since 1.0.0
 */
public class RetryableException extends BaseException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 最大重试次数
     */
    private final int maxRetries;
    
    /**
     * 当前重试次数
     */
    private final int currentRetries;
    
    /**
     * 重试间隔时间（毫秒）
     */
    private final long retryInterval;
    
    public RetryableException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
        this.maxRetries = 3;
        this.currentRetries = 0;
        this.retryInterval = 1000;
    }
    
    public RetryableException(String errorCode, String errorMessage, int maxRetries) {
        super(errorCode, errorMessage);
        this.maxRetries = maxRetries;
        this.currentRetries = 0;
        this.retryInterval = 1000;
    }
    
    public RetryableException(String errorCode, String errorMessage, int maxRetries, int currentRetries) {
        super(errorCode, errorMessage);
        this.maxRetries = maxRetries;
        this.currentRetries = currentRetries;
        this.retryInterval = 1000;
    }
    
    public RetryableException(String errorCode, String errorMessage, int maxRetries, 
                             int currentRetries, long retryInterval) {
        super(errorCode, errorMessage);
        this.maxRetries = maxRetries;
        this.currentRetries = currentRetries;
        this.retryInterval = retryInterval;
    }
    
    public RetryableException(String errorCode, String errorMessage, String errorDetail, 
                             int maxRetries, int currentRetries, long retryInterval) {
        super(errorCode, errorMessage, errorDetail);
        this.maxRetries = maxRetries;
        this.currentRetries = currentRetries;
        this.retryInterval = retryInterval;
    }
    
    public RetryableException(String errorCode, String errorMessage, Throwable cause, 
                             int maxRetries, int currentRetries, long retryInterval) {
        super(errorCode, errorMessage, cause);
        this.maxRetries = maxRetries;
        this.currentRetries = currentRetries;
        this.retryInterval = retryInterval;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public int getCurrentRetries() {
        return currentRetries;
    }
    
    public long getRetryInterval() {
        return retryInterval;
    }
    
    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return currentRetries < maxRetries;
    }
    
    /**
     * 获取下次重试时间
     */
    public long getNextRetryTime() {
        return System.currentTimeMillis() + retryInterval;
    }
    
    /**
     * 创建重试异常
     */
    public static RetryableException create(String errorCode, String errorMessage, 
                                           int maxRetries, int currentRetries) {
        return new RetryableException(errorCode, errorMessage, maxRetries, currentRetries);
    }
    
    /**
     * 创建重试异常
     */
    public static RetryableException create(String errorCode, String errorMessage, 
                                           int maxRetries, int currentRetries, long retryInterval) {
        return new RetryableException(errorCode, errorMessage, maxRetries, currentRetries, retryInterval);
    }
}
