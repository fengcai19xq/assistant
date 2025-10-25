package com.assistant.web.exception;

import com.assistant.common.dto.BaseResponse;
import com.assistant.common.dto.ErrorResponse;
import com.assistant.common.exception.BaseException;
import com.assistant.common.exception.BusinessException;
import com.assistant.common.exception.SystemException;
import com.assistant.common.exception.ValidationException;
import com.assistant.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 全局异常处理器
 * 
 * @author assistant
 * @since 1.0.0
 */
@RestControllerAdvice
public class ExceptionAdvice {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        logger.warn("业务异常: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = buildErrorResponse(ex, request);
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理系统异常
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleSystemException(
            SystemException ex, HttpServletRequest request) {
        
        logger.error("系统异常: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = buildErrorResponse(ex, request);
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        
        logger.warn("参数验证异常: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = buildErrorResponse(ex, request);
        // Convert ValidationException.ValidationError to ErrorResponse.ValidationError
        List<ErrorResponse.ValidationError> convertedErrors = new ArrayList<>();
        for (com.assistant.common.exception.ValidationException.ValidationError exError : ex.getValidationErrors()) {
            convertedErrors.add(new ErrorResponse.ValidationError(exError.getField(), exError.getMessage()));
        }
        errorResponse.setValidationErrors(convertedErrors);
        
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理Spring参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logger.warn("参数验证异常: {}", ex.getMessage(), ex);
        
        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            ErrorResponse.ValidationError error = new ErrorResponse.ValidationError(
                    fieldError.getField(),
                    fieldError.getDefaultMessage(),
                    fieldError.getRejectedValue()
            );
            validationErrors.add(error);
        }
        
        ErrorResponse errorResponse = buildErrorResponse(
                ErrorCode.PARAM_INVALID.getCode(),
                "参数验证失败",
                request);
        errorResponse.setValidationErrors(validationErrors);
        
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleBindException(
            BindException ex, HttpServletRequest request) {
        
        logger.warn("参数绑定异常: {}", ex.getMessage(), ex);
        
        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            ErrorResponse.ValidationError error = new ErrorResponse.ValidationError(
                    fieldError.getField(),
                    fieldError.getDefaultMessage(),
                    fieldError.getRejectedValue()
            );
            validationErrors.add(error);
        }
        
        ErrorResponse errorResponse = buildErrorResponse(
                ErrorCode.PARAM_INVALID.getCode(),
                "参数绑定失败",
                request);
        errorResponse.setValidationErrors(validationErrors);
        
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        logger.warn("参数类型不匹配异常: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = buildErrorResponse(
                ErrorCode.PARAM_TYPE_ERROR.getCode(),
                String.format("参数 '%s' 类型不匹配，期望类型: %s", 
                        ex.getName(), ex.getRequiredType().getSimpleName()),
                request);
        
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理基础异常
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleBaseException(
            BaseException ex, HttpServletRequest request) {
        
        logger.error("基础异常: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = buildErrorResponse(ex, request);
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        
        logger.error("运行时异常: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = buildErrorResponse(
                ErrorCode.SYSTEM_ERROR.getCode(),
                "系统内部错误",
                request);
        
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleException(
            Exception ex, HttpServletRequest request) {
        
        logger.error("未知异常: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = buildErrorResponse(
                ErrorCode.SYSTEM_ERROR.getCode(),
                "系统内部错误",
                request);
        
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 构建错误响应
     */
    private ErrorResponse buildErrorResponse(BaseException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getErrorCode(), ex.getErrorMessage(), request);
    }
    
    /**
     * 构建错误响应
     */
    private ErrorResponse buildErrorResponse(String errorCode, String errorMessage, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(errorCode);
        errorResponse.setErrorMessage(errorMessage);
        errorResponse.setRequestPath(request.getRequestURI());
        errorResponse.setRequestMethod(request.getMethod());
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setRequestId(UUID.randomUUID().toString());
        return errorResponse;
    }
}
