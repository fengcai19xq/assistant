package com.assistant.common.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 搜索请求DTO
 */
public class SearchRequest {
    
    @NotBlank(message = "搜索关键词不能为空")
    private String query;
    
    private String fileType;
    private String folderPath;
    private Integer pageSize = 20;
    private Integer pageNum = 1;
    private Boolean useSemanticSearch = true;
    
    public SearchRequest() {}
    
    public SearchRequest(String query) {
        this.query = query;
    }
    
    // Getters and Setters
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public String getFolderPath() {
        return folderPath;
    }
    
    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    
    public Integer getPageNum() {
        return pageNum;
    }
    
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }
    
    public Boolean getUseSemanticSearch() {
        return useSemanticSearch;
    }
    
    public void setUseSemanticSearch(Boolean useSemanticSearch) {
        this.useSemanticSearch = useSemanticSearch;
    }
}
