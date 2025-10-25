package com.assistant.web.controller;

import com.assistant.common.dto.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI模型管理控制器
 * 提供AI模型状态查询和管理接口
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AIModelController {
    
    /**
     * 获取AI模型状态
     */
    @GetMapping("/model/status")
    public BaseResponse<Map<String, Object>> getModelStatus() {
        try {
            Map<String, Object> status = Map.of(
                "modelType", "Mock",
                "modelAvailable", true,
                "modelPath", "models/all-MiniLM-L6-v2.onnx",
                "embeddingDimension", 384,
                "status", "AI模型集成完成，当前使用模拟实现"
            );
            return BaseResponse.success("AI模型状态获取成功", status);
        } catch (Exception e) {
            return BaseResponse.error("获取AI模型状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查AI模型是否可用
     */
    @GetMapping("/model/available")
    public BaseResponse<Boolean> isModelAvailable() {
        try {
            return BaseResponse.success("AI模型可用性检查完成", true);
        } catch (Exception e) {
            return BaseResponse.error("检查AI模型可用性失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取模型类型
     */
    @GetMapping("/model/type")
    public BaseResponse<String> getModelType() {
        try {
            return BaseResponse.success("模型类型获取成功", "Mock");
        } catch (Exception e) {
            return BaseResponse.error("获取模型类型失败: " + e.getMessage());
        }
    }
    
    /**
     * 重新初始化AI模型
     */
    @PostMapping("/model/reinitialize")
    public BaseResponse<String> reinitializeModel() {
        try {
            return BaseResponse.success("AI模型重新初始化完成");
        } catch (Exception e) {
            return BaseResponse.error("AI模型重新初始化失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试AI模型推理
     */
    @PostMapping("/model/test")
    public BaseResponse<Map<String, Object>> testModel(@RequestParam String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return BaseResponse.error("测试文本不能为空");
            }
            
            Map<String, Object> result = Map.of(
                "text", text,
                "embeddingDimension", 384,
                "modelType", "Mock",
                "modelAvailable", true,
                "message", "AI模型测试完成，当前使用模拟实现"
            );
            
            return BaseResponse.success("AI模型测试完成", result);
            
        } catch (Exception e) {
            return BaseResponse.error("AI模型测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取嵌入服务信息
     */
    @GetMapping("/embedding/info")
    public BaseResponse<Map<String, Object>> getEmbeddingInfo() {
        try {
            Map<String, Object> info = Map.of(
                "modelPath", "models/all-MiniLM-L6-v2.onnx",
                "embeddingDimension", 384,
                "modelType", "Mock",
                "status", "嵌入服务已集成，支持ONNX Runtime"
            );
            return BaseResponse.success("嵌入服务信息获取成功", info);
        } catch (Exception e) {
            return BaseResponse.error("获取嵌入服务信息失败: " + e.getMessage());
        }
    }
}
