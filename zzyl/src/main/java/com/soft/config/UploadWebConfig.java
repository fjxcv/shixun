package com.soft.config;

import com.soft.controller.FileController;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = FileController.uploadDir().toAbsolutePath().normalize()
                .toUri().toString();
        if (!uploadDir.endsWith("/")) {
            uploadDir = uploadDir + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadDir);
    }
}
