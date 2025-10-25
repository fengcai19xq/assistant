package com.assistant.common.exception;

/**
 * 业务异常类
 * 
 * @author assistant
 * @since 1.0.0
 */
public class BusinessException extends BaseException {
    
    private static final long serialVersionUID = 1L;
    
    public BusinessException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
    
    public BusinessException(String errorCode, String errorMessage, String errorDetail) {
        super(errorCode, errorMessage, errorDetail);
    }
    
    public BusinessException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }
    
    public BusinessException(String errorCode, String errorMessage, String errorDetail, Throwable cause) {
        super(errorCode, errorMessage, errorDetail, cause);
    }
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }
    
    public BusinessException(ErrorCode errorCode, String errorDetail) {
        super(errorCode.getCode(), errorCode.getMessage(), errorDetail);
    }
    
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getCode(), errorCode.getMessage(), cause);
    }
    
    public BusinessException(ErrorCode errorCode, String errorDetail, Throwable cause) {
        super(errorCode.getCode(), errorCode.getMessage(), errorDetail, cause);
    }
    
    public BusinessException(ErrorCode errorCode, String errorDetail, String errorMessage) {
        super(errorCode.getCode(), errorMessage, errorDetail);
    }
}
