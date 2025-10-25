package com.assistant.common.exception;

/**
 * 基础异常类
 * 
 * @author assistant
 * @since 1.0.0
 */
public class BaseException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private final String errorCode;
    
    /**
     * 错误信息
     */
    private final String errorMessage;
    
    /**
     * 错误详情
     */
    private final String errorDetail;
    
    public BaseException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDetail = null;
    }
    
    public BaseException(String errorCode, String errorMessage, String errorDetail) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDetail = errorDetail;
    }
    
    public BaseException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDetail = null;
    }
    
    public BaseException(String errorCode, String errorMessage, String errorDetail, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDetail = errorDetail;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public String getErrorDetail() {
        return errorDetail;
    }
    
    @Override
    public String toString() {
        return String.format("BaseException{errorCode='%s', errorMessage='%s', errorDetail='%s'}", 
                errorCode, errorMessage, errorDetail);
    }
}
