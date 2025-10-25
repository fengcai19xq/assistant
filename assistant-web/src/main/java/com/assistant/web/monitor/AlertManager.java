package com.assistant.web.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 告警管理器
 * 监控系统指标并触发告警
 */
@Component
public class AlertManager {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertManager.class);
    
    @Autowired
    private SystemMetricsCollector metricsCollector;
    
    @Autowired
    private PerformanceMonitor performanceMonitor;
    
    @Value("${assistant.monitor.alert.enabled:true}")
    private boolean alertEnabled;
    
    @Value("${assistant.monitor.alert.memory-threshold:80}")
    private double memoryThreshold;
    
    @Value("${assistant.monitor.alert.disk-threshold:90}")
    private double diskThreshold;
    
    @Value("${assistant.monitor.alert.error-threshold:10}")
    private int errorThreshold;
    
    @Value("${assistant.monitor.alert.response-time-threshold:5000}")
    private long responseTimeThreshold;
    
    private ScheduledExecutorService scheduler;
    private final Map<String, Alert> activeAlerts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastAlertTime = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initialize() {
        if (alertEnabled) {
            scheduler = Executors.newScheduledThreadPool(1);
            
            // 每30秒检查一次告警条件
            scheduler.scheduleAtFixedRate(this::checkAlerts, 0, 30, TimeUnit.SECONDS);
            
            logger.info("告警管理器初始化完成");
        }
    }
    
    /**
     * 检查告警条件
     */
    private void checkAlerts() {
        try {
            Map<String, Object> systemMetrics = metricsCollector.getSystemMetrics();
            Map<String, Object> performanceStats = performanceMonitor.getPerformanceStats();
            
            // 检查内存使用率
            checkMemoryAlert(systemMetrics);
            
            // 检查磁盘使用率
            checkDiskAlert(systemMetrics);
            
            // 检查错误率
            checkErrorAlert(performanceStats);
            
            // 检查响应时间
            checkResponseTimeAlert(performanceStats);
            
            // 检查GC告警
            checkGCAlert(systemMetrics);
            
            // 检查线程告警
            checkThreadAlert(systemMetrics);
            
        } catch (Exception e) {
            logger.error("检查告警条件失败", e);
        }
    }
    
    /**
     * 检查内存告警
     */
    private void checkMemoryAlert(Map<String, Object> metrics) {
        try {
            Object heapUsagePercent = metrics.get("heapUsagePercent");
            if (heapUsagePercent instanceof Number) {
                double usage = ((Number) heapUsagePercent).doubleValue();
                String alertKey = "memory_high";
                if (usage > memoryThreshold) {
                    Alert alert = new Alert(
                        "MEMORY_HIGH",
                        "内存使用率过高",
                        String.format("当前内存使用率: %.2f%%, 阈值: %.2f%%", usage, memoryThreshold),
                        AlertLevel.WARNING,
                        System.currentTimeMillis()
                    );
                    triggerAlert(alertKey, alert);
                } else {
                    clearAlert(alertKey);
                }
            }
        } catch (Exception e) {
            logger.error("检查内存告警失败", e);
        }
    }
    
    /**
     * 检查磁盘告警
     */
    private void checkDiskAlert(Map<String, Object> metrics) {
        try {
            Object diskUsagePercent = metrics.get("diskUsagePercent");
            if (diskUsagePercent instanceof Number) {
                double usage = ((Number) diskUsagePercent).doubleValue();
                String alertKey = "disk_high";
                if (usage > diskThreshold) {
                    Alert alert = new Alert(
                        "DISK_HIGH",
                        "磁盘使用率过高",
                        String.format("当前磁盘使用率: %.2f%%, 阈值: %.2f%%", usage, diskThreshold),
                        AlertLevel.WARNING,
                        System.currentTimeMillis()
                    );
                    triggerAlert(alertKey, alert);
                } else {
                    clearAlert(alertKey);
                }
            }
        } catch (Exception e) {
            logger.error("检查磁盘告警失败", e);
        }
    }
    
    /**
     * 检查错误告警
     */
    private void checkErrorAlert(Map<String, Object> performanceStats) {
        try {
            Object totalErrors = performanceStats.get("totalErrors");
            if (totalErrors instanceof Number) {
                int errorCount = ((Number) totalErrors).intValue();
                String alertKey = "error_high";
                if (errorCount > errorThreshold) {
                    Alert alert = new Alert(
                        "ERROR_HIGH",
                        "错误数量过多",
                        String.format("当前错误数量: %d, 阈值: %d", errorCount, errorThreshold),
                        AlertLevel.ERROR,
                        System.currentTimeMillis()
                    );
                    triggerAlert(alertKey, alert);
                } else {
                    clearAlert(alertKey);
                }
            }
        } catch (Exception e) {
            logger.error("检查错误告警失败", e);
        }
    }
    
    /**
     * 检查响应时间告警
     */
    private void checkResponseTimeAlert(Map<String, Object> performanceStats) {
        try {
            Object avgSearchTime = performanceStats.get("avgSearchTimeMs");
            if (avgSearchTime instanceof String) {
                double responseTime = Double.parseDouble((String) avgSearchTime);
                String alertKey = "response_time_high";
                if (responseTime > responseTimeThreshold) {
                    Alert alert = new Alert(
                        "RESPONSE_TIME_HIGH",
                        "响应时间过长",
                        String.format("平均响应时间: %.2fms, 阈值: %dms", responseTime, responseTimeThreshold),
                        AlertLevel.WARNING,
                        System.currentTimeMillis()
                    );
                    triggerAlert(alertKey, alert);
                } else {
                    clearAlert(alertKey);
                }
            }
        } catch (Exception e) {
            logger.error("检查响应时间告警失败", e);
        }
    }
    
    /**
     * 检查GC告警
     */
    private void checkGCAlert(Map<String, Object> metrics) {
        try {
            // 检查GC时间是否过长
            for (Map.Entry<String, Object> entry : metrics.entrySet()) {
                if (entry.getKey().startsWith("gc_") && entry.getKey().endsWith("_time")) {
                    Object gcTime = entry.getValue();
                    if (gcTime instanceof Number) {
                        long time = ((Number) gcTime).longValue();
                        if (time > 1000) { // GC时间超过1秒
                            String alertKey = "gc_long_" + entry.getKey();
                            Alert alert = new Alert(
                                "GC_LONG",
                                "GC时间过长",
                                String.format("GC %s 耗时: %dms", entry.getKey(), time),
                                AlertLevel.WARNING,
                                System.currentTimeMillis()
                            );
                            triggerAlert(alertKey, alert);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("检查GC告警失败", e);
        }
    }
    
    /**
     * 检查线程告警
     */
    private void checkThreadAlert(Map<String, Object> metrics) {
        try {
            Object threadCount = metrics.get("threadCount");
            if (threadCount instanceof Number) {
                int count = ((Number) threadCount).intValue();
                String alertKey = "thread_high";
                if (count > 200) { // 线程数过多
                    Alert alert = new Alert(
                        "THREAD_HIGH",
                        "线程数量过多",
                        String.format("当前线程数: %d", count),
                        AlertLevel.WARNING,
                        System.currentTimeMillis()
                    );
                    triggerAlert(alertKey, alert);
                } else {
                    clearAlert(alertKey);
                }
            }
        } catch (Exception e) {
            logger.error("检查线程告警失败", e);
        }
    }
    
    /**
     * 触发告警
     */
    private void triggerAlert(String alertKey, Alert alert) {
        // 防止重复告警（5分钟内不重复）
        Long lastTime = lastAlertTime.get(alertKey);
        if (lastTime != null && System.currentTimeMillis() - lastTime < 300000) {
            return;
        }
        
        activeAlerts.put(alertKey, alert);
        lastAlertTime.put(alertKey, System.currentTimeMillis());
        
        logger.warn("告警触发: {} - {}", alert.getType(), alert.getMessage());
    }
    
    /**
     * 清除告警
     */
    private void clearAlert(String alertKey) {
        Alert removed = activeAlerts.remove(alertKey);
        if (removed != null) {
            logger.info("告警清除: {} - {}", removed.getType(), removed.getMessage());
        }
    }
    
    /**
     * 获取活跃告警
     */
    public List<Alert> getActiveAlerts() {
        return new ArrayList<>(activeAlerts.values());
    }
    
    /**
     * 获取告警统计
     */
    public Map<String, Object> getAlertStats() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Alert> alerts = getActiveAlerts();
        stats.put("totalAlerts", alerts.size());
        stats.put("warningAlerts", alerts.stream().filter(a -> a.getLevel() == AlertLevel.WARNING).count());
        stats.put("errorAlerts", alerts.stream().filter(a -> a.getLevel() == AlertLevel.ERROR).count());
        stats.put("criticalAlerts", alerts.stream().filter(a -> a.getLevel() == AlertLevel.CRITICAL).count());
        
        return stats;
    }
    
    /**
     * 手动触发告警
     */
    public void triggerManualAlert(String type, String message, AlertLevel level) {
        String alertKey = "manual_" + type.toLowerCase();
        Alert alert = new Alert(type, message, message, level, System.currentTimeMillis());
        triggerAlert(alertKey, alert);
    }
    
    /**
     * 清除所有告警
     */
    public void clearAllAlerts() {
        activeAlerts.clear();
        lastAlertTime.clear();
        logger.info("所有告警已清除");
    }
    
    /**
     * 告警级别枚举
     */
    public enum AlertLevel {
        INFO, WARNING, ERROR, CRITICAL
    }
    
    /**
     * 告警实体
     */
    public static class Alert {
        private final String type;
        private final String title;
        private final String message;
        private final AlertLevel level;
        private final long timestamp;
        
        public Alert(String type, String title, String message, AlertLevel level, long timestamp) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.level = level;
            this.timestamp = timestamp;
        }
        
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public AlertLevel getLevel() { return level; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * 清理资源
     */
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
