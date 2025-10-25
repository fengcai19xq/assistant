package com.assistant.web.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.management.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 系统指标收集器
 * 收集系统级别的监控指标
 */
@Component
public class SystemMetricsCollector {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemMetricsCollector.class);
    
    @Autowired
    private PerformanceMonitor performanceMonitor;
    
    private ScheduledExecutorService scheduler;
    private final Map<String, Object> systemMetrics = new HashMap<>();
    
    @PostConstruct
    public void initialize() {
        scheduler = Executors.newScheduledThreadPool(2);
        
        // 每30秒收集一次系统指标
        scheduler.scheduleAtFixedRate(this::collectSystemMetrics, 0, 30, TimeUnit.SECONDS);
        
        // 每5分钟收集一次详细指标
        scheduler.scheduleAtFixedRate(this::collectDetailedMetrics, 0, 5, TimeUnit.MINUTES);
        
        logger.info("系统指标收集器初始化完成");
    }
    
    /**
     * 收集基础系统指标
     */
    private void collectSystemMetrics() {
        try {
            Map<String, Object> metrics = new HashMap<>();
            
            // JVM内存指标
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            
            metrics.put("heapUsed", heapUsage.getUsed());
            metrics.put("heapMax", heapUsage.getMax());
            metrics.put("heapCommitted", heapUsage.getCommitted());
            metrics.put("nonHeapUsed", nonHeapUsage.getUsed());
            metrics.put("nonHeapMax", nonHeapUsage.getMax());
            
            // 计算内存使用率
            double heapUsagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
            metrics.put("heapUsagePercent", Math.round(heapUsagePercent * 100.0) / 100.0);
            
            // 线程指标
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            metrics.put("threadCount", threadBean.getThreadCount());
            metrics.put("peakThreadCount", threadBean.getPeakThreadCount());
            metrics.put("daemonThreadCount", threadBean.getDaemonThreadCount());
            
            // GC指标
            for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                String gcName = gcBean.getName();
                metrics.put("gc_" + gcName + "_count", gcBean.getCollectionCount());
                metrics.put("gc_" + gcName + "_time", gcBean.getCollectionTime());
            }
            
            // 操作系统指标
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            metrics.put("availableProcessors", osBean.getAvailableProcessors());
            metrics.put("systemLoadAverage", osBean.getSystemLoadAverage());
            
            // 磁盘空间
            File dataDir = new File(System.getProperty("user.home") + "/.file-assistant/data");
            if (dataDir.exists()) {
                long freeSpace = dataDir.getFreeSpace();
                long totalSpace = dataDir.getTotalSpace();
                long usedSpace = totalSpace - freeSpace;
                
                metrics.put("diskFreeSpace", freeSpace);
                metrics.put("diskTotalSpace", totalSpace);
                metrics.put("diskUsedSpace", usedSpace);
                metrics.put("diskUsagePercent", Math.round((double) usedSpace / totalSpace * 10000.0) / 100.0);
            }
            
            // 更新时间戳
            metrics.put("timestamp", System.currentTimeMillis());
            
            synchronized (systemMetrics) {
                systemMetrics.putAll(metrics);
            }
            
            logger.debug("系统指标收集完成");
            
        } catch (Exception e) {
            logger.error("收集系统指标失败", e);
        }
    }
    
    /**
     * 收集详细指标
     */
    private void collectDetailedMetrics() {
        try {
            Map<String, Object> detailedMetrics = new HashMap<>();
            
            // 运行时指标
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            detailedMetrics.put("uptime", runtimeBean.getUptime());
            detailedMetrics.put("startTime", runtimeBean.getStartTime());
            detailedMetrics.put("jvmName", runtimeBean.getVmName());
            detailedMetrics.put("jvmVersion", runtimeBean.getVmVersion());
            detailedMetrics.put("jvmVendor", runtimeBean.getVmVendor());
            
            // 类加载指标
            ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
            detailedMetrics.put("loadedClassCount", classBean.getLoadedClassCount());
            detailedMetrics.put("totalLoadedClassCount", classBean.getTotalLoadedClassCount());
            detailedMetrics.put("unloadedClassCount", classBean.getUnloadedClassCount());
            
            // 编译指标
            CompilationMXBean compilationBean = ManagementFactory.getCompilationMXBean();
            if (compilationBean != null) {
                detailedMetrics.put("compilationTime", compilationBean.getTotalCompilationTime());
            }
            
            // 性能监控指标
            Map<String, Object> performanceStats = performanceMonitor.getPerformanceStats();
            detailedMetrics.putAll(performanceStats);
            
            synchronized (systemMetrics) {
                systemMetrics.put("detailed", detailedMetrics);
            }
            
            logger.debug("详细指标收集完成");
            
        } catch (Exception e) {
            logger.error("收集详细指标失败", e);
        }
    }
    
    /**
     * 获取系统指标
     */
    public Map<String, Object> getSystemMetrics() {
        synchronized (systemMetrics) {
            return new HashMap<>(systemMetrics);
        }
    }
    
    /**
     * 获取内存使用情况
     */
    public Map<String, Object> getMemoryUsage() {
        Map<String, Object> memoryInfo = new HashMap<>();
        
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            
            Map<String, Object> heapInfo = new HashMap<>();
            heapInfo.put("used", heapUsage.getUsed());
            heapInfo.put("max", heapUsage.getMax());
            heapInfo.put("committed", heapUsage.getCommitted());
            heapInfo.put("usagePercent", Math.round((double) heapUsage.getUsed() / heapUsage.getMax() * 10000.0) / 100.0);
            memoryInfo.put("heap", heapInfo);
            
            Map<String, Object> nonHeapInfo = new HashMap<>();
            nonHeapInfo.put("used", nonHeapUsage.getUsed());
            nonHeapInfo.put("max", nonHeapUsage.getMax());
            nonHeapInfo.put("committed", nonHeapUsage.getCommitted());
            memoryInfo.put("nonHeap", nonHeapInfo);
            
        } catch (Exception e) {
            logger.error("获取内存使用情况失败", e);
        }
        
        return memoryInfo;
    }
    
    /**
     * 获取GC信息
     */
    public Map<String, Object> getGCInfo() {
        Map<String, Object> gcInfo = new HashMap<>();
        
        try {
            for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                String gcName = gcBean.getName();
                Map<String, Object> gcBeanInfo = new HashMap<>();
                gcBeanInfo.put("collectionCount", gcBean.getCollectionCount());
                gcBeanInfo.put("collectionTime", gcBean.getCollectionTime());
                gcInfo.put(gcName, gcBeanInfo);
            }
        } catch (Exception e) {
            logger.error("获取GC信息失败", e);
        }
        
        return gcInfo;
    }
    
    /**
     * 获取线程信息
     */
    public Map<String, Object> getThreadInfo() {
        Map<String, Object> threadInfo = new HashMap<>();
        
        try {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            
            threadInfo.put("threadCount", threadBean.getThreadCount());
            threadInfo.put("peakThreadCount", threadBean.getPeakThreadCount());
            threadInfo.put("daemonThreadCount", threadBean.getDaemonThreadCount());
            threadInfo.put("totalStartedThreadCount", threadBean.getTotalStartedThreadCount());
            
            // 获取死锁线程
            long[] deadlockedThreads = threadBean.findDeadlockedThreads();
            if (deadlockedThreads != null) {
                threadInfo.put("deadlockedThreadCount", deadlockedThreads.length);
            } else {
                threadInfo.put("deadlockedThreadCount", 0);
            }
            
        } catch (Exception e) {
            logger.error("获取线程信息失败", e);
        }
        
        return threadInfo;
    }
    
    /**
     * 获取磁盘使用情况
     */
    public Map<String, Object> getDiskUsage() {
        Map<String, Object> diskInfo = new HashMap<>();
        
        try {
            File dataDir = new File(System.getProperty("user.home") + "/.file-assistant/data");
            if (dataDir.exists()) {
                long freeSpace = dataDir.getFreeSpace();
                long totalSpace = dataDir.getTotalSpace();
                long usedSpace = totalSpace - freeSpace;
                
                diskInfo.put("freeSpace", freeSpace);
                diskInfo.put("totalSpace", totalSpace);
                diskInfo.put("usedSpace", usedSpace);
                diskInfo.put("usagePercent", Math.round((double) usedSpace / totalSpace * 10000.0) / 100.0);
                diskInfo.put("freeSpaceFormatted", formatBytes(freeSpace));
                diskInfo.put("totalSpaceFormatted", formatBytes(totalSpace));
                diskInfo.put("usedSpaceFormatted", formatBytes(usedSpace));
            }
        } catch (Exception e) {
            logger.error("获取磁盘使用情况失败", e);
        }
        
        return diskInfo;
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
