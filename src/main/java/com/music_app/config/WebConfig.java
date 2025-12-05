package com.music_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                    "https://*.vercel.app",      // Keeps working for Vercel preview links
                    "http://localhost:*",        // Keeps working for local testing
                    "https://astronote.live",    // <--- ADD THIS (Your new domain)
                    "https://www.astronote.live" // <--- ADD THIS (With www)
                ) 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
