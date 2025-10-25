package com.assistant.common.dto;

import com.assistant.common.exception.ValidationException;
import com.assistant.common.exception.ValidationException.ValidationError;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 错误响应DTO
 * 
 * @author assistant
 * @since 1.0.0
 */
public class ErrorResponse {
    
    /**
     * 错误码
     */
    private String errorCode;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 错误详情
     */
    private String errorDetail;
    
    /**
     * 请求路径
     */
    private String requestPath;
    
    /**
     * 请求方法
     */
    private String requestMethod;
    
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 请求ID（用于追踪）
     */
    private String requestId;
    
    /**
     * 验证错误列表
     */
    private List<ValidationError> validationErrors;
    
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.validationErrors = new ArrayList<>();
    }
    
    public ErrorResponse(String errorCode, String errorMessage) {
        this();
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public ErrorResponse(String errorCode, String errorMessage, String errorDetail) {
        this(errorCode, errorMessage);
        this.errorDetail = errorDetail;
    }
    
    public ErrorResponse(String errorCode, String errorMessage, String errorDetail, String requestPath, String requestMethod) {
        this(errorCode, errorMessage, errorDetail);
        this.requestPath = requestPath;
        this.requestMethod = requestMethod;
    }
    
    public static ErrorResponse fromValidationException(ValidationException ex, String requestPath, String requestMethod) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode(), ex.getErrorMessage(), ex.getErrorDetail(), requestPath, requestMethod);
        // Convert ValidationException.ValidationError to ErrorResponse.ValidationError
        List<ValidationError> convertedErrors = new ArrayList<>();
        for (com.assistant.common.exception.ValidationException.ValidationError exError : ex.getValidationErrors()) {
            convertedErrors.add(new ValidationError(exError.getField(), exError.getMessage()));
        }
        response.setValidationErrors(convertedErrors);
        return response;
    }
    
    // Getters and Setters
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getErrorDetail() {
        return errorDetail;
    }
    
    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }
    
    public String getRequestPath() {
        return requestPath;
    }
    
    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }
    
    public String getRequestMethod() {
        return requestMethod;
    }
    
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
    
    public void setValidationErrors(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }
    
    /**
     * 验证错误详情
     */
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
        
        public ValidationError() {}
        
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        
        public ValidationError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }
        
        public String getField() {
            return field;
        }
        
        public void setField(String field) {
            this.field = field;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public Object getRejectedValue() {
            return rejectedValue;
        }
        
        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }
    }
    
    @Override
    public String toString() {
        return String.format("ErrorResponse{errorCode='%s', errorMessage='%s', errorDetail='%s', requestPath='%s', requestMethod='%s', timestamp=%s, requestId='%s'}", 
                errorCode, errorMessage, errorDetail, requestPath, requestMethod, timestamp, requestId);
    }
}
