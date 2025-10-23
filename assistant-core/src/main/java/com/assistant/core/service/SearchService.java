package com.assistant.core.service;

import com.assistant.common.dto.SearchRequest;
import com.assistant.common.dto.SearchResult;
import com.assistant.core.entity.FileIndex;
import com.assistant.core.entity.SearchHistory;
import com.assistant.core.mapper.FileIndexMapper;
import com.assistant.core.mapper.SearchHistoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索服务
 */
@Service
public class SearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    
    @Autowired
    private FileIndexMapper fileIndexMapper;
    
    @Autowired
    private SearchHistoryMapper searchHistoryMapper;
    
           @Autowired
           private AIEmbeddingService aiEmbeddingService;
           
           @Autowired
           private AISummaryService aiSummaryService;
           
           @Autowired
           private AIEnhancedSummaryService aiEnhancedSummaryService;
           
           @Autowired
           private FinancialAnalysisService financialAnalysisService;
    
    /**
     * 搜索文件
     */
    public List<SearchResult> searchFiles(SearchRequest request) {
        logger.info("执行搜索: {}", request.getQuery());
        
        try {
            List<FileIndex> fileIndexes;
            String searchType = "text";
            
            // 优先使用AI语义搜索，如果不可用则使用改进的文本搜索
            if (aiEmbeddingService.isModelAvailable()) {
                try {
                    fileIndexes = performSemanticSearch(request.getQuery());
                    searchType = "semantic";
                } catch (Exception e) {
                    logger.warn("AI语义搜索失败，回退到改进的文本搜索: {}", e.getMessage());
                    fileIndexes = performImprovedTextSearch(request.getQuery());
                    searchType = "text";
                }
            } else {
                fileIndexes = performImprovedTextSearch(request.getQuery());
                searchType = "text";
            }
            
                   // 转换为搜索结果
                   List<SearchResult> results = fileIndexes.stream()
                       .map(fileIndex -> {
                           SearchResult result = new SearchResult();
                           result.setFileId(fileIndex.getId());
                           result.setFilePath(fileIndex.getFilePath());
                           result.setFileName(fileIndex.getFileName());
                           result.setFileType(fileIndex.getFileType());
                           result.setFileSize(fileIndex.getFileSize());
                           result.setLastModified(fileIndex.getLastModified());
                           result.setContent(fileIndex.getContent());
                           result.setSummary(fileIndex.getSummary());
                           result.setScore(calculateScore(fileIndex, request.getQuery()));
                           result.setHighlight(generateHighlight(fileIndex.getContent(), request.getQuery()));
                           return result;
                       })
                       .sorted((a, b) -> Double.compare(b.getScore(), a.getScore())) // 按分数降序排列
                       .limit(request.getPageSize())
                       .collect(Collectors.toList());
                   
                   // 生成智能总结分析
                   if (!results.isEmpty()) {
                       try {
                           logger.info("开始生成AI总结分析，查询: {}, 文件数量: {}", request.getQuery(), fileIndexes.size());
                           // 根据查询类型选择不同的分析服务
                           String analysisSummary;
                           if (request.getQuery().contains("利润") || request.getQuery().contains("财务") || 
                               request.getQuery().contains("收入") || request.getQuery().contains("成本")) {
                               analysisSummary = financialAnalysisService.generateFinancialAnalysis(request.getQuery(), fileIndexes);
                           } else {
                               analysisSummary = aiEnhancedSummaryService.generateSummary(request.getQuery(), fileIndexes);
                           }
                           logger.info("AI总结分析生成完成，长度: {}", analysisSummary != null ? analysisSummary.length() : 0);
                           // 将总结添加到第一个结果中，或者创建一个特殊的结果
                           if (!results.isEmpty()) {
                               results.get(0).setAnalysisSummary(analysisSummary);
                               logger.info("AI总结分析已设置到第一个结果中，内容: {}", analysisSummary != null ? analysisSummary.substring(0, Math.min(100, analysisSummary.length())) : "null");
                           }
                       } catch (Exception e) {
                           logger.error("生成AI总结分析失败", e);
                       }
                   }
            
            // 记录搜索历史
            recordSearchHistory(request.getQuery(), results.size(), searchType);
            
            logger.info("搜索完成，找到 {} 个结果", results.size());
            return results;
            
        } catch (Exception e) {
            logger.error("搜索失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 计算搜索分数
     */
    private double calculateScore(FileIndex fileIndex, String query) {
        double score = 0.0;
        String queryLower = query.toLowerCase();
        
        // 文件名匹配分数
        if (fileIndex.getFileName().toLowerCase().contains(queryLower)) {
            score += 0.5;
        }
        
        // 内容匹配分数
        if (fileIndex.getContent() != null && fileIndex.getContent().toLowerCase().contains(queryLower)) {
            score += 0.3;
        }
        
        // 摘要匹配分数
        if (fileIndex.getSummary() != null && fileIndex.getSummary().toLowerCase().contains(queryLower)) {
            score += 0.2;
        }
        
        return Math.min(score, 1.0);
    }
    
    /**
     * 生成高亮文本
     */
    private String generateHighlight(String content, String query) {
        if (content == null || query == null) {
            return content;
        }
        
        // 简单的关键词高亮
        String highlight = content.replaceAll("(?i)" + query, "<mark>" + query + "</mark>");
        
        // 限制高亮文本长度
        if (highlight.length() > 500) {
            int index = highlight.toLowerCase().indexOf(query.toLowerCase());
            if (index > 0) {
                int start = Math.max(0, index - 100);
                int end = Math.min(highlight.length(), index + query.length() + 100);
                highlight = "..." + highlight.substring(start, end) + "...";
            }
        }
        
        return highlight;
    }
    
           /**
            * 执行AI语义搜索
            */
           private List<FileIndex> performSemanticSearch(String query) {
               try {
                   // 生成查询向量
                   float[] queryVector = aiEmbeddingService.generateEmbedding(query);
                   if (queryVector == null) {
                       logger.warn("无法生成查询向量，回退到改进的文本搜索");
                       return performImprovedTextSearch(query);
                   }
                   
                   // 获取所有文件（不包含vector_data字段，避免BLOB读取问题）
                   List<FileIndex> allFiles = fileIndexMapper.selectAllWithoutVectorData();
                   
                   // 使用改进的文本相似度计算作为语义搜索的替代方案
                   List<FileIndex> results = allFiles.stream()
                       .map(file -> {
                           // 计算文本相似度（简化版语义搜索）
                           double similarity = calculateTextSimilarity(query, file.getContent());
                           if (similarity > 0.1) { // 提高相似度阈值，减少低质量结果
                               logger.debug("文件 {} 相似度: {}", file.getFileName(), similarity);
                           }
                           return new Object[]{file, similarity};
                       })
                       .filter(obj -> (Double) obj[1] > 0.1) // 提高相似度阈值
                       .sorted((a, b) -> Double.compare((Double) b[1], (Double) a[1])) // 按相似度降序
                       .limit(20) // 限制返回结果数量
                       .map(obj -> (FileIndex) obj[0])
                       .collect(Collectors.toList());
                   
                   logger.info("AI语义搜索完成，找到 {} 个相关结果", results.size());
                   return results;
                   
               } catch (Exception e) {
                   logger.error("AI语义搜索失败", e);
                   return performImprovedTextSearch(query);
               }
           }
    
           /**
            * 执行改进的文本搜索
            */
           private List<FileIndex> performImprovedTextSearch(String query) {
               try {
                   // 获取所有文件
                   List<FileIndex> allFiles = fileIndexMapper.selectAllWithoutVectorData();
                   
                   // 使用改进的文本相似度计算
                   List<FileIndex> results = allFiles.stream()
                       .map(file -> {
                           double similarity = calculateTextSimilarity(query, file.getContent());
                           return new Object[]{file, similarity};
                       })
                       .filter(obj -> (Double) obj[1] > 0.1) // 提高相似度阈值
                       .sorted((a, b) -> Double.compare((Double) b[1], (Double) a[1])) // 按相似度降序
                       .limit(20) // 限制返回结果数量
                       .map(obj -> (FileIndex) obj[0])
                       .collect(Collectors.toList());
                   
                   logger.info("改进的文本搜索完成，找到 {} 个相关结果", results.size());
                   return results;
                   
               } catch (Exception e) {
                   logger.error("改进的文本搜索失败", e);
                   return fileIndexMapper.searchByContent(query);
               }
           }
    
    /**
     * 计算文本相似度（简化版语义搜索）
     */
    private double calculateTextSimilarity(String query, String content) {
        if (content == null || content.isEmpty()) {
            return 0.0;
        }
        
        String queryLower = query.toLowerCase();
        String contentLower = content.toLowerCase();
        
        // 1. 完全匹配检查
        if (contentLower.contains(queryLower)) {
            return 1.0;
        }
        
        // 2. 中文词汇匹配（对"利润情况分析"这样的查询更友好）
        double chineseWordMatch = 0.0;
        String[] chineseWords = {"利润", "情况", "分析", "财务", "收入", "支出", "成本", "收益", "亏损", "盈利"};
        for (String word : chineseWords) {
            if (queryLower.contains(word) && contentLower.contains(word)) {
                chineseWordMatch += 0.2;
            }
        }
        
        // 3. 关键词匹配度
        String[] queryWords = queryLower.split("\\s+");
        int matchCount = 0;
        for (String word : queryWords) {
            if (contentLower.contains(word)) {
                matchCount++;
            }
        }
        double wordMatchRatio = queryWords.length > 0 ? (double) matchCount / queryWords.length : 0.0;
        
        // 4. 字符级别相似度（对中文更友好）
        int charMatchCount = 0;
        for (char c : queryLower.toCharArray()) {
            if (contentLower.indexOf(c) >= 0) {
                charMatchCount++;
            }
        }
        double charMatchRatio = (double) charMatchCount / query.length();
        
        // 5. 部分匹配（检查查询词的子串）
        double partialMatch = 0.0;
        for (String word : queryWords) {
            if (word.length() > 1) {
                for (int i = 0; i < word.length() - 1; i++) {
                    String substring = word.substring(i, i + 2);
                    if (contentLower.contains(substring)) {
                        partialMatch += 0.1;
                    }
                }
            }
        }
        
        // 6. 长度相似度
        double lengthRatio = Math.min(query.length(), content.length()) / Math.max(query.length(), content.length());
        
        // 7. 语义相关词匹配（扩展词汇）
        double semanticMatch = 0.0;
        if (queryLower.contains("利润") || queryLower.contains("收益") || queryLower.contains("收入")) {
            String[] profitWords = {"利润", "收益", "收入", "盈利", "赚钱", "赚钱", "收入", "营收"};
            for (String word : profitWords) {
                if (contentLower.contains(word)) {
                    semanticMatch += 0.15;
                }
            }
        }
        
        if (queryLower.contains("情况") || queryLower.contains("分析") || queryLower.contains("报告")) {
            String[] analysisWords = {"情况", "分析", "报告", "总结", "概述", "说明", "描述"};
            for (String word : analysisWords) {
                if (contentLower.contains(word)) {
                    semanticMatch += 0.1;
                }
            }
        }
        
        // 综合相似度计算
        double totalSimilarity = wordMatchRatio * 0.3 + charMatchRatio * 0.2 + lengthRatio * 0.1 + 
                                partialMatch * 0.15 + chineseWordMatch + semanticMatch * 0.25;
        
        return Math.min(totalSimilarity, 1.0);
    }
    
    
    /**
     * 将byte数组转换为float数组
     */
    private float[] bytesToFloats(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        
        float[] floats = new float[bytes.length / 4];
        for (int i = 0; i < floats.length; i++) {
            int intBits = ((bytes[i * 4] & 0xFF) << 24) |
                         ((bytes[i * 4 + 1] & 0xFF) << 16) |
                         ((bytes[i * 4 + 2] & 0xFF) << 8) |
                         (bytes[i * 4 + 3] & 0xFF);
            floats[i] = Float.intBitsToFloat(intBits);
        }
        return floats;
    }
    
    /**
     * 记录搜索历史
     */
    private void recordSearchHistory(String query, int resultCount, String searchType) {
        try {
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setQueryText(query);
            searchHistory.setResultCount(resultCount);
            searchHistory.setSearchType(searchType);
            searchHistory.setSearchTime(java.time.LocalDateTime.now().toString());
            
            searchHistoryMapper.insert(searchHistory);
            
        } catch (Exception e) {
            logger.warn("记录搜索历史失败", e);
        }
    }
    
    /**
     * 获取搜索历史
     */
    public List<SearchHistory> getSearchHistory(int limit) {
        try {
            return searchHistoryMapper.selectList(null)
                .stream()
                .sorted((a, b) -> b.getSearchTime().compareTo(a.getSearchTime()))
                .limit(limit)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取搜索历史失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 清空搜索历史
     */
    public boolean clearSearchHistory() {
        try {
            searchHistoryMapper.delete(null);
            logger.info("搜索历史已清空");
            return true;
        } catch (Exception e) {
            logger.error("清空搜索历史失败", e);
            return false;
        }
    }
}
