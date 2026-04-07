package com.wft.guesscolleague.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "http://localhost:*",           // Локальная разработка
                        "guess-the-colleague-e286h2ibb-1wft1s-projects.vercel.app",  // Ваш Vercel домен
                        "https://*.vercel.app",          // Все Vercel поддомены
                        "https://*.serveousercontent.com" // Serveo туннель
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);  // Кэширование CORS preflight на 1 час
    }
}