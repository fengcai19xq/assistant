package com.assistant.common.exception;

/**
 * 验证错误详情
 * 
 * @author assistant
 * @since 1.0.0
 */
public class ValidationError {
    
    /**
     * 字段名
     */
    private String fieldName;
    
    /**
     * 字段值
     */
    private Object fieldValue;
    
    /**
     * 错误信息
     */
    private String message;
    
    /**
     * 错误码
     */
    private String errorCode;
    
    public ValidationError() {
    }
    
    public ValidationError(String fieldName, Object fieldValue, String message) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.message = message;
    }
    
    public ValidationError(String fieldName, Object fieldValue, String message, String errorCode) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.message = message;
        this.errorCode = errorCode;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public Object getFieldValue() {
        return fieldValue;
    }
    
    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    @Override
    public String toString() {
        return String.format("ValidationError{fieldName='%s', fieldValue=%s, message='%s', errorCode='%s'}", 
                fieldName, fieldValue, message, errorCode);
    }
}
