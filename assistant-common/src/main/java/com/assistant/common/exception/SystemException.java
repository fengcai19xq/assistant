package com.assistant.common.exception;

/**
 * 系统异常类
 * 
 * @author assistant
 * @since 1.0.0
 */
public class SystemException extends BaseException {
    
    private static final long serialVersionUID = 1L;
    
    public SystemException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
    
    public SystemException(String errorCode, String errorMessage, String errorDetail) {
        super(errorCode, errorMessage, errorDetail);
    }
    
    public SystemException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }
    
    public SystemException(String errorCode, String errorMessage, String errorDetail, Throwable cause) {
        super(errorCode, errorMessage, errorDetail, cause);
    }
    
    public SystemException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }
    
    public SystemException(ErrorCode errorCode, String errorDetail) {
        super(errorCode.getCode(), errorCode.getMessage(), errorDetail);
    }
    
    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getCode(), errorCode.getMessage(), cause);
    }
    
    public SystemException(ErrorCode errorCode, String errorDetail, Throwable cause) {
        super(errorCode.getCode(), errorCode.getMessage(), errorDetail, cause);
    }
    
    public SystemException(ErrorCode errorCode, String errorDetail, String errorMessage) {
        super(errorCode.getCode(), errorMessage, errorDetail);
    }
}
