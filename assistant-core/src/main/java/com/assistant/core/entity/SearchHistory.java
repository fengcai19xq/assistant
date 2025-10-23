package com.assistant.core.entity;

import com.baomidou.mybatisplus.annotation.*;

/**
 * 搜索历史实体
 */
@TableName("search_history")
public class SearchHistory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("query_text")
    private String queryText;
    
    @TableField("result_count")
    private Integer resultCount;
    
    @TableField(value = "search_time", fill = FieldFill.INSERT)
    private String searchTime;
    
    @TableField("search_type")
    private String searchType;
    
    public SearchHistory() {}
    
    public SearchHistory(String queryText, Integer resultCount, String searchType) {
        this.queryText = queryText;
        this.resultCount = resultCount;
        this.searchType = searchType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getQueryText() {
        return queryText;
    }
    
    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }
    
    public Integer getResultCount() {
        return resultCount;
    }
    
    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }
    
    public String getSearchTime() {
        return searchTime;
    }
    
    public void setSearchTime(String searchTime) {
        this.searchTime = searchTime;
    }
    
    public String getSearchType() {
        return searchType;
    }
    
    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }
}
