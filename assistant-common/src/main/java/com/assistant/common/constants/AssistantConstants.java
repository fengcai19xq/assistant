package com.assistant.common.constants;

/**
 * 助手应用常量
 */
public class AssistantConstants {
    
    // 应用配置
    public static final String APP_NAME = "File AI Assistant";
    public static final String APP_VERSION = "1.0.0";
    
    // 数据库配置
    public static final String DB_NAME = "assistant.db";
    public static final String DB_PATH = System.getProperty("user.home") + "/.file-assistant/data/";
    
    // 索引配置
    public static final String INDEX_PATH = System.getProperty("user.home") + "/.file-assistant/index/";
    public static final int MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    public static final int BATCH_SIZE = 100;
    
    // AI模型配置
    public static final String MODEL_PATH = System.getProperty("user.home") + "/.file-assistant/models/";
    public static final String EMBEDDING_MODEL_NAME = "all-MiniLM-L6-v2.onnx";
    public static final int EMBEDDING_DIMENSION = 384;
    
    // 搜索配置
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MAX_SEARCH_RESULTS = 1000;
    
    // 文件类型
    public static final String FILE_TYPE_TEXT = "text";
    public static final String FILE_TYPE_DOCUMENT = "document";
    public static final String FILE_TYPE_PDF = "pdf";
    public static final String FILE_TYPE_PRESENTATION = "presentation";
    public static final String FILE_TYPE_SPREADSHEET = "spreadsheet";
    public static final String FILE_TYPE_CODE = "code";
    public static final String FILE_TYPE_WEB = "web";
    public static final String FILE_TYPE_OTHER = "other";
    
    // 错误码
    public static final int ERROR_CODE_FILE_NOT_FOUND = 1001;
    public static final int ERROR_CODE_FILE_TOO_LARGE = 1002;
    public static final int ERROR_CODE_UNSUPPORTED_FORMAT = 1003;
    public static final int ERROR_CODE_INDEX_FAILED = 1004;
    public static final int ERROR_CODE_SEARCH_FAILED = 1005;
    public static final int ERROR_CODE_MODEL_LOAD_FAILED = 1006;
    
    // 消息
    public static final String MSG_INDEX_SUCCESS = "索引创建成功";
    public static final String MSG_INDEX_FAILED = "索引创建失败";
    public static final String MSG_SEARCH_SUCCESS = "搜索完成";
    public static final String MSG_SEARCH_FAILED = "搜索失败";
    public static final String MSG_FILE_NOT_FOUND = "文件不存在";
    public static final String MSG_UNSUPPORTED_FORMAT = "不支持的文件格式";
}
