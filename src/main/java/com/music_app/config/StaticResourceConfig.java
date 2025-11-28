package com.music_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.media.base-path}")
    private String mediaBasePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // serve /media/** from the file system folder mediaBasePath
        String fsPath = "file:" + mediaBasePath + "/";
        registry.addResourceHandler("/media/**")
                .addResourceLocations(fsPath);
    }
}
