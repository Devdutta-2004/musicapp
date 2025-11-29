// package com.music_app.config;

// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.CorsRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration
// public class WebConfig implements WebMvcConfigurer {

//     @Override
//     public void addCorsMappings(CorsRegistry registry) {
//         registry.addMapping("/**")
//                 .allowedOrigins("http://localhost:3000", "https://your-frontend-domain.vercel.app")
//                 .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                 .allowedHeaders("*")
//                 .allowCredentials(true);
//     }
// }
package com.music_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // using allowedOriginPatterns is more powerful than allowedOrigins
                .allowedOriginPatterns("https://*.vercel.app", "http://localhost:*") 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
