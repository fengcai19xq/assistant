package com.assistant.core.entity;

import com.baomidou.mybatisplus.annotation.*;

/**
 * 监控文件夹实体
 */
@TableName("watch_folders")
public class WatchFolder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("path")
    private String path;
    
    @TableField("recursive")
    private Boolean recursive;
    
    @TableField("enabled")
    private Boolean enabled;
    
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private String createdTime;
    
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private String updatedTime;
    
    public WatchFolder() {}
    
    public WatchFolder(String path, Boolean recursive, Boolean enabled) {
        this.path = path;
        this.recursive = recursive;
        this.enabled = enabled;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public Boolean getRecursive() {
        return recursive;
    }
    
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
    
    public String getUpdatedTime() {
        return updatedTime;
    }
    
    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }
}
