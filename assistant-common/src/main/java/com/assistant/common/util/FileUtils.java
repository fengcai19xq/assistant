package com.assistant.common.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 文件工具类
 */
public class FileUtils {
    
    // 支持的文件类型
    private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(Arrays.asList(
        "txt", "md", "doc", "docx", "pdf", "ppt", "pptx", "xls", "xlsx",
        "java", "js", "ts", "py", "cpp", "c", "h", "xml", "json", "yaml", "yml",
        "html", "htm", "css", "sql", "sh", "bat"
    ));
    
    // 排除的文件类型
    private static final Set<String> EXCLUDED_EXTENSIONS = new HashSet<>(Arrays.asList(
        "tmp", "temp", "log", "cache", "bak", "swp", "~"
    ));
    
    // 排除的目录名
    private static final Set<String> EXCLUDED_DIRS = new HashSet<>(Arrays.asList(
        ".git", ".svn", ".idea", ".vscode", "node_modules", "target", "build",
        ".DS_Store", "Thumbs.db", "__pycache__", ".pytest_cache"
    ));
    
    /**
     * 检查文件是否应该被索引
     */
    public static boolean shouldIndex(Path filePath) {
        if (filePath == null) {
            return false;
        }
        
        File file = filePath.toFile();
        
        // 检查文件是否存在
        if (!file.exists() || !file.isFile()) {
            return false;
        }
        
        // 检查文件大小（限制为50MB）
        if (file.length() > 50 * 1024 * 1024) {
            return false;
        }
        
        // 检查文件扩展名
        String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
        if (StringUtils.isBlank(extension) || EXCLUDED_EXTENSIONS.contains(extension) || !SUPPORTED_EXTENSIONS.contains(extension)) {
            return false;
        }
        
        // 检查是否在排除的目录中
        Path parent = filePath.getParent();
        while (parent != null) {
            if (parent.getFileName() != null) {
                String dirName = parent.getFileName().toString();
                if (EXCLUDED_DIRS.contains(dirName)) {
                    return false;
                }
            }
            parent = parent.getParent();
        }
        
        return true;
    }
    
    /**
     * 获取文件类型
     */
    public static String getFileType(String fileName) {
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        
        switch (extension) {
            case "txt":
            case "md":
                return "text";
            case "doc":
            case "docx":
                return "document";
            case "pdf":
                return "pdf";
            case "ppt":
            case "pptx":
                return "presentation";
            case "xls":
            case "xlsx":
                return "spreadsheet";
            case "java":
            case "js":
            case "ts":
            case "py":
            case "cpp":
            case "c":
            case "h":
                return "code";
            case "html":
            case "htm":
            case "css":
                return "web";
            default:
                return "other";
        }
    }
    
    /**
     * 检查是否为支持的文本文件
     */
    public static boolean isTextFile(String fileName) {
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        return SUPPORTED_EXTENSIONS.contains(extension);
    }
    
    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
