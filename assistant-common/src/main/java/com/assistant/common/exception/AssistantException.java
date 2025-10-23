package com.assistant.common.exception;

/**
 * 助手应用异常
 */
public class AssistantException extends RuntimeException {
    
    private Integer code;
    
    public AssistantException(String message) {
        super(message);
    }
    
    public AssistantException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AssistantException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    public AssistantException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
}
