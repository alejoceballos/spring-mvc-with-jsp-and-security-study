package com.momo2x.study.springmvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(@NonNull final ViewControllerRegistry registry) {
                registry.addViewController("/main").setViewName("main");
                registry.addViewController("/admin").setViewName("admin");
            }
        };
    }

}
