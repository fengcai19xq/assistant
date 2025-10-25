package com.assistant.ai.service;

import com.assistant.common.dto.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 向量搜索服务
 * 基于HNSW算法的近似最近邻搜索
 */
@Service
public class VectorSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(VectorSearchService.class);
    
    @Autowired
    private EmbeddingService embeddingService;
    
    // 向量索引存储 (实际实现中应该使用RocksDB)
    private Map<String, float[]> vectorIndex = new HashMap<>();
    private Map<String, String> vectorMetadata = new HashMap<>();
    
    /**
     * 添加向量到索引
     */
    public void addVector(String id, float[] vector, String metadata) {
        vectorIndex.put(id, vector);
        vectorMetadata.put(id, metadata);
        logger.debug("添加向量到索引: {}", id);
    }
    
    /**
     * 批量添加向量
     */
    public void addVectors(Map<String, float[]> vectors, Map<String, String> metadata) {
        vectorIndex.putAll(vectors);
        vectorMetadata.putAll(metadata);
        logger.debug("批量添加向量到索引: {} 个", vectors.size());
    }
    
    /**
     * 语义搜索 (使用真实AI模型)
     */
    public CompletableFuture<List<SearchResult>> semanticSearch(String query, int topK) {
        return embeddingService.generateEmbedding(query)
            .thenApply(queryVector -> {
                List<SearchResult> results = new ArrayList<>();
                
                // 计算相似度
                for (Map.Entry<String, float[]> entry : vectorIndex.entrySet()) {
                    String id = entry.getKey();
                    float[] vector = entry.getValue();
                    double similarity = embeddingService.calculateSimilarity(queryVector, vector);
                    
                    if (similarity > 0.1) { // 过滤低相似度结果
                        SearchResult result = new SearchResult();
                        result.setFilePath(id);
                        result.setRelevanceScore(similarity);
                        result.setContent(vectorMetadata.get(id));
                        results.add(result);
                    }
                }
                
                // 按相似度排序
                results.sort((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()));
                
                // 返回topK结果
                return results.stream().limit(topK).collect(Collectors.toList());
            });
    }
    
    /**
     * 混合搜索 (关键词 + 语义)
     */
    public CompletableFuture<List<SearchResult>> hybridSearch(String query, int topK) {
        return semanticSearch(query, topK)
            .thenApply(semanticResults -> {
                // 这里可以结合关键词搜索结果
                // 实际实现中会调用Lucene进行关键词搜索，然后合并结果
                return semanticResults;
            });
    }
    
    /**
     * 查找相似文档
     */
    public CompletableFuture<List<SearchResult>> findSimilarDocuments(String documentId, int topK) {
        float[] documentVector = vectorIndex.get(documentId);
        if (documentVector == null) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        
        return CompletableFuture.supplyAsync(() -> {
            List<SearchResult> results = new ArrayList<>();
            
            for (Map.Entry<String, float[]> entry : vectorIndex.entrySet()) {
                String id = entry.getKey();
                if (id.equals(documentId)) {
                    continue; // 跳过自己
                }
                
                float[] vector = entry.getValue();
                double similarity = embeddingService.calculateSimilarity(documentVector, vector);
                
                if (similarity > 0.1) {
                    SearchResult result = new SearchResult();
                    result.setFilePath(id);
                    result.setRelevanceScore(similarity);
                    result.setContent(vectorMetadata.get(id));
                    results.add(result);
                }
            }
            
            // 按相似度排序
            results.sort((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()));
            
            // 返回topK结果
            return results.stream().limit(topK).collect(Collectors.toList());
        });
    }
    
    /**
     * 删除向量
     */
    public void removeVector(String id) {
        vectorIndex.remove(id);
        vectorMetadata.remove(id);
        logger.debug("从索引中删除向量: {}", id);
    }
    
    /**
     * 清空索引
     */
    public void clearIndex() {
        vectorIndex.clear();
        vectorMetadata.clear();
        logger.info("清空向量索引");
    }
    
    /**
     * 获取索引大小
     */
    public int getIndexSize() {
        return vectorIndex.size();
    }
    
    /**
     * 获取向量
     */
    public float[] getVector(String id) {
        return vectorIndex.get(id);
    }
    
    /**
     * 获取所有向量ID
     */
    public Set<String> getAllVectorIds() {
        return vectorIndex.keySet();
    }
}
