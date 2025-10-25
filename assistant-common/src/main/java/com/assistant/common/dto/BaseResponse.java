package com.assistant.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一响应结果
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private Integer code;
    
    public BaseResponse() {}
    
    public BaseResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public BaseResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(true, "操作成功");
    }
    
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, "操作成功", data);
    }
    
    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, message, data);
    }
    
    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(false, message);
    }
    
    public static <T> BaseResponse<T> error(String message, Integer code) {
        BaseResponse<T> response = new BaseResponse<>(false, message);
        response.setCode(code);
        return response;
    }
    
    public static <T> BaseResponse<T> fail(T data) {
        return new BaseResponse<>(false, "操作失败", data);
    }
    
    public static <T> BaseResponse<T> fail(String errorCode, String errorMessage) {
        BaseResponse<T> response = new BaseResponse<>(false, errorMessage);
        response.setCode(Integer.parseInt(errorCode));
        return response;
    }
    
    public static <T> BaseResponse<T> fail(String errorCode, String errorMessage, T data) {
        BaseResponse<T> response = new BaseResponse<>(false, errorMessage, data);
        response.setCode(Integer.parseInt(errorCode));
        return response;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
}
