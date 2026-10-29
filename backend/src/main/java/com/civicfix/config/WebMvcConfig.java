package com.civicfix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${civicfix.storage.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadResourcePath = uploadPath.toUri().toString();

        if (!uploadResourcePath.endsWith("/")) {
            uploadResourcePath += "/";
        }

        registry.addResourceHandler("/api/v1/uploads/**")
                .addResourceLocations(uploadResourcePath);
    }
}
