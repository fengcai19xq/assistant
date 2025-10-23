package com.assistant.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 助手Web应用启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.assistant"})
@MapperScan("com.assistant.core.mapper")
public class AssistantWebApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AssistantWebApplication.class, args);
    }
}
