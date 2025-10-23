package com.assistant.common.dto;


/**
 * 搜索结果DTO
 */
public class SearchResult {
    
    private Long fileId;
    private String filePath;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String lastModified;
    private String content;
    private String summary;
    private Double score;
    private String highlight;
    private String analysisSummary;
    
    public SearchResult() {}
    
    public SearchResult(FileInfo fileInfo, Double score) {
        this.fileId = fileInfo.getId();
        this.filePath = fileInfo.getFilePath();
        this.fileName = fileInfo.getFileName();
        this.fileType = fileInfo.getFileType();
        this.fileSize = fileInfo.getFileSize();
        this.lastModified = fileInfo.getLastModified();
        this.content = fileInfo.getContent();
        this.summary = fileInfo.getSummary();
        this.score = score;
    }
    
    // Getters and Setters
    public Long getFileId() {
        return fileId;
    }
    
    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
    }
    
    public String getHighlight() {
        return highlight;
    }
    
    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }
    
    public String getAnalysisSummary() {
        return analysisSummary;
    }
    
    public void setAnalysisSummary(String analysisSummary) {
        this.analysisSummary = analysisSummary;
    }
}
