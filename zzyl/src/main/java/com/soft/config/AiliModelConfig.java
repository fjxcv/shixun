package com.soft.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiliModelConfig {
    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Bean
    public DashScopeApi dashScopeApi(){
        //返回阿里大模型访问对象，封装了apiKey
        return DashScopeApi.builder().apiKey(apiKey).build();
    }
}
