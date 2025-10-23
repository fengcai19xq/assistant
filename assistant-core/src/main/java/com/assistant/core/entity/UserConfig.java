package com.assistant.core.entity;

import com.baomidou.mybatisplus.annotation.*;

/**
 * 用户配置实体
 */
@TableName("user_config")
public class UserConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("config_key")
    private String configKey;
    
    @TableField("config_value")
    private String configValue;
    
    @TableField("config_type")
    private String configType;
    
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private String createdTime;
    
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private String updatedTime;
    
    public UserConfig() {}
    
    public UserConfig(String configKey, String configValue, String configType) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.configType = configType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getConfigKey() {
        return configKey;
    }
    
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }
    
    public String getConfigValue() {
        return configValue;
    }
    
    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
    
    public String getConfigType() {
        return configType;
    }
    
    public void setConfigType(String configType) {
        this.configType = configType;
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
