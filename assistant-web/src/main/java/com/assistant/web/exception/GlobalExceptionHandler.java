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
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
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
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("业务异常: {} - {}", ex.getErrorCode(), ex.getErrorMessage(), ex);
        
        ErrorResponse errorResponse = createErrorResponse(ex, request, requestId);
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理系统异常
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleSystemException(
            SystemException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.error("系统异常: {} - {}", ex.getErrorCode(), ex.getErrorMessage(), ex);
        
        ErrorResponse errorResponse = createErrorResponse(ex, request, requestId);
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("参数验证异常: {} - {}", ex.getErrorCode(), ex.getErrorMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.fromValidationException(ex, 
                request.getRequestURI(), request.getMethod());
        errorResponse.setRequestId(requestId);
        
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理Spring参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("参数验证异常: {}", ex.getMessage());
        
        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.add(new ErrorResponse.ValidationError(
                    fieldError.getField(), 
                    fieldError.getDefaultMessage(), 
                    fieldError.getRejectedValue()));
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.PARAM_INVALID.getCode(),
                "参数验证失败",
                "请求参数不符合要求",
                request.getRequestURI(),
                request.getMethod());
        errorResponse.setRequestId(requestId);
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
        
        String requestId = generateRequestId();
        logger.warn("绑定异常: {}", ex.getMessage());
        
        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.add(new ErrorResponse.ValidationError(
                    fieldError.getField(), 
                    fieldError.getDefaultMessage(), 
                    fieldError.getRejectedValue()));
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.PARAM_INVALID.getCode(),
                "参数绑定失败",
                "请求参数绑定失败",
                request.getRequestURI(),
                request.getMethod());
        errorResponse.setRequestId(requestId);
        errorResponse.setValidationErrors(validationErrors);
        
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理方法参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("参数类型不匹配异常: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.PARAM_TYPE_ERROR.getCode(),
                "参数类型错误",
                String.format("参数 '%s' 类型不匹配，期望类型: %s", 
                        ex.getName(), ex.getRequiredType().getSimpleName()),
                request.getRequestURI(),
                request.getMethod());
        errorResponse.setRequestId(requestId);
        
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("404异常: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                "资源不存在",
                String.format("请求的资源 '%s' 不存在", ex.getRequestURL()),
                request.getRequestURI(),
                request.getMethod());
        errorResponse.setRequestId(requestId);
        
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<ErrorResponse>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.error("未处理的异常: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.SYSTEM_ERROR.getCode(),
                "系统内部错误",
                "系统发生未知错误，请联系管理员",
                request.getRequestURI(),
                request.getMethod());
        errorResponse.setRequestId(requestId);
        
        BaseResponse<ErrorResponse> response = BaseResponse.fail(errorResponse);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 创建错误响应
     */
    private ErrorResponse createErrorResponse(BaseException ex, HttpServletRequest request, String requestId) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getErrorCode(),
                ex.getErrorMessage(),
                ex.getErrorDetail(),
                request.getRequestURI(),
                request.getMethod());
        errorResponse.setRequestId(requestId);
        return errorResponse;
    }
    
    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
