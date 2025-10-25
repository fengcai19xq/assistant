package com.assistant.web.health;

import com.assistant.storage.service.VectorStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * 助手健康检查指示器
 * 监控系统各项指标
 */
@Component
public class AssistantHealthIndicator {
    
    private static final Logger logger = LoggerFactory.getLogger(AssistantHealthIndicator.class);
    
    @Autowired
    private VectorStorageService vectorStorageService;
    
    /**
     * 获取系统健康状态
     */
    public Map<String, Object> getHealthStatus() {
        try {
            Map<String, Object> details = new HashMap<>();
            
            // 检查磁盘空间
            File dataDir = new File(System.getProperty("user.home") + "/.file-assistant/data");
            if (dataDir.exists()) {
                long freeSpace = dataDir.getFreeSpace();
                long totalSpace = dataDir.getTotalSpace();
                double diskUsagePercent = (double) (totalSpace - freeSpace) / totalSpace * 100;
                
                details.put("diskFreeSpace", formatBytes(freeSpace));
                details.put("diskTotalSpace", formatBytes(totalSpace));
                details.put("diskUsagePercent", String.format("%.2f%%", diskUsagePercent));
            }
            
            // 检查内存使用
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
            
            details.put("memoryUsed", formatBytes(usedMemory));
            details.put("memoryMax", formatBytes(maxMemory));
            details.put("memoryUsagePercent", String.format("%.2f%%", memoryUsagePercent));
            
            // 检查CPU使用率
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            details.put("cpuUsage", "N/A"); // 简化CPU检查
            
            // 检查向量索引状态
            int vectorIndexSize = vectorStorageService.getIndexSize();
            details.put("vectorIndexSize", vectorIndexSize);
            
            // 检查存储统计
            Map<String, Object> storageStats = vectorStorageService.getStorageStats();
            details.put("storageStats", storageStats);
            
            // 系统信息
            details.put("javaVersion", System.getProperty("java.version"));
            details.put("osName", System.getProperty("os.name"));
            details.put("osVersion", System.getProperty("os.version"));
            details.put("availableProcessors", osBean.getAvailableProcessors());
            
            return details;
                
        } catch (Exception e) {
            logger.error("健康检查失败", e);
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("error", e.getMessage());
            errorDetails.put("status", "error");
            return errorDetails;
        }
    }
    
    /**
     * 格式化字节数
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
