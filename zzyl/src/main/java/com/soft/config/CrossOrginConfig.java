package com.soft.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CrossOrginConfig implements WebMvcConfigurer {

    /*解决前端访问的跨域问题*/
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 1. 允许跨域的路径
                // 2. 允许所有来源（生产环境建议指定具体域名）
                .allowedOriginPatterns("http://localhost")
                // 3. 允许的请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*") // 4. 允许的请求头
                .allowCredentials(true) // 5. 是否允许携带Cookie等凭证
                .maxAge(3600); // 6. 预检请求的有效期（秒）
    }
}
