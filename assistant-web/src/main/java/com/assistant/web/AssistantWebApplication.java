package com.assistant.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

/**
 * 助手Web应用启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.assistant"})
@MapperScan("com.assistant.core.mapper")
public class AssistantWebApplication {
    
    public static void main(String[] args) {
        // 设置默认profile
        if (args.length == 0 || !args[0].contains("spring.profiles.active")) {
            System.setProperty("spring.profiles.active", "v1");
        }
        SpringApplication.run(AssistantWebApplication.class, args);
    }
}
