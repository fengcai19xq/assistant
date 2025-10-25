package com.assistant.common.util;

import com.assistant.common.exception.SystemException;
import com.assistant.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 熔断器
 * 
 * @author assistant
 * @since 1.0.0
 */
public class CircuitBreaker {
    
    private static final Logger logger = LoggerFactory.getLogger(CircuitBreaker.class);
    
    /**
     * 熔断器状态
     */
    public enum State {
        CLOSED,    // 关闭状态，正常执行
        OPEN,      // 开启状态，直接拒绝
        HALF_OPEN  // 半开状态，尝试执行
    }
    
    /**
     * 熔断器名称
     */
    private final String name;
    
    /**
     * 失败阈值
     */
    private final int failureThreshold;
    
    /**
     * 成功阈值
     */
    private final int successThreshold;
    
    /**
     * 熔断时间（毫秒）
     */
    private final long timeout;
    
    /**
     * 当前状态
     */
    private volatile State state = State.CLOSED;
    
    /**
     * 失败次数
     */
    private final AtomicInteger failureCount = new AtomicInteger(0);
    
    /**
     * 成功次数
     */
    private final AtomicInteger successCount = new AtomicInteger(0);
    
    /**
     * 最后失败时间
     */
    private volatile long lastFailureTime = 0;
    
    /**
     * 最后状态变更时间
     */
    private volatile long lastStateChangeTime = System.currentTimeMillis();
    
    public CircuitBreaker(String name, int failureThreshold, int successThreshold, long timeout) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.timeout = timeout;
    }
    
    /**
     * 执行操作
     */
    public <T> T execute(Supplier<T> operation) throws Exception {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime >= timeout) {
                // 超时后转为半开状态
                transitionToHalfOpen();
            } else {
                throw new SystemException(ErrorCode.SYSTEM_BUSY, 
                        String.format("熔断器 %s 处于开启状态，请求被拒绝", name));
            }
        }
        
        try {
            T result = operation.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }
    
    /**
     * 成功回调
     */
    private void onSuccess() {
        if (state == State.HALF_OPEN) {
            int success = successCount.incrementAndGet();
            if (success >= successThreshold) {
                transitionToClosed();
            }
        } else if (state == State.CLOSED) {
            // 重置失败计数
            failureCount.set(0);
        }
    }
    
    /**
     * 失败回调
     */
    private void onFailure() {
        lastFailureTime = System.currentTimeMillis();
        
        if (state == State.HALF_OPEN) {
            // 半开状态下失败，直接转为开启状态
            transitionToOpen();
        } else if (state == State.CLOSED) {
            int failures = failureCount.incrementAndGet();
            if (failures >= failureThreshold) {
                transitionToOpen();
            }
        }
    }
    
    /**
     * 转为关闭状态
     */
    private void transitionToClosed() {
        state = State.CLOSED;
        failureCount.set(0);
        successCount.set(0);
        lastStateChangeTime = System.currentTimeMillis();
        logger.info("熔断器 {} 转为关闭状态", name);
    }
    
    /**
     * 转为开启状态
     */
    private void transitionToOpen() {
        state = State.OPEN;
        lastStateChangeTime = System.currentTimeMillis();
        logger.warn("熔断器 {} 转为开启状态", name);
    }
    
    /**
     * 转为半开状态
     */
    private void transitionToHalfOpen() {
        state = State.HALF_OPEN;
        successCount.set(0);
        lastStateChangeTime = System.currentTimeMillis();
        logger.info("熔断器 {} 转为半开状态", name);
    }
    
    /**
     * 获取当前状态
     */
    public State getState() {
        return state;
    }
    
    /**
     * 获取失败次数
     */
    public int getFailureCount() {
        return failureCount.get();
    }
    
    /**
     * 获取成功次数
     */
    public int getSuccessCount() {
        return successCount.get();
    }
    
    /**
     * 获取熔断器名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 重置熔断器
     */
    public void reset() {
        state = State.CLOSED;
        failureCount.set(0);
        successCount.set(0);
        lastFailureTime = 0;
        lastStateChangeTime = System.currentTimeMillis();
        logger.info("熔断器 {} 已重置", name);
    }
    
    /**
     * 获取熔断器状态信息
     */
    public String getStatus() {
        return String.format("CircuitBreaker{name='%s', state=%s, failures=%d, successes=%d, " +
                        "lastFailureTime=%d, lastStateChangeTime=%d}",
                name, state, failureCount.get(), successCount.get(), 
                lastFailureTime, lastStateChangeTime);
    }
    
    /**
     * 创建熔断器
     */
    public static CircuitBreaker create(String name, int failureThreshold, 
                                       int successThreshold, long timeout) {
        return new CircuitBreaker(name, failureThreshold, successThreshold, timeout);
    }
    
    /**
     * 创建默认熔断器
     */
    public static CircuitBreaker createDefault(String name) {
        return new CircuitBreaker(name, 5, 3, 60000); // 5次失败，3次成功，60秒超时
    }
}
