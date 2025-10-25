package com.assistant.common.util;

import com.assistant.common.exception.ValidationException;
import com.assistant.common.exception.ErrorCode;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 参数验证工具类
 * 
 * @author assistant
 * @since 1.0.0
 */
public class ValidationUtils {
    
    // 常用正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?$");
    private static final Pattern FILE_PATH_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-/\\.\\\\:]+$");
    
    /**
     * 验证字符串不为空
     */
    public static void notBlank(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("参数 '%s' 不能为空", fieldName));
        }
    }
    
    /**
     * 验证字符串不为空
     */
    public static void notBlank(String value, String fieldName, String message) {
        if (!StringUtils.hasText(value)) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("%s: %s", fieldName, message));
        }
    }
    
    /**
     * 验证对象不为空
     */
    public static void notNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("参数 '%s' 不能为空", fieldName));
        }
    }
    
    /**
     * 验证对象不为空
     */
    public static void notNull(Object value, String fieldName, String message) {
        if (value == null) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("%s: %s", fieldName, message));
        }
    }
    
    /**
     * 验证字符串长度
     */
    public static void length(String value, int minLength, int maxLength, String fieldName) {
        if (value == null) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("参数 '%s' 不能为空", fieldName));
        }
        if (value.length() < minLength || value.length() > maxLength) {
            throw new ValidationException(ErrorCode.PARAM_RANGE_ERROR, 
                    String.format("参数 '%s' 长度必须在 %d-%d 之间，当前长度: %d", 
                            fieldName, minLength, maxLength, value.length()));
        }
    }
    
    /**
     * 验证数值范围
     */
    public static void range(Number value, Number min, Number max, String fieldName) {
        if (value == null) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("参数 '%s' 不能为空", fieldName));
        }
        double val = value.doubleValue();
        double minVal = min.doubleValue();
        double maxVal = max.doubleValue();
        if (val < minVal || val > maxVal) {
            throw new ValidationException(ErrorCode.PARAM_RANGE_ERROR, 
                    String.format("参数 '%s' 值必须在 %s-%s 之间，当前值: %s", 
                            fieldName, min, max, value));
        }
    }
    
    /**
     * 验证数值最小值
     */
    public static void min(Number value, Number min, String fieldName) {
        if (value == null) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("参数 '%s' 不能为空", fieldName));
        }
        if (value.doubleValue() < min.doubleValue()) {
            throw new ValidationException(ErrorCode.PARAM_RANGE_ERROR, 
                    String.format("参数 '%s' 值不能小于 %s，当前值: %s", 
                            fieldName, min, value));
        }
    }
    
    /**
     * 验证数值最大值
     */
    public static void max(Number value, Number max, String fieldName) {
        if (value == null) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("参数 '%s' 不能为空", fieldName));
        }
        if (value.doubleValue() > max.doubleValue()) {
            throw new ValidationException(ErrorCode.PARAM_RANGE_ERROR, 
                    String.format("参数 '%s' 值不能大于 %s，当前值: %s", 
                            fieldName, max, value));
        }
    }
    
    /**
     * 验证邮箱格式
     */
    public static void email(String value, String fieldName) {
        if (value == null) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("参数 '%s' 不能为空", fieldName));
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new ValidationException(ErrorCode.PARAM_INVALID, 
                    String.format("参数 '%s' 邮箱格式不正确: %s", fieldName, value));
        }
    }
    
    /**
     * 验证手机号格式
     */
    public static void phone(String value, String fieldName) {
        if (value == null) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("参数 '%s' 不能为空", fieldName));
        }
        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new ValidationException(ErrorCode.PARAM_INVALID, 
                    String.format("参数 '%s' 手机号格式不正确: %s", fieldName, value));
        }
    }
    
    /**
     * 验证URL格式
     */
    public static void url(String value, String fieldName) {
        if (value == null) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("参数 '%s' 不能为空", fieldName));
        }
        if (!URL_PATTERN.matcher(value).matches()) {
            throw new ValidationException(ErrorCode.PARAM_INVALID, 
                    String.format("参数 '%s' URL格式不正确: %s", fieldName, value));
        }
    }
    
    /**
     * 验证文件路径格式
     */
    public static void filePath(String value, String fieldName) {
        if (value == null) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("参数 '%s' 不能为空", fieldName));
        }
        if (!FILE_PATH_PATTERN.matcher(value).matches()) {
            throw new ValidationException(ErrorCode.PARAM_INVALID, 
                    String.format("参数 '%s' 文件路径格式不正确: %s", fieldName, value));
        }
    }
    
    /**
     * 验证正则表达式
     */
    public static void pattern(String value, Pattern pattern, String fieldName, String message) {
        if (value == null) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("参数 '%s' 不能为空", fieldName));
        }
        if (!pattern.matcher(value).matches()) {
            throw new ValidationException(ErrorCode.PARAM_INVALID, 
                    String.format("参数 '%s' %s: %s", fieldName, message, value));
        }
    }
    
    /**
     * 验证集合不为空
     */
    public static void notEmpty(java.util.Collection<?> value, String fieldName) {
        if (value == null) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("参数 '%s' 不能为空", fieldName));
        }
        if (value.isEmpty()) {
            throw new ValidationException(ErrorCode.PARAM_INVALID, 
                    String.format("参数 '%s' 不能为空集合", fieldName));
        }
    }
    
    /**
     * 验证数组不为空
     */
    public static void notEmpty(Object[] value, String fieldName) {
        if (value == null) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("参数 '%s' 不能为空", fieldName));
        }
        if (value.length == 0) {
            throw new ValidationException(ErrorCode.PARAM_INVALID, 
                    String.format("参数 '%s' 不能为空数组", fieldName));
        }
    }
    
    /**
     * 验证集合大小
     */
    public static void size(java.util.Collection<?> value, int minSize, int maxSize, String fieldName) {
        if (value == null) {
            throw new ValidationException(ErrorCode.PARAM_MISSING, 
                    String.format("参数 '%s' 不能为空", fieldName));
        }
        int size = value.size();
        if (size < minSize || size > maxSize) {
            throw new ValidationException(ErrorCode.PARAM_RANGE_ERROR, 
                    String.format("参数 '%s' 集合大小必须在 %d-%d 之间，当前大小: %d", 
                            fieldName, minSize, maxSize, size));
        }
    }
    
    /**
     * 验证条件
     */
    public static void condition(boolean condition, String fieldName, String message) {
        if (!condition) {
            throw new ValidationException(ErrorCode.PARAM_INVALID, 
                    String.format("参数 '%s' 验证失败: %s", fieldName, message));
        }
    }
    
    /**
     * 批量验证
     */
    public static void validateAll(ValidationRule... rules) {
        List<ValidationException.ValidationError> errors = new ArrayList<>();
        
        for (ValidationRule rule : rules) {
            try {
                rule.validate();
            } catch (ValidationException e) {
                errors.addAll(e.getValidationErrors());
            }
        }
        
        if (!errors.isEmpty()) {
            ValidationException ex = new ValidationException(ErrorCode.PARAM_INVALID, "参数验证失败");
            for (ValidationException.ValidationError error : errors) {
                ex.addValidationError(error);
            }
            throw ex;
        }
    }
    
    /**
     * 验证规则接口
     */
    @FunctionalInterface
    public interface ValidationRule {
        void validate();
    }
    
    /**
     * 创建验证规则
     */
    public static ValidationRule rule(String fieldName, String message, Runnable validator) {
        return () -> {
            try {
                validator.run();
            } catch (Exception e) {
                throw new ValidationException(ErrorCode.PARAM_INVALID, 
                        String.format("参数 '%s' 验证失败: %s", fieldName, message));
            }
        };
    }
}
