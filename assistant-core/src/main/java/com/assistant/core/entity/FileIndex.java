package com.assistant.core.entity;

import com.baomidou.mybatisplus.annotation.*;

/**
 * 文件索引实体
 */
@TableName("file_index")
public class FileIndex {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("file_path")
    private String filePath;
    
    @TableField("file_name")
    private String fileName;
    
    @TableField("file_size")
    private Long fileSize;
    
    @TableField("file_type")
    private String fileType;
    
    @TableField("last_modified")
    private String lastModified;
    
    @TableField(value = "indexed_time", fill = FieldFill.INSERT)
    private String indexedTime;
    
    @TableField("folder_id")
    private Long folderId;
    
    @TableField("content")
    private String content;
    
    @TableField("summary")
    private String summary;
    
    @TableField("vector_data")
    private byte[] vectorData;
    
    public FileIndex() {}
    
    public FileIndex(String filePath, String fileName, Long fileSize, String fileType) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public String getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
    
    public String getIndexedTime() {
        return indexedTime;
    }
    
    public void setIndexedTime(String indexedTime) {
        this.indexedTime = indexedTime;
    }
    
    public Long getFolderId() {
        return folderId;
    }
    
    public void setFolderId(Long folderId) {
        this.folderId = folderId;
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
    
    public byte[] getVectorData() {
        return vectorData;
    }

    public void setVectorData(byte[] vectorData) {
        this.vectorData = vectorData;
    }
}
