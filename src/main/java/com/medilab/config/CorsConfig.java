package com.medilab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply CORS to all endpoints
                .allowedOriginPatterns("*") // Keep permissive for dev; recommend configuring specific origins in
                                            // production
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Allowed HTTP methods
                .allowedHeaders("Authorization", "Content-Type", "Accept", "Origin") // Allow common headers
                .exposedHeaders("X-Total-Count") // Expose pagination header
                .allowCredentials(false) // safer default: do not allow credentials with wildcard origins
                .maxAge(3600);
    }
}
