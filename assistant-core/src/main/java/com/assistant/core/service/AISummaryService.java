package com.assistant.core.service;

import com.assistant.core.entity.FileIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * AI总结分析服务
 */
@Service
public class AISummaryService {

    private static final Logger logger = LoggerFactory.getLogger(AISummaryService.class);

    /**
     * 对搜索结果进行智能总结分析
     */
    public String generateSummary(String query, List<FileIndex> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return "未找到相关文件，无法生成总结。";
        }

        try {
            // 提取关键信息
            Map<String, Object> analysis = analyzeSearchResults(query, searchResults);
            
            // 生成总结报告
            StringBuilder summary = new StringBuilder();
            summary.append("## 搜索结果分析总结\n\n");
            
            // 基本信息
            summary.append("**查询关键词**: ").append(query).append("\n");
            summary.append("**找到相关文件**: ").append(searchResults.size()).append(" 个\n");
            summary.append("**分析时间**: ").append(java.time.LocalDateTime.now().toString()).append("\n\n");
            
            // 文件类型分布
            Map<String, Long> fileTypeDistribution = searchResults.stream()
                .collect(Collectors.groupingBy(
                    file -> file.getFileType() != null ? file.getFileType() : "未知",
                    Collectors.counting()
                ));
            
            summary.append("### 文件类型分布\n");
            fileTypeDistribution.forEach((type, count) -> 
                summary.append("- ").append(type).append(": ").append(count).append(" 个文件\n"));
            summary.append("\n");
            
            // 关键内容提取
            summary.append("### 关键信息提取\n");
            String keyContent = extractKeyContent(query, searchResults);
            summary.append(keyContent).append("\n\n");
            
            // 相关文件列表
            summary.append("### 相关文件列表\n");
            for (int i = 0; i < Math.min(searchResults.size(), 10); i++) {
                FileIndex file = searchResults.get(i);
                summary.append(i + 1).append(". **").append(file.getFileName()).append("**\n");
                if (file.getSummary() != null && !file.getSummary().trim().isEmpty()) {
                    summary.append("   - 摘要: ").append(truncateText(file.getSummary(), 200)).append("\n");
                }
                summary.append("   - 文件类型: ").append(file.getFileType()).append("\n");
                summary.append("   - 文件大小: ").append(formatFileSize(file.getFileSize())).append("\n\n");
            }
            
            if (searchResults.size() > 10) {
                summary.append("... 还有 ").append(searchResults.size() - 10).append(" 个相关文件\n");
            }
            
            return summary.toString();
            
        } catch (Exception e) {
            logger.error("生成总结失败", e);
            return "生成总结时发生错误: " + e.getMessage();
        }
    }

    /**
     * 分析搜索结果
     */
    private Map<String, Object> analyzeSearchResults(String query, List<FileIndex> searchResults) {
        Map<String, Object> analysis = new HashMap<>();
        
        // 分析文件类型分布
        Map<String, Long> fileTypeDistribution = searchResults.stream()
            .collect(Collectors.groupingBy(
                file -> file.getFileType() != null ? file.getFileType() : "未知",
                Collectors.counting()
            ));
        analysis.put("fileTypeDistribution", fileTypeDistribution);
        
        // 分析文件大小分布
        long totalSize = searchResults.stream()
            .mapToLong(file -> file.getFileSize() != null ? file.getFileSize() : 0)
            .sum();
        analysis.put("totalSize", totalSize);
        
        // 分析内容关键词
        String allContent = searchResults.stream()
            .map(FileIndex::getContent)
            .filter(content -> content != null && !content.trim().isEmpty())
            .collect(Collectors.joining(" "));
        analysis.put("contentLength", allContent.length());
        
        return analysis;
    }

    /**
     * 提取关键内容
     */
    private String extractKeyContent(String query, List<FileIndex> searchResults) {
        StringBuilder keyContent = new StringBuilder();
        
        // 根据查询关键词提取相关内容
        String[] queryWords = query.toLowerCase().split("[\\s，,、]");
        
        for (FileIndex file : searchResults.subList(0, Math.min(5, searchResults.size()))) {
            if (file.getContent() != null && !file.getContent().trim().isEmpty()) {
                String content = file.getContent();
                
                // 查找包含查询关键词的段落
                String[] paragraphs = content.split("[\\n\\r]+");
                for (String paragraph : paragraphs) {
                    if (paragraph.trim().length() > 50) { // 只处理较长的段落
                        boolean containsKeyword = false;
                        for (String word : queryWords) {
                            if (word.length() > 1 && paragraph.toLowerCase().contains(word)) {
                                containsKeyword = true;
                                break;
                            }
                        }
                        
                        if (containsKeyword) {
                            keyContent.append("- ").append(truncateText(paragraph.trim(), 300)).append("\n");
                            break; // 每个文件只取第一个相关段落
                        }
                    }
                }
            }
        }
        
        if (keyContent.length() == 0) {
            keyContent.append("未找到与查询关键词直接相关的内容段落。");
        }
        
        return keyContent.toString();
    }

    /**
     * 截断文本
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long size) {
        if (size == null) return "未知";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
}
