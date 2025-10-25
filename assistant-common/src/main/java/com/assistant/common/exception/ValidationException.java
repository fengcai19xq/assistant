package com.assistant.common.exception;

import java.util.List;
import java.util.ArrayList;

/**
 * 参数验证异常类
 * 
 * @author assistant
 * @since 1.0.0
 */
public class ValidationException extends BaseException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 验证错误列表
     */
    private final List<ValidationError> validationErrors;
    
    public ValidationException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
        this.validationErrors = new ArrayList<>();
    }
    
    public ValidationException(String errorCode, String errorMessage, List<ValidationError> validationErrors) {
        super(errorCode, errorMessage);
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }
    
    public ValidationException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.validationErrors = new ArrayList<>();
    }
    
    public ValidationException(ErrorCode errorCode, List<ValidationError> validationErrors) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }
    
    public ValidationException(ErrorCode errorCode, String errorDetail) {
        super(errorCode.getCode(), errorCode.getMessage(), errorDetail);
        this.validationErrors = new ArrayList<>();
    }
    
    public ValidationException(ErrorCode errorCode, String errorDetail, List<ValidationError> validationErrors) {
        super(errorCode.getCode(), errorCode.getMessage(), errorDetail);
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }
    
    public ValidationException(ErrorCode errorCode, String errorDetail, String errorMessage) {
        super(errorCode.getCode(), errorMessage, errorDetail);
        this.validationErrors = new ArrayList<>();
    }
    
    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
    
    public void addValidationError(String field, String message) {
        this.validationErrors.add(new ValidationError(field, message));
    }
    
    public void addValidationError(ValidationError validationError) {
        this.validationErrors.add(validationError);
    }
    
    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }
    
    /**
     * 验证错误详情
     */
    public static class ValidationError {
        private final String field;
        private final String message;
        private final Object rejectedValue;
        
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
            this.rejectedValue = null;
        }
        
        public ValidationError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }
        
        public String getField() {
            return field;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Object getRejectedValue() {
            return rejectedValue;
        }
        
        @Override
        public String toString() {
            return String.format("ValidationError{field='%s', message='%s', rejectedValue=%s}", 
                    field, message, rejectedValue);
        }
    }
}
