package com.pstsearch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // React Router SPA: 점(.)이 없고 /api 로 시작하지 않는 단일 세그먼트 경로 → index.html
        registry.addViewController("/")
                .setViewName("forward:/index.html");
        registry.addViewController("/{path:^(?!api)[^\\.]*}")
                .setViewName("forward:/index.html");
    }
}
