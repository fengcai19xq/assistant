package com.assistant.common.util;

import com.assistant.common.exception.BusinessException;
import com.assistant.common.exception.SystemException;
import com.assistant.common.exception.ValidationException;
import com.assistant.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

/**
 * 异常工具类
 * 
 * @author assistant
 * @since 1.0.0
 */
public class ExceptionUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);
    
    /**
     * 抛出业务异常
     */
    public static void throwBusinessException(String errorCode, String errorMessage) {
        throw new BusinessException(errorCode, errorMessage);
    }
    
    /**
     * 抛出业务异常
     */
    public static void throwBusinessException(ErrorCode errorCode) {
        throw new BusinessException(errorCode);
    }
    
    /**
     * 抛出业务异常
     */
    public static void throwBusinessException(ErrorCode errorCode, String errorDetail) {
        throw new BusinessException(errorCode, errorDetail);
    }
    
    /**
     * 抛出系统异常
     */
    public static void throwSystemException(String errorCode, String errorMessage) {
        throw new SystemException(errorCode, errorMessage);
    }
    
    /**
     * 抛出系统异常
     */
    public static void throwSystemException(ErrorCode errorCode) {
        throw new SystemException(errorCode);
    }
    
    /**
     * 抛出系统异常
     */
    public static void throwSystemException(ErrorCode errorCode, String errorDetail) {
        throw new SystemException(errorCode, errorDetail);
    }
    
    /**
     * 抛出参数验证异常
     */
    public static void throwValidationException(String errorCode, String errorMessage) {
        throw new ValidationException(errorCode, errorMessage);
    }
    
    /**
     * 抛出参数验证异常
     */
    public static void throwValidationException(ErrorCode errorCode) {
        throw new ValidationException(errorCode);
    }
    
    /**
     * 条件判断抛出业务异常
     */
    public static void throwBusinessExceptionIf(boolean condition, String errorCode, String errorMessage) {
        if (condition) {
            throw new BusinessException(errorCode, errorMessage);
        }
    }
    
    /**
     * 条件判断抛出业务异常
     */
    public static void throwBusinessExceptionIf(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new BusinessException(errorCode);
        }
    }
    
    /**
     * 条件判断抛出业务异常
     */
    public static void throwBusinessExceptionIf(boolean condition, ErrorCode errorCode, String errorDetail) {
        if (condition) {
            throw new BusinessException(errorCode, errorCode.getMessage(), errorDetail);
        }
    }
    
    /**
     * 条件判断抛出系统异常
     */
    public static void throwSystemExceptionIf(boolean condition, String errorCode, String errorMessage) {
        if (condition) {
            throw new SystemException(errorCode, errorMessage);
        }
    }
    
    /**
     * 条件判断抛出系统异常
     */
    public static void throwSystemExceptionIf(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new SystemException(errorCode);
        }
    }
    
    /**
     * 条件判断抛出系统异常
     */
    public static void throwSystemExceptionIf(boolean condition, ErrorCode errorCode, String errorDetail) {
        if (condition) {
            throw new SystemException(errorCode, errorCode.getMessage(), errorDetail);
        }
    }
    
    /**
     * 条件判断抛出参数验证异常
     */
    public static void throwValidationExceptionIf(boolean condition, String errorCode, String errorMessage) {
        if (condition) {
            throw new ValidationException(errorCode, errorMessage);
        }
    }
    
    /**
     * 条件判断抛出参数验证异常
     */
    public static void throwValidationExceptionIf(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new ValidationException(errorCode);
        }
    }
    
    /**
     * 条件判断抛出参数验证异常
     */
    public static void throwValidationExceptionIf(boolean condition, ErrorCode errorCode, String errorDetail) {
        if (condition) {
            throw new ValidationException(errorCode, errorCode.getMessage(), errorDetail);
        }
    }
    
    /**
     * 安全执行，捕获异常并记录日志
     */
    public static <T> T safeExecute(Supplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (Exception e) {
            logger.error("安全执行失败", e);
            return defaultValue;
        }
    }
    
    /**
     * 安全执行，捕获异常并记录日志
     */
    public static void safeExecute(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            logger.error("安全执行失败", e);
        }
    }
    
    /**
     * 获取异常堆栈信息
     */
    public static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * 获取异常根原因
     */
    public static Throwable getRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
    
    /**
     * 判断是否为业务异常
     */
    public static boolean isBusinessException(Throwable throwable) {
        return throwable instanceof BusinessException;
    }
    
    /**
     * 判断是否为系统异常
     */
    public static boolean isSystemException(Throwable throwable) {
        return throwable instanceof SystemException;
    }
    
    /**
     * 判断是否为参数验证异常
     */
    public static boolean isValidationException(Throwable throwable) {
        return throwable instanceof ValidationException;
    }
    
    /**
     * 判断是否为运行时异常
     */
    public static boolean isRuntimeException(Throwable throwable) {
        return throwable instanceof RuntimeException;
    }
    
    /**
     * 判断是否为检查异常
     */
    public static boolean isCheckedException(Throwable throwable) {
        return !(throwable instanceof RuntimeException) && !(throwable instanceof Error);
    }
}
