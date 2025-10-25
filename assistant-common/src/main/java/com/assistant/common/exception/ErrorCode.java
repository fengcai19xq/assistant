package com.assistant.common.exception;

/**
 * 错误码枚举
 * 
 * @author assistant
 * @since 1.0.0
 */
public enum ErrorCode {
    
    // 系统级错误 (1000-1999)
    SYSTEM_ERROR("1000", "系统内部错误"),
    SYSTEM_BUSY("1001", "系统繁忙，请稍后重试"),
    SYSTEM_TIMEOUT("1002", "系统超时"),
    SYSTEM_MAINTENANCE("1003", "系统维护中"),
    
    // 参数错误 (2000-2999)
    PARAM_ERROR("2000", "参数错误"),
    PARAM_MISSING("2001", "缺少必要参数"),
    PARAM_INVALID("2002", "参数格式错误"),
    PARAM_TYPE_ERROR("2003", "参数类型错误"),
    PARAM_RANGE_ERROR("2004", "参数值超出范围"),
    
    // 业务错误 (3000-3999)
    BUSINESS_ERROR("3000", "业务处理错误"),
    FILE_NOT_FOUND("3001", "文件不存在"),
    FILE_ACCESS_DENIED("3002", "文件访问被拒绝"),
    FILE_SIZE_EXCEEDED("3003", "文件大小超出限制"),
    FILE_TYPE_NOT_SUPPORTED("3004", "文件类型不支持"),
    FOLDER_NOT_FOUND("3005", "文件夹不存在"),
    FOLDER_ACCESS_DENIED("3006", "文件夹访问被拒绝"),
    SEARCH_ERROR("3007", "搜索处理错误"),
    INDEX_ERROR("3008", "索引处理错误"),
    AI_MODEL_ERROR("3009", "AI模型处理错误"),
    VECTOR_SEARCH_ERROR("3010", "向量搜索错误"),
    
    // 数据库错误 (4000-4999)
    DATABASE_ERROR("4000", "数据库操作错误"),
    DATABASE_CONNECTION_ERROR("4001", "数据库连接错误"),
    DATABASE_TIMEOUT("4002", "数据库操作超时"),
    DATABASE_CONSTRAINT_ERROR("4003", "数据库约束错误"),
    DATABASE_DEADLOCK("4004", "数据库死锁"),
    
    // 网络错误 (5000-5999)
    NETWORK_ERROR("5000", "网络错误"),
    NETWORK_TIMEOUT("5001", "网络超时"),
    NETWORK_CONNECTION_ERROR("5002", "网络连接错误"),
    API_CALL_ERROR("5003", "API调用错误"),
    API_RATE_LIMIT("5004", "API调用频率超限"),
    
    // 权限错误 (6000-6999)
    PERMISSION_DENIED("6000", "权限不足"),
    AUTHENTICATION_ERROR("6001", "认证失败"),
    AUTHORIZATION_ERROR("6002", "授权失败"),
    TOKEN_EXPIRED("6003", "令牌已过期"),
    TOKEN_INVALID("6004", "令牌无效"),
    
    // 资源错误 (7000-7999)
    RESOURCE_NOT_FOUND("7000", "资源不存在"),
    RESOURCE_ALREADY_EXISTS("7001", "资源已存在"),
    RESOURCE_LOCKED("7002", "资源被锁定"),
    RESOURCE_EXHAUSTED("7003", "资源耗尽"),
    STORAGE_ERROR("7004", "存储错误"),
    STORAGE_FULL("7005", "存储空间不足"),
    
    // 配置错误 (8000-8999)
    CONFIG_ERROR("8000", "配置错误"),
    CONFIG_MISSING("8001", "配置缺失"),
    CONFIG_INVALID("8002", "配置无效"),
    CONFIG_FORMAT_ERROR("8003", "配置格式错误"),
    
    // 外部服务错误 (9000-9999)
    EXTERNAL_SERVICE_ERROR("9000", "外部服务错误"),
    EXTERNAL_SERVICE_UNAVAILABLE("9001", "外部服务不可用"),
    EXTERNAL_SERVICE_TIMEOUT("9002", "外部服务超时"),
    EXTERNAL_SERVICE_AUTH_ERROR("9003", "外部服务认证错误");
    
    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    /**
     * 根据错误码获取错误信息
     */
    public static String getMessageByCode(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode.getMessage();
            }
        }
        return "未知错误";
    }
    
    /**
     * 根据错误码获取ErrorCode枚举
     */
    public static ErrorCode getByCode(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }
}
