package com.assistant.web.controller;

import com.assistant.common.dto.BaseResponse;
import com.assistant.web.exception.ExceptionMetrics;
import com.assistant.web.exception.ExceptionMetrics.ExceptionSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 异常管理控制器
 * 
 * @author assistant
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/exception")
public class ExceptionController {
    
    @Autowired
    private ExceptionMetrics exceptionMetrics;
    
    /**
     * 获取异常统计摘要
     */
    @GetMapping("/summary")
    public BaseResponse<ExceptionSummary> getExceptionSummary() {
        ExceptionSummary summary = exceptionMetrics.getExceptionSummary();
        return BaseResponse.success(summary);
    }
    
    /**
     * 获取异常类型统计
     */
    @GetMapping("/counts")
    public BaseResponse<Map<String, Long>> getExceptionCounts() {
        Map<String, Long> counts = exceptionMetrics.getExceptionCounts();
        return BaseResponse.success(counts);
    }
    
    /**
     * 获取错误码统计
     */
    @GetMapping("/error-codes")
    public BaseResponse<Map<String, Long>> getErrorCodeCounts() {
        Map<String, Long> counts = exceptionMetrics.getErrorCodeCounts();
        return BaseResponse.success(counts);
    }
    
    /**
     * 获取最后异常时间
     */
    @GetMapping("/last-times")
    public BaseResponse<Map<String, String>> getLastExceptionTimes() {
        Map<String, String> times = exceptionMetrics.getLastExceptionTimes().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toString()
                ));
        return BaseResponse.success(times);
    }
    
    /**
     * 获取总异常数
     */
    @GetMapping("/total")
    public BaseResponse<Long> getTotalExceptionCount() {
        long total = exceptionMetrics.getTotalExceptionCount();
        return BaseResponse.success(total);
    }
    
    /**
     * 重置异常统计
     */
    @GetMapping("/reset")
    public BaseResponse<String> resetExceptionMetrics() {
        exceptionMetrics.reset();
        return BaseResponse.success("异常统计已重置");
    }
}
