package com.assistant.web.controller;

import com.assistant.common.dto.BaseResponse;
import com.assistant.web.monitor.AlertManager;
import com.assistant.web.monitor.PerformanceMonitor;
import com.assistant.web.monitor.SystemMetricsCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 监控控制器
 * 提供系统监控和告警管理接口
 */
@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringController {
    
    @Autowired
    private SystemMetricsCollector metricsCollector;
    
    @Autowired
    private PerformanceMonitor performanceMonitor;
    
    @Autowired
    private AlertManager alertManager;
    
    /**
     * 获取系统指标
     */
    @GetMapping("/metrics/system")
    public BaseResponse<Map<String, Object>> getSystemMetrics() {
        try {
            Map<String, Object> metrics = metricsCollector.getSystemMetrics();
            return BaseResponse.success("系统指标获取成功", metrics);
        } catch (Exception e) {
            return BaseResponse.error("获取系统指标失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取内存使用情况
     */
    @GetMapping("/metrics/memory")
    public BaseResponse<Map<String, Object>> getMemoryUsage() {
        try {
            Map<String, Object> memoryInfo = metricsCollector.getMemoryUsage();
            return BaseResponse.success("内存使用情况获取成功", memoryInfo);
        } catch (Exception e) {
            return BaseResponse.error("获取内存使用情况失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取GC信息
     */
    @GetMapping("/metrics/gc")
    public BaseResponse<Map<String, Object>> getGCInfo() {
        try {
            Map<String, Object> gcInfo = metricsCollector.getGCInfo();
            return BaseResponse.success("GC信息获取成功", gcInfo);
        } catch (Exception e) {
            return BaseResponse.error("获取GC信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取线程信息
     */
    @GetMapping("/metrics/threads")
    public BaseResponse<Map<String, Object>> getThreadInfo() {
        try {
            Map<String, Object> threadInfo = metricsCollector.getThreadInfo();
            return BaseResponse.success("线程信息获取成功", threadInfo);
        } catch (Exception e) {
            return BaseResponse.error("获取线程信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取磁盘使用情况
     */
    @GetMapping("/metrics/disk")
    public BaseResponse<Map<String, Object>> getDiskUsage() {
        try {
            Map<String, Object> diskInfo = metricsCollector.getDiskUsage();
            return BaseResponse.success("磁盘使用情况获取成功", diskInfo);
        } catch (Exception e) {
            return BaseResponse.error("获取磁盘使用情况失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取性能统计
     */
    @GetMapping("/metrics/performance")
    public BaseResponse<Map<String, Object>> getPerformanceStats() {
        try {
            Map<String, Object> performanceStats = performanceMonitor.getPerformanceStats();
            return BaseResponse.success("性能统计获取成功", performanceStats);
        } catch (Exception e) {
            return BaseResponse.error("获取性能统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取活跃告警
     */
    @GetMapping("/alerts/active")
    public BaseResponse<List<AlertManager.Alert>> getActiveAlerts() {
        try {
            List<AlertManager.Alert> alerts = alertManager.getActiveAlerts();
            return BaseResponse.success("活跃告警获取成功", alerts);
        } catch (Exception e) {
            return BaseResponse.error("获取活跃告警失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取告警统计
     */
    @GetMapping("/alerts/stats")
    public BaseResponse<Map<String, Object>> getAlertStats() {
        try {
            Map<String, Object> alertStats = alertManager.getAlertStats();
            return BaseResponse.success("告警统计获取成功", alertStats);
        } catch (Exception e) {
            return BaseResponse.error("获取告警统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动触发告警
     */
    @PostMapping("/alerts/trigger")
    public BaseResponse<String> triggerAlert(
            @RequestParam String type,
            @RequestParam String message,
            @RequestParam(defaultValue = "WARNING") String level) {
        try {
            AlertManager.AlertLevel alertLevel = AlertManager.AlertLevel.valueOf(level.toUpperCase());
            alertManager.triggerManualAlert(type, message, alertLevel);
            return BaseResponse.success("告警触发成功");
        } catch (Exception e) {
            return BaseResponse.error("触发告警失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除所有告警
     */
    @PostMapping("/alerts/clear")
    public BaseResponse<String> clearAllAlerts() {
        try {
            alertManager.clearAllAlerts();
            return BaseResponse.success("所有告警已清除");
        } catch (Exception e) {
            return BaseResponse.error("清除告警失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置性能统计
     */
    @PostMapping("/performance/reset")
    public BaseResponse<String> resetPerformanceStats() {
        try {
            performanceMonitor.resetStats();
            return BaseResponse.success("性能统计已重置");
        } catch (Exception e) {
            return BaseResponse.error("重置性能统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取综合监控信息
     */
    @GetMapping("/dashboard")
    public BaseResponse<Map<String, Object>> getDashboard() {
        try {
            Map<String, Object> dashboard = Map.of(
                "systemMetrics", metricsCollector.getSystemMetrics(),
                "performanceStats", performanceMonitor.getPerformanceStats(),
                "activeAlerts", alertManager.getActiveAlerts(),
                "alertStats", alertManager.getAlertStats(),
                "memoryUsage", metricsCollector.getMemoryUsage(),
                "diskUsage", metricsCollector.getDiskUsage(),
                "threadInfo", metricsCollector.getThreadInfo(),
                "gcInfo", metricsCollector.getGCInfo(),
                "timestamp", System.currentTimeMillis()
            );
            return BaseResponse.success("监控面板信息获取成功", dashboard);
        } catch (Exception e) {
            return BaseResponse.error("获取监控面板信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取系统健康状态
     */
    @GetMapping("/health")
    public BaseResponse<Map<String, Object>> getSystemHealth() {
        try {
            Map<String, Object> health = Map.of(
                "status", "健康",
                "timestamp", System.currentTimeMillis(),
                "uptime", System.currentTimeMillis() - System.getProperty("java.class.path").hashCode(),
                "version", "1.0.0",
                "monitoringEnabled", true,
                "alertingEnabled", true
            );
            return BaseResponse.success("系统健康状态获取成功", health);
        } catch (Exception e) {
            return BaseResponse.error("获取系统健康状态失败: " + e.getMessage());
        }
    }
}
