package com.assistant.core.service;

import com.assistant.common.dto.SearchRequest;
import com.assistant.common.dto.SearchResult;
import com.assistant.core.mapper.FileIndexMapper;
import com.assistant.core.entity.FileIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 优化版搜索服务
 * 实现缓存、异步处理、索引优化等功能
 */
@Service
public class OptimizedSearchService {

    private static final Logger logger = LoggerFactory.getLogger(OptimizedSearchService.class);

    @Autowired
    private FileIndexMapper fileIndexMapper;

    @Autowired
    private FileIndexService fileIndexService;

    // 搜索缓存
    private final Map<String, List<SearchResult>> searchCache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRE_TIME = 5 * 60 * 1000; // 5分钟缓存

    // 异步处理线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    // 索引缓存
    private final Map<String, FileIndex> fileIndexCache = new ConcurrentHashMap<>();
    private volatile boolean indexCacheInitialized = false;

    /**
     * 优化的搜索方法
     */
    public CompletableFuture<List<SearchResult>> searchFilesAsync(SearchRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String cacheKey = generateCacheKey(request);
            
            // 检查缓存
            if (isCacheValid(cacheKey)) {
                logger.debug("从缓存返回搜索结果: {}", request.getQuery());
                return searchCache.get(cacheKey);
            }

            // 执行搜索
            List<SearchResult> results = performOptimizedSearch(request);
            
            // 更新缓存
            searchCache.put(cacheKey, results);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
            
            return results;
        }, executorService);
    }

    /**
     * 同步搜索方法（保持兼容性）
     */
    public List<SearchResult> searchFiles(SearchRequest request) {
        try {
            return searchFilesAsync(request).get();
        } catch (Exception e) {
            logger.error("搜索失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 执行优化的搜索
     */
    private List<SearchResult> performOptimizedSearch(SearchRequest request) {
        logger.info("执行优化搜索: {}", request.getQuery());
        
        try {
            // 1. 初始化索引缓存
            initializeIndexCache();
            
            // 2. 多阶段搜索策略
            List<SearchResult> results = new ArrayList<>();
            
            // 阶段1: 精确匹配搜索
            List<SearchResult> exactMatches = performExactMatchSearch(request);
            results.addAll(exactMatches);
            
            // 阶段2: 模糊匹配搜索
            if (results.size() < request.getPageSize()) {
                List<SearchResult> fuzzyMatches = performFuzzyMatchSearch(request, results);
                results.addAll(fuzzyMatches);
            }
            
            // 阶段3: 语义搜索（如果前两个阶段结果不足）
            if (results.size() < request.getPageSize()) {
                List<SearchResult> semanticMatches = performSemanticSearch(request, results);
                results.addAll(semanticMatches);
            }
            
            // 去重和排序
            results = deduplicateAndSort(results, request.getQuery());
            
            // 限制结果数量
            results = results.stream()
                    .limit(request.getPageSize())
                    .collect(Collectors.toList());
            
            logger.info("优化搜索完成，返回 {} 个结果", results.size());
            return results;
            
        } catch (Exception e) {
            logger.error("优化搜索失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 精确匹配搜索
     */
    private List<SearchResult> performExactMatchSearch(SearchRequest request) {
        try {
            List<FileIndex> exactMatches = fileIndexMapper.searchByContent(request.getQuery());
            return exactMatches.stream()
                    .map(fileIndex -> createSearchResult(fileIndex, request.getQuery(), 1.0))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("精确匹配搜索失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 模糊匹配搜索
     */
    private List<SearchResult> performFuzzyMatchSearch(SearchRequest request, List<SearchResult> existingResults) {
        try {
            Set<Long> existingIds = existingResults.stream()
                    .map(SearchResult::getFileId)
                    .collect(Collectors.toSet());
            
            List<FileIndex> allFiles = fileIndexMapper.selectAllWithoutVectorData();
            
            return allFiles.stream()
                    .filter(file -> !existingIds.contains(file.getId()))
                    .map(file -> {
                        double score = calculateOptimizedSimilarity(request.getQuery(), file.getContent());
                        return new Object[]{file, score};
                    })
                    .filter(obj -> (Double) obj[1] > 0.3) // 提高阈值
                    .sorted((a, b) -> Double.compare((Double) b[1], (Double) a[1]))
                    .limit(10)
                    .map(obj -> {
                        FileIndex file = (FileIndex) obj[0];
                        double score = (Double) obj[1];
                        return createSearchResult(file, request.getQuery(), score);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("模糊匹配搜索失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 语义搜索
     */
    private List<SearchResult> performSemanticSearch(SearchRequest request, List<SearchResult> existingResults) {
        try {
            Set<Long> existingIds = existingResults.stream()
                    .map(SearchResult::getFileId)
                    .collect(Collectors.toSet());
            
            List<FileIndex> allFiles = fileIndexMapper.selectAllWithoutVectorData();
            
            return allFiles.stream()
                    .filter(file -> !existingIds.contains(file.getId()))
                    .map(file -> {
                        double score = calculateSemanticSimilarity(request.getQuery(), file.getContent());
                        return new Object[]{file, score};
                    })
                    .filter(obj -> (Double) obj[1] > 0.1)
                    .sorted((a, b) -> Double.compare((Double) b[1], (Double) a[1]))
                    .limit(5)
                    .map(obj -> {
                        FileIndex file = (FileIndex) obj[0];
                        double score = (Double) obj[1];
                        return createSearchResult(file, request.getQuery(), score);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("语义搜索失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 优化的相似度计算
     */
    private double calculateOptimizedSimilarity(String query, String content) {
        if (content == null || content.isEmpty()) {
            return 0.0;
        }
        
        String queryLower = query.toLowerCase();
        String contentLower = content.toLowerCase();
        
        // 1. 完全匹配
        if (contentLower.contains(queryLower)) {
            return 1.0;
        }
        
        // 2. 词汇匹配
        String[] queryWords = queryLower.split("\\s+");
        int matchCount = 0;
        for (String word : queryWords) {
            if (contentLower.contains(word)) {
                matchCount++;
            }
        }
        double wordMatchRatio = queryWords.length > 0 ? (double) matchCount / queryWords.length : 0.0;
        
        // 3. 字符匹配
        int charMatchCount = 0;
        for (char c : queryLower.toCharArray()) {
            if (contentLower.indexOf(c) >= 0) {
                charMatchCount++;
            }
        }
        double charMatchRatio = (double) charMatchCount / query.length();
        
        // 4. 部分匹配
        double partialMatch = 0.0;
        for (String word : queryWords) {
            if (word.length() > 2) {
                for (int i = 0; i < word.length() - 1; i++) {
                    String substring = word.substring(i, i + 2);
                    if (contentLower.contains(substring)) {
                        partialMatch += 0.1;
                    }
                }
            }
        }
        
        return Math.min(wordMatchRatio * 0.5 + charMatchRatio * 0.3 + partialMatch * 0.2, 1.0);
    }

    /**
     * 语义相似度计算
     */
    private double calculateSemanticSimilarity(String query, String content) {
        if (content == null || content.isEmpty()) {
            return 0.0;
        }
        
        String queryLower = query.toLowerCase();
        String contentLower = content.toLowerCase();
        
        // 语义相关词匹配
        double semanticScore = 0.0;
        
        // 财务相关词汇
        if (queryLower.contains("利润") || queryLower.contains("收益") || queryLower.contains("收入")) {
            String[] profitWords = {"利润", "收益", "收入", "盈利", "营收", "财务", "成本", "支出"};
            for (String word : profitWords) {
                if (contentLower.contains(word)) {
                    semanticScore += 0.2;
                }
            }
        }
        
        // 分析相关词汇
        if (queryLower.contains("分析") || queryLower.contains("报告") || queryLower.contains("情况")) {
            String[] analysisWords = {"分析", "报告", "情况", "总结", "概述", "说明", "描述", "数据"};
            for (String word : analysisWords) {
                if (contentLower.contains(word)) {
                    semanticScore += 0.15;
                }
            }
        }
        
        // 技术相关词汇
        if (queryLower.contains("技术") || queryLower.contains("研发") || queryLower.contains("创新")) {
            String[] techWords = {"技术", "研发", "创新", "专利", "发明", "工艺", "制造", "生产"};
            for (String word : techWords) {
                if (contentLower.contains(word)) {
                    semanticScore += 0.15;
                }
            }
        }
        
        return Math.min(semanticScore, 1.0);
    }

    /**
     * 创建搜索结果
     */
    private SearchResult createSearchResult(FileIndex fileIndex, String query, double score) {
        SearchResult result = new SearchResult();
        result.setFileId(fileIndex.getId());
        result.setFilePath(fileIndex.getFilePath());
        result.setFileName(fileIndex.getFileName());
        result.setFileType(fileIndex.getFileType());
        result.setFileSize(fileIndex.getFileSize());
        result.setLastModified(fileIndex.getLastModified());
        result.setContent(fileIndex.getContent());
        result.setSummary(fileIndex.getSummary());
        result.setScore(score);
        result.setHighlight(generateHighlight(fileIndex.getContent(), query));
        return result;
    }

    /**
     * 生成高亮显示
     */
    private String generateHighlight(String content, String query) {
        if (content == null || query == null) {
            return content;
        }
        
        String queryLower = query.toLowerCase();
        String contentLower = content.toLowerCase();
        
        if (contentLower.contains(queryLower)) {
            return content.replaceAll("(?i)" + query, "<mark>" + query + "</mark>");
        }
        
        return content;
    }

    /**
     * 去重和排序
     */
    private List<SearchResult> deduplicateAndSort(List<SearchResult> results, String query) {
        // 按文件ID去重
        Map<Long, SearchResult> uniqueResults = new LinkedHashMap<>();
        for (SearchResult result : results) {
            uniqueResults.put(result.getFileId(), result);
        }
        
        // 按分数排序
        return uniqueResults.values().stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
    }

    /**
     * 初始化索引缓存
     */
    private void initializeIndexCache() {
        if (!indexCacheInitialized) {
            synchronized (this) {
                if (!indexCacheInitialized) {
                    try {
                        List<FileIndex> allFiles = fileIndexMapper.selectAllWithoutVectorData();
                        for (FileIndex file : allFiles) {
                            fileIndexCache.put(file.getFilePath(), file);
                        }
                        indexCacheInitialized = true;
                        logger.info("索引缓存初始化完成，缓存 {} 个文件", allFiles.size());
                    } catch (Exception e) {
                        logger.error("索引缓存初始化失败", e);
                    }
                }
            }
        }
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(SearchRequest request) {
        return request.getQuery() + "_" + request.getPageSize();
    }

    /**
     * 检查缓存是否有效
     */
    private boolean isCacheValid(String cacheKey) {
        Long timestamp = cacheTimestamps.get(cacheKey);
        if (timestamp == null) {
            return false;
        }
        return System.currentTimeMillis() - timestamp < CACHE_EXPIRE_TIME;
    }

    /**
     * 清理过期缓存
     */
    public void clearExpiredCache() {
        long currentTime = System.currentTimeMillis();
        cacheTimestamps.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > CACHE_EXPIRE_TIME);
        
        searchCache.keySet().removeIf(key -> !cacheTimestamps.containsKey(key));
        
        logger.info("清理过期缓存完成");
    }

    /**
     * 获取缓存统计
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", searchCache.size());
        stats.put("indexCacheSize", fileIndexCache.size());
        stats.put("indexCacheInitialized", indexCacheInitialized);
        return stats;
    }
}
